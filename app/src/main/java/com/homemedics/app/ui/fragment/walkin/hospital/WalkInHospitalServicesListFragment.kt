package com.homemedics.app.ui.fragment.walkin.hospital

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.labtest.LabTestFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.labtest.LabTestListResponse
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentWalkinHospitalServicesListBinding
import com.homemedics.app.databinding.ItemBorderedTextviewBinding
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.adapter.LabTestListAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.LabTestViewModel
import com.homemedics.app.viewmodel.WalkInViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class WalkInHospitalServicesListFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentWalkinHospitalServicesListBinding

    private lateinit var adapter: LabTestListAdapter

    private val labTestViewModel: LabTestViewModel by activityViewModels()
    private val walkInViewModel: WalkInViewModel by activityViewModels()

    var delay: Long = 1000 // 1 seconds after user stops typing
    private var taskJob: Job? = null
    private var last_text_edit: Long = 0

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.labPharmacyScreen?.labTest.getSafe()
            etSearch.hint = lang?.labPharmacyScreen?.searchLabTest.getSafe()
        }
    }

    override fun init() {
        val savedBookingId = tinydb.getInt(com.fatron.network_module.utils.Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
        if(savedBookingId != 0){
            labTestViewModel.bookingIdResponse.bookingId = savedBookingId
        }

        val locale =  TinyDB.instance.getString(Enums.TinyDBKeys.LOCALE.key)
        mBinding.flTags.isRtl = locale != DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN

        populateDoctorList()
        setupFilterChips()
        setDataInViews()

        labTestViewModel.labTestFilterRequest.page = 1

        if(labTestViewModel.fromSearch.not()){
            getLabTests(labTestViewModel.labTestFilterRequest)
        }
        else {
            mBinding.etSearch.requestFocus()
            showKeypad()
        }
        labTestViewModel.fromSearch = false
    }

    private fun setupFilterChips(){
        labTestViewModel.labTestFilterRequest.apply {
            if(countryName.isNullOrEmpty().not()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.apply {
                    text = countryName
                    setVisible(false)
                }
                mBinding.flTags.addView(chipView.root)
            }
            if(cityName.isNullOrEmpty().not()){
                val chipView = ItemBorderedTextviewBinding.inflate(layoutInflater)
                chipView.tvTitle.text = cityName
                mBinding.flTags.addView(chipView.root)
            }
            if(categoryName.isNullOrEmpty().not()){
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

    private fun setDataInViews(){
        mBinding.apply {
            actionbar.dotText = labTestViewModel.orderDetailsResponse.labCartItems?.size.getSafe().toString()
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_lab_test_list

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentWalkinHospitalServicesListBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            adapter.itemClickListener = {
                item, _ ->
                labTestViewModel.selectedLabTest = item
                etSearch.text.clear()
                labTestViewModel.labTestFilterRequest.labTestId = item.id
//                findNavController().safeNavigate(LabTestListFragmentDirections.actionLabTestListFragmentToSelectMainLabFragment())
            }

            etSearch.doAfterTextChanged { s ->
                taskJob?.cancel()
                taskJob = lifecycleScope.launch {
                    if (s?.length.getSafe() > 0) {
                        last_text_edit = System.currentTimeMillis()
                        delay(delay)
                        if (System.currentTimeMillis() > last_text_edit + delay - 500) {
                            adapter.listItems.clear()
                            getLabTests(labTestViewModel.labTestFilterRequest)
                        }
                    } else {
                        last_text_edit = 0
                        adapter.listItems.clear()
                        adapter.listItems = arrayListOf()
                        tvNoData.visible()
                    }

                }
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1

    private fun populateDoctorList() {
        mBinding.apply {
            adapter = LabTestListAdapter()
            val spacing = resources.getDimensionPixelSize(R.dimen.dp16)
            rvLabTests.addItemDecoration(RecyclerViewItemDecorator(spacing, RecyclerViewItemDecorator.VERTICAL))
            rvLabTests.adapter = adapter
            val layoutManager = rvLabTests.layoutManager as LinearLayoutManager

            rvLabTests.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                                loading = false
                                Timber.e("Last Item Wow !")

                                labTestViewModel.labTestFilterRequest.page = currentPage?.plus(1)

                                if(isLastPage.not()){
                                    getLabTests(labTestViewModel.labTestFilterRequest)
                                    loading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun getLabTests(request: LabTestFilterRequest) {
        val queryText = mBinding.etSearch.text.toString()
        if(queryText.isNotEmpty())
            request.name = queryText
        else {
            request.name = null
        }

        if (isOnline(requireActivity())) {
            labTestViewModel.getLabTests(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<LabTestListResponse>
                        response.data?.let { res ->
                            isLastPage = request.page == res.lastPage
                            currentPage = res.currentPage

                            val tempList = adapter.listItems
                            tempList.addAll((res.labTests?.data as ArrayList<LabTestResponse>).getSafe())
                            adapter.listItems = tempList.distinctBy {
                                it.name
                            } as ArrayList<LabTestResponse>
                            adapter.originalList = adapter.listItems
                            adapter.notifyDataSetChanged()

                            loading = false

                            mBinding.tvNoData.setVisible(adapter.listItems.isEmpty())
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
                                message =   getErrorMessage(it.generalResponse.message.getSafe()).getSafe(),
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