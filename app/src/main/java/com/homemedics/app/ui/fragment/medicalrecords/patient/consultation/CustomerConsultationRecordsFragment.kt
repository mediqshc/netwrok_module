package com.homemedics.app.ui.fragment.medicalrecords.patient.consultation

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fatron.network_module.models.request.emr.customer.consultation.EMRConsultationFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.consultation.ConsultationRecordsListResponse
import com.fatron.network_module.models.response.emr.customer.consultation.ConsultationRecordsResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCustomerConsultationRecordsBinding
import com.homemedics.app.ui.adapter.EMRRecordsAdapter
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel
import timber.log.Timber

class CustomerConsultationRecordsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerConsultationRecordsBinding
    private lateinit var listAdapter: EMRRecordsAdapter
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.consultationRecords.getSafe()
            etSearch.hint = lang?.emrScreens?.searchByRecordNumber.getSafe()
        }
    }

    override fun init() {
        setupViews()

        mBinding.actionbar.desc = if(emrViewModel.selectedFamily == null) mBinding.lang?.globalString?.self.getSafe() else emrViewModel.selectedFamily?.fullName.getSafe()
        populateList()

        emrViewModel.consultationFilterRequest.customerId = if(emrViewModel.selectedFamily == null) DataCenter.getUser()?.id else emrViewModel.selectedFamily?.familyMemberId
        emrViewModel.consultationFilterRequest.page = 1

        handlePN()

        getRecordsApi(emrViewModel.consultationFilterRequest)

        setFilterViews()
    }

    override fun getFragmentLayout() = R.layout.fragment_customer_consultation_records

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerConsultationRecordsBinding
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
                findNavController().safeNavigate(CustomerConsultationRecordsFragmentDirections.actionCustomerConsultationRecordsFragmentToEMRConsultationFilterFragment())
            }

            etSearch.addTextChangedListener {
                listAdapter.filter.filter(it)
            }

            bAdd.setOnClickListener {
                emrViewModel.emrID = listAdapter.getSelectedItem()?.emrId.getSafe()
                if(emrViewModel.fromChat.not())
                attachEMRtoBDCApi()
                else {
                    emrViewModel.emrNum = listAdapter.getSelectedItem()?.emrNumber.getSafe()
                    findNavController().popBackStack(R.id.customerMedicalRecordsFragment, true)
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
                    emrViewModel.tempEmrID = item.emrId.getSafe()
                    findNavController().safeNavigate(CustomerConsultationRecordsFragmentDirections.actionCustomerConsultationRecordsFragmentToCustomerConsultationRecordDetailsFragment())
                }
            }
            listAdapter.onShareClick = { item, _ ->
                emrViewModel.emrID = item.emrId.getSafe()
                findNavController().safeNavigate(R.id.action_globalToShareRecordWith)
            }

            listAdapter.onDataFilter = { items ->
                rvList.setVisible(items.isNotEmpty())
                tvNoData.setVisible(items.isEmpty())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun handlePN(){
        if(emrViewModel.fromPush){
            emrViewModel.tempEmrID = emrViewModel.emrID
            findNavController().safeNavigate(CustomerConsultationRecordsFragmentDirections.actionCustomerConsultationRecordsFragmentToCustomerConsultationRecordDetailsFragment())
        }

        emrViewModel.fromPush = false //PN consumed
    }

    private var loading = false
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var isLastPage = false
    var currentPage: Int? = 1

    private fun populateList() {
        mBinding.apply {
            listAdapter = EMRRecordsAdapter(emrViewModel)
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

                                emrViewModel.consultationFilterRequest.page = currentPage?.plus(1)

                                if(isLastPage.not()){
                                    getRecordsApi(emrViewModel.consultationFilterRequest)
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
                bAdd.visible()
                actionbar.mBinding.apply {
                    ivAction3.gone()
                    ivAction2.gone()
                }
            }
        }
    }

    private fun setFilterViews(){
        mBinding.apply {
            emrViewModel.consultationFilterRequest.let {
                if(it.startDate.isNullOrEmpty()
                    && it.endDate.isNullOrEmpty()
                    && it.serviceType == null
                    && it.diagnosisId == null
                    && it.specialityId == null
                    && it.symptomId == null
                    && (it.sharedRecord == null || it.sharedRecord?.getBoolean().getSafe().not())){
                    ivFilter.setImageResource(R.drawable.ic_filter_black_with_border)
                }
                else {
                    ivFilter.setImageResource(R.drawable.ic_filter_with_rounded_primary)
                }
            }
        }
    }

    private fun getRecordsApi(request: EMRConsultationFilterRequest){
        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerConsultationRecords(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<ConsultationRecordsListResponse>
                        response.data?.let {
                            isLastPage = request.page == it.lastPage
                            currentPage = it.currentPage

                            val tempList = listAdapter.listItems
                            tempList.addAll(it.consultationRecords as ArrayList<ConsultationRecordsResponse>? ?: arrayListOf())
                            listAdapter.listItems = tempList.distinctBy {
                                it.emrNumber
                            } as ArrayList<ConsultationRecordsResponse>
                            listAdapter.originalList = listAdapter.listItems
                            listAdapter.notifyDataSetChanged()

                            mBinding.rvList.setVisible(listAdapter.listItems.isNotEmpty())
                            mBinding.tvNoData.setVisible(listAdapter.listItems.isEmpty())
                            if(emrViewModel.fromBDC){
                                mBinding.bAdd.isEnabled = listAdapter.listItems.isNotEmpty()
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
                    else -> { hideLoader() }
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
                    else -> { hideLoader() }
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