package com.homemedics.app.ui.fragment.pharmacy

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.pharmacy.PharmacyProductRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.pharmacy.PharmacyProduct
import com.fatron.network_module.models.response.pharmacy.PharmacyProductsListResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPharmacyMedicineListBinding
import com.homemedics.app.databinding.ItemBorderedTextviewBinding
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.PharmacyListAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.PharmacyViewModel
import kotlinx.coroutines.launch

class PharmacyMedicineListFragment : BaseFragment(), View.OnClickListener {

    private val pharmacyViewModel: PharmacyViewModel by activityViewModels()

    private lateinit var mBinding: FragmentPharmacyMedicineListBinding

    private lateinit var adapter: PharmacyListAdapter

    private val pharmaList: ArrayList<PharmacyProduct> = arrayListOf()

    private var langData: RemoteConfigLanguage? = null

    private var isReload = false

    private var loading = false

    private var isLastPage = false

    private var pastVisibleItems = 0

    private var visibleItemCount = 0

    private var totalItemCount = 0

    private var currentPage: Int? = 1

    var handler: Handler = Handler(Looper.getMainLooper())

    var delay: Long = 1000 // 1 seconds after user stops typing

    var lastTextEdit: Long = 0

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            val search = mBinding.etSearch.text.toString()
            pharmacyViewModel.page = 0
            lifecycleScope.launch {
                adapter.listItems.clear()
                pharmaList.clear()
                pharmacyViewModel.pharmacyProducts.value = null
                getPharmacyProductsList(category = 0, displayName = search, page = pharmacyViewModel.page)
                pharmacyViewModel.fromSearch = true
            }
        }
    }

    override fun setLanguageData() {
        langData = ApplicationClass.mGlobalData
        mBinding.apply {
            lang = langData
            actionbar.title = langData?.labPharmacyScreen?.pharmacyTitle.getSafe()
            etSearch.hint = langData?.labPharmacyScreen?.eGPanadolNebulizerEnsure.getSafe()
        }
    }

    override fun init() {
        mBinding.actionbar.dotText = pharmacyViewModel.products?.size.getSafe().toString()
        val locale =  TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        mBinding.flTags.isRtl = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
        setObserver()
        if (!pharmacyViewModel.fromSearch)
            getPharmacyProductsList()

        populateMedicineProductList()
    }

    override fun onResume() {
        super.onResume()
        setupFilterChips()
    }

    override fun onPause() {
        super.onPause()
        closeKeypad()
        isReload = true
        mBinding.etSearch.setText("")
        pharmacyViewModel.fromSearch = false
        mBinding.etSearch.clearFocus()
        pharmaList.clear()
        adapter.listItems.clear()
        pharmacyViewModel.page = 1
        pharmacyViewModel.pharmacyProducts.value = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.etSearch.setText("")
        isReload = false
        pharmaList.clear()
        adapter.listItems.clear()
        pharmacyViewModel.fromSearch = false
        handler.removeCallbacks(inputFinishChecker)
        pharmacyViewModel.page = 1
        pharmacyViewModel.pharmacyProducts.value = null
    }

    override fun getFragmentLayout() = R.layout.fragment_pharmacy_medicine_list

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPharmacyMedicineListBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.apply {
                onAction1Click = {
                    closeKeypad()
                    findNavController().popBackStack()
                }
                onAction2Click = {
                    closeKeypad()
                    if (pharmacyViewModel.products?.isEmpty().getSafe() || pharmacyViewModel.products == null) {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message = langData?.labPharmacyScreen?.emptyCart.getSafe(),
                                buttonCallback = {},
                            )
                    } else {
                        findNavController().safeNavigate(
                            PharmacyMedicineListFragmentDirections.actionPharmacyMedicineListFragmentToPharmacyCartDetailsFragment()
                        )
                    }
                }
            }

            etSearch.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    handler.removeCallbacks(inputFinishChecker)
                }

                override fun afterTextChanged(s: Editable?) {
                    //avoid triggering event when text is empty
                    if (s?.length.getSafe() > 0) {
                        pharmacyViewModel.fromSearch = true
                        lastTextEdit = System.currentTimeMillis()
                        handler.postDelayed(inputFinishChecker, delay)
                    } else {
                        lastTextEdit = 0
                        adapter.listItems.clear()
                        adapter.listItems = arrayListOf()
                        tvNoData.visible()
                        pharmacyViewModel.fromSearch = false
                        pharmacyViewModel.page = 1
                        if (etSearch.hasFocus())
                            getPharmacyProductsList()
                    }
                }
            })

            adapter.apply {
                itemClickListener = { item, _ ->
                    closeKeypad()
                    pharmacyViewModel.pharmaProductId = item.id.getSafe()
                    etSearch.clearFocus()
                    findNavController().safeNavigate(
                        PharmacyMedicineListFragmentDirections.actionPharmacyMedicineListFragmentToPharmacyProductDetailsFragment()
                    )
                }
                onDataFilter = { items ->
                    rvPharmacy.setVisible(items.isNotEmpty())
                    tvNoData.setVisible(items.isEmpty())
                }
            }

            ivSort.setOnClickListener(this@PharmacyMedicineListFragment)

            val layoutManager = rvPharmacy.layoutManager as LinearLayoutManager
            rvPharmacy.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (pharmacyViewModel.fromSearch.not()) {
                        if (dy > 0) { //check for scroll down
                            visibleItemCount = layoutManager.childCount
                            totalItemCount = layoutManager.itemCount
                            pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                            if (loading.not()) {
                                if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                                    pharmacyViewModel.page = currentPage?.plus(1)
                                    if(isLastPage.not()) {
                                        if (pharmacyViewModel.fromSearch) {
                                            val search = mBinding.etSearch.text.toString()
                                            getPharmacyProductsList(category = 0, displayName = search, page = pharmacyViewModel.page)
                                        } else {
                                            getPharmacyProductsList(page = pharmacyViewModel.page)
                                        }
                                        loading = true
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.ivSort -> {
                closeKeypad()
                findNavController().safeNavigate(
                    PharmacyMedicineListFragmentDirections.actionPharmacyMedicineListFragmentToPharmacyMedicinesFilterFragment()
                )
            }
        }
    }

    private fun setObserver() {
        if (isReload.not()) {
            pharmacyViewModel.pharmacyProducts.observe(this) {
                it ?: return@observe
                mBinding.apply {
                    rvPharmacy.setVisible((it.isNullOrEmpty().not()))
                    tvNoData.setVisible((it.isNullOrEmpty()))
                }
                pharmaList.apply {
                    clear()
                    addAll(it)
                }
                adapter.listItems = pharmaList.getSafe()
            }
        }
    }

    private fun populateMedicineProductList() {
        mBinding.apply {
            adapter = PharmacyListAdapter()
            rvPharmacy.adapter = adapter
        }
    }

    private fun setupFilterChips(){
        mBinding.flTags.removeAllViews()
        pharmacyViewModel.apply {
            if(country.isNotEmpty()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.apply {
                    text = country
                    gone()
                }
                mBinding.flTags.addView(chipView.root)

            }
            if(city.isNotEmpty()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = city
                mBinding.flTags.addView(chipView.root)
            }
            if(categoryName.isNotEmpty()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = categoryName
                mBinding.flTags.addView(chipView.root)
            }
        }

        mBinding.apply {
            tvFilterBadge.text = "${flTags.childCount}"
            tvFilterBadge.setVisible(flTags.childCount > 0)
        }
    }

    private fun getPharmacyProductsList(category: Int = pharmacyViewModel.categoryId, displayName: String? = null, page: Int? = 1) {
        val cityId = if (pharmacyViewModel.cityId != 0) pharmacyViewModel.cityId else null
        val countryId = if (pharmacyViewModel.countryId != 0) pharmacyViewModel.countryId else null
        val request = PharmacyProductRequest(categoryId = category, displayName = displayName, page = page, cityId = cityId, countryId = countryId)
        pharmacyViewModel.getPharmacyProductsList(request).observe(this) {
            when(it) {
                is ResponseResult.Success -> {
                    val response = it.data as ResponseGeneral<*>
                    (response.data as PharmacyProductsListResponse).let { pharmacyProducts ->
                        pharmacyViewModel.pharmacyProducts.value?.clear()
                        isLastPage = request.page == pharmacyProducts.lastPage
                        currentPage = pharmacyProducts.currentPage

                        val tempList = adapter.listItems
                        tempList.addAll(pharmacyProducts.products as ArrayList<PharmacyProduct>?  ?: arrayListOf())

                        pharmacyViewModel.pharmacyProducts.postValue(
                            tempList.distinctBy { pharmaProd ->
                                pharmaProd.id
                            } as ArrayList<PharmacyProduct>? ?: arrayListOf()
                        )
                        adapter.notifyDataSetChanged()
                        loading = false
                    }
                }
                is ResponseResult.Failure -> {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = it.error.message.getSafe(),
                            buttonCallback = {},
                        )
                }
                is ResponseResult.ApiError -> {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            message = getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
                            buttonCallback = {},
                        )
                }
                is ResponseResult.Pending -> {
                    showLoader()
                }
                is ResponseResult.Complete -> {
                    hideLoader()
                }
                else -> { hideLoader() }
            }
        }
    }
}