package com.homemedics.app.ui.fragment.medicalrecords.patient.medications

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.type.GenericEMRListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentSelectLabTestBinding
import com.homemedics.app.ui.adapter.SelectMedicineAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class SelectMedicineFragment : BaseFragment() {

    private lateinit var mBinding: FragmentSelectLabTestBinding
    private lateinit var adapter: SelectMedicineAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()
    private val pharmaList: ArrayList<GenericItem> = arrayListOf()
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var delay: Long = 1000 // 1 seconds after user stops typing
    private var lastTextEdit: Long = 0
    private var fromSearch = false
    private var loading = false
    private var pastVisibleItems = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var isLastPage = false
    private var currentPage: Int? = 1
    private var isReload = false

    private val inputFinishChecker = Runnable {
        if (System.currentTimeMillis() > lastTextEdit + delay - 500) {
            val search = mBinding.etSearch.text.toString()
            emrViewModel.emrFilterRequest.page = 0
            lifecycleScope.launch {
                adapter.listItems.clear()
                pharmaList.clear()
                emrViewModel.pharmacyProducts.value = null
                getMedicines(PageRequest(page = emrViewModel.emrFilterRequest.page, displayName = search))
            }
        }
    }

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.globalString?.selectMedicine.getSafe()
            etSearch.hint = lang?.emrScreens?.searchMedicineByName.getSafe()
        }
    }

    override fun init() {
        setObserver()
        populateList()

        if (fromSearch.not()) {
            val request = PageRequest(1)
            getMedicines(request)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_select_lab_test

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentSelectLabTestBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }

            adapter.onCheckChange = {
                bSelect.isEnabled = adapter.getSelectedItem() != null
                adapter.getSelectedItem()?.let {
                    emrViewModel.selectedRecord = adapter.getSelectedItem()
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
                        fromSearch = true
                        lastTextEdit = System.currentTimeMillis()
                        handler.postDelayed(inputFinishChecker, delay)
                    } else {
                        lastTextEdit = 0
                        adapter.listItems.clear()
                        adapter.listItems = arrayListOf()
                        tvNoData.visible()
                        fromSearch = false
                        val request = PageRequest(page = 1)
                        if (etSearch.hasFocus())
                            getMedicines(request)
                    }
                }
            })

            bSelect.setOnClickListener {
                etSearch.clearFocus()
                findNavController().safeNavigate(SelectMedicineFragmentDirections.actionSelectMedicineFragmentToAddMedicineDetailsFragment())
            }
        }
    }

    private fun setObserver() {
        if (isReload.not()) {
            emrViewModel.pharmacyProducts.observe(this) {
                it ?: return@observe
                mBinding.apply {
//                rvList.setVisible((it.isNullOrEmpty().not()))
                    tvNoData.setVisible((it.isNullOrEmpty()))
                }
                pharmaList.apply {
                    clear()
                    addAll(it)
                }
                adapter.listItems = pharmaList
            }
        }
    }

    override fun onPause() {
        super.onPause()
        closeKeypad()
        isReload = true
        pharmaList.clear()
        adapter.listItems.clear()
        mBinding.etSearch.setText("")
        fromSearch = false
        mBinding.etSearch.clearFocus()
        emrViewModel.emrFilterRequest.page = 1
        emrViewModel.pharmacyProducts.value = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // flush data
        mBinding.etSearch.setText("")
        isReload = false
        fromSearch = false
        handler.removeCallbacks(inputFinishChecker)
        pharmaList.clear()
        adapter.listItems.clear()
        emrViewModel.emrFilterRequest.page = 1
        emrViewModel.pharmacyProducts.value = null
    }

    private fun populateList() {
        mBinding.apply {
            adapter = SelectMedicineAdapter()
            rvList.adapter = adapter
            val layoutManager = rvList.layoutManager as LinearLayoutManager
            rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (fromSearch.not()) {
                        if (dy > 0) { //check for scroll down
                            visibleItemCount = layoutManager.childCount
                            totalItemCount = layoutManager.itemCount
                            pastVisibleItems = layoutManager.findFirstVisibleItemPosition()
                            if (loading.not()) {
                                if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                                    Timber.e("Last Item Wow !")
                                    emrViewModel.emrFilterRequest.page = currentPage?.plus(1)
                                    if(isLastPage.not()) {
                                        if (fromSearch) {
                                            val search = mBinding.etSearch.text.toString()
                                            getMedicines(PageRequest(page = emrViewModel.emrFilterRequest.page, displayName = search))
                                        } else {
                                            getMedicines(PageRequest(page = emrViewModel.emrFilterRequest.page))
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

    private fun getMedicines(pageRequest: PageRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.getMedicineList(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        (response.data as GenericEMRListResponse).let { res ->

                            isLastPage = pageRequest.page == res.products?.lastPage
                            currentPage = res.products?.currentPage

                            val tempList = adapter.listItems
                            tempList.addAll(res.products?.data as ArrayList<GenericItem>?  ?: arrayListOf())

                            emrViewModel.pharmacyProducts.postValue(
                                tempList.distinctBy { item ->
                                    item.genericItemId
                                }.getSafe()
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
                    else -> {}
                }
            }
        } else {
            DialogUtils(requireActivity())
                .showSingleButtonAlertDialog(
                    title = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    message = mBinding.lang?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}