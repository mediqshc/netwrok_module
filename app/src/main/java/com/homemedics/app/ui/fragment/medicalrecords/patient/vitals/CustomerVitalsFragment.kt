package com.homemedics.app.ui.fragment.medicalrecords.patient.vitals

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.emr.customer.records.EMRRecordsFilterRequest
import com.fatron.network_module.models.request.emr.type.SymptomsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordsListResponse
import com.fatron.network_module.models.response.emr.type.EmrCreateResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCustomerVitalsBinding
import com.homemedics.app.ui.adapter.EMRVitalsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import timber.log.Timber

class CustomerVitalsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerVitalsBinding
    private lateinit var listAdapter: EMRVitalsAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.vitals.getSafe()
            etSearch.hint = lang?.emrScreens?.searchByRecordNumber.getSafe()
        }
    }

    override fun init() {
        setupViews()

        mBinding.actionbar.desc = if(emrViewModel.selectedFamily == null) mBinding.lang?.globalString?.self.getSafe() else emrViewModel.selectedFamily?.fullName.getSafe()
        populateList()
        setFilterViews()

        emrViewModel.emrFilterRequest.apply {
            customerId = if(emrViewModel.selectedFamily == null) DataCenter.getUser()?.id else emrViewModel.selectedFamily?.familyMemberId
            page = 1
            type = emrViewModel.selectedEMRType?.key
        }
        getRecordsApi(emrViewModel.emrFilterRequest)
    }

    override fun getFragmentLayout() = R.layout.fragment_customer_vitals

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerVitalsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            actionbar.onAction2Click = {
                findNavController().safeNavigate(R.id.action_globalToSelectFamilyFragment)
            }
            actionbar.mBinding.tvDesc.setOnClickListener {
                if(emrViewModel.fromBDC || emrViewModel.fromChat)
                    return@setOnClickListener
                findNavController().safeNavigate(R.id.action_globalToSelectFamilyFragment)
            }

            ivFilter.setOnClickListener {
                findNavController().safeNavigate(CustomerVitalsFragmentDirections.actionCustomerVitalsFragmentToCustomerEMRFilterRecordsFragment2())
            }

            etSearch.addTextChangedListener {
                listAdapter.filter.filter(it)
            }

            bAddRecord.setOnClickListener {
                if(emrViewModel.fromBDC){
                    emrViewModel.emrID = listAdapter.getSelectedItem()?.emrId.getSafe()
                    if(emrViewModel.fromChat.not())
                        attachEMRtoBDCApi()
                    else {
                        emrViewModel.emrNum = listAdapter.getSelectedItem()?.emrNumber.getSafe()
                        findNavController().popBackStack(R.id.customerMedicalRecordsFragment, true)
                    }
                }
                else{
                    emrCreateApi()
                }
            }

            listAdapter.itemClickListener = {
                item, pos ->
                if(emrViewModel.fromBDC){
                    listAdapter.apply {
                        listItems.map { it.isSelected = false }
                        listItems[pos].isSelected = true
                        notifyDataSetChanged()
                    }
                }
                else {
                    emrViewModel.emrID = item.emrId.getSafe()
                    findNavController().safeNavigate(CustomerVitalsFragmentDirections.actionCustomerVitalsFragmentToCustomerVitalsRecordDetailsFragment())
                }
            }

            listAdapter.onShareClick = { item, position ->
                emrViewModel.emrID = item.emrId.getSafe()
                findNavController().safeNavigate(R.id.action_globalToShareRecordWith)
            }

            listAdapter.onDataFilter = { items ->
                rvList.setVisible(items.isNotEmpty())
                tvNoData.setVisible(items.isEmpty())
            }

            if(emrViewModel.fromBDC){
                listAdapter.onCheckChange = {
                    bAddRecord.isEnabled = listAdapter.getSelectedItem() != null
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

    private fun populateList() {
        mBinding.apply {
            listAdapter = EMRVitalsAdapter(emrViewModel)
            listAdapter.lang = lang
            rvList.adapter = listAdapter
            val layoutManager = rvList.layoutManager as LinearLayoutManager

            rvList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.childCount
                        totalItemCount = layoutManager.itemCount
                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition()
                        if (loading.not()) {
                            if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                                loading = false
                                Timber.e("Last Item Wow !")

                                emrViewModel.emrFilterRequest.page = currentPage?.plus(1)

                                if(isLastPage.not()){
                                    getRecordsApi(emrViewModel.emrFilterRequest)
                                    loading = true
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    private fun setupViews(){
        mBinding.apply {
            if(emrViewModel.fromBDC){
                bAddRecord.text = lang?.globalString?.add.getSafe()
                actionbar.mBinding.apply {
                    ivAction3.gone()
                    ivAction2.gone()
                }
            }
        }
    }

    private fun setFilterViews(){
        mBinding.apply {
            emrViewModel.emrFilterRequest.let {
                if(it.startDate.isNullOrEmpty()
                    && it.endDate.isNullOrEmpty()
                    && it.referredBy == null
                    && it.medicineType == null
                    && it.procedureType == null
                    && (it.sharedRecord == null || it.sharedRecord?.getBoolean().getSafe().not())){
                    ivFilter.setImageResource(R.drawable.ic_filter_black_with_border)
                }
                else {
                    ivFilter.setImageResource(R.drawable.ic_filter_with_rounded_primary)
                }
            }
        }
    }

    private fun getRecordsApi(request: EMRRecordsFilterRequest){
        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerEMRRecords(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CustomerRecordsListResponse>
                        response.data?.let {
                            isLastPage = request.page == it.lastPage
                            currentPage = it.currentPage

                            val tempList = listAdapter.listItems
                            tempList.addAll(it.records as ArrayList<CustomerRecordResponse>? ?: arrayListOf())
                            listAdapter.listItems = tempList.distinctBy {
                                it.emrNumber
                            } as ArrayList<CustomerRecordResponse>
                            listAdapter.originalList = listAdapter.listItems
                            listAdapter.notifyDataSetChanged()

                            mBinding.rvList.setVisible(listAdapter.listItems.isNotEmpty())
                            mBinding.tvNoData.setVisible(listAdapter.listItems.isEmpty())

                            if(emrViewModel.fromBDC){
                                mBinding.bAddRecord.isEnabled = listAdapter.listItems.isNotEmpty()
                            }

                            loading = false
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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

    private fun emrCreateApi(){
        val request = SymptomsRequest(customerId = emrViewModel.emrFilterRequest.customerId.toString())

        if (isOnline(requireActivity())) {
            emrViewModel.emrCreate(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EmrCreateResponse>
                        response.data?.let { emrResponse ->
                            emrViewModel.emrID = emrResponse.emrId.getSafe()

                            findNavController().safeNavigate(CustomerVitalsFragmentDirections.actionCustomerVitalsFragmentToAddNewVitalsRecordFragment2())
                        }
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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

    private fun attachEMRtoBDCApi(){
        if (isOnline(requireActivity())) {
            emrViewModel.attachEMRtoBDC().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        findNavController().popBackStack(R.id.customerMedicalRecordsFragment, true)
                    }
                    is ResponseResult.Failure -> {
                        DialogUtils(requireActivity())
                            .showSingleButtonAlertDialog(
                                message =it.error.message.getSafe(),
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