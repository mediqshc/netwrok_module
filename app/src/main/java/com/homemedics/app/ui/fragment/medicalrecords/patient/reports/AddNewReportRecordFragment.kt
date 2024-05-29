package com.homemedics.app.ui.fragment.medicalrecords.patient.reports

import android.text.format.DateFormat
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.request.emr.type.EMRTypeDeleteRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.records.CustomerEMRRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddNewReportRecordBinding
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class AddNewReportRecordFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAddNewReportRecordBinding
    private var isModify = false
    private val emrViewModel: EMRViewModel by activityViewModels()
    private val start = "\u2066"
    private val end = "\u2069"
    private val hash = "\u0023"
    private val pipe = "\u007C"

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.newMedicalRecord.getSafe()
            caRecord.apply {
                title = lang?.emrScreens?.labAndDiagnostics.getSafe()
                custDesc = lang?.emrScreens?.addLabReportDesc.getSafe()
            }
        }
    }

    override fun init() {
        setupViews()
        setDataInViews(null)
        getRecordsDetailsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_add_new_report_record

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddNewReportRecordBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }

            actionbar.onAction2Click = {
                DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                    title = lang?.globalString?.warning.getSafe(),
                    message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                    negativeButtonStringText = lang?.globalString?.cancel.getSafe(),
                    positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                    buttonCallback = {
                        deleteRecordApi()
                    }
                )
            }

            caRecord.onDeleteClick = { item, position ->
                deleteRecordItemApi(item.itemId?.toInt().getSafe(), position)
            }

            caRecord.onAddItemClick = {
                findNavController().safeNavigate(AddNewReportRecordFragmentDirections.actionAddNewReportRecordFragmentToSelectLabTestFragment())
            }

            bSave.setOnClickListener {
                saveRecordApi()
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun setupViews(){
        mBinding.apply {
            if(arguments?.containsKey(getString(R.string.modify)).getSafe()){
                isModify = true
                actionbar.action2Res = R.drawable.ic_delete_white
            }
        }
    }

    private fun setUserDetails(){
        mBinding.apply {
            emrViewModel.selectedFamily?.let {
                iDoctor.tvTitle.text = it.fullName
                iDoctor.tvDesc.text = getCurrentDateTime("dd MMMM yy")
                iDoctor.ivThumbnail.loadImage(it.profilePicture, getGenderIcon(it.genderId))
            } ?: kotlin.run {
                val user = DataCenter.getUser()
                iDoctor.tvTitle.text = user?.fullName
                iDoctor.tvDesc.text = getCurrentDateTime("dd MMMM yy")
                iDoctor.ivThumbnail.loadImage(
                    user?.profilePicture,
                    getGenderIcon(user?.genderId.toString())
                )
            }
        }
    }

    private fun setDataInViews(details: CustomerRecordResponse?){
        mBinding.apply {
            setUserDetails()

            bSave.isEnabled = details?.labTests.isNullOrEmpty().not()
            details?.let {
                iDoctor.apply {
                    tvTitle.text = details.partnerName.toString()

                    if(details.speciality.isNullOrEmpty()){
                        tvDesc.text = "${details.date}"
                    }
                    else{
                        tvDesc.text = "${details.speciality?.get(0)?.genericItemName} $pipe ${details.date}"
                    }

                    if(details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0"){
                        setUserDetails()
                    }
                    else {
                        tvTitle.text = details.partnerName.toString()
                        ivIcon.setImageResource(CustomServiceTypeView.ServiceType.getServiceById(details.serviceTypeId.getSafe().toInt())?.icon.getSafe())
                        ivThumbnail.invisible()
                    }
                }


                caRecord.listItems = ((details.labTests?.map {
                    it.drawable = R.drawable.ic_upload_file
//                    it.itemEndIcon = R.drawable.ic_delete
                    it
                }) as ArrayList<MultipleViewItem>?) ?: arrayListOf()

                if (isModify){
                    actionbar.title = "${lang?.emrScreens?.modifyText.getSafe()} ${lang?.emrScreens?.record?.lowercase()} $hash ${it.emrNumber}"
                }
            }
        }
    }

    private fun getRecordsDetailsApi(){
        val request = EMRDetailsRequest(
            emrId = emrViewModel.emrID,
            type = emrViewModel.selectedEMRType?.key
        )

        if (isOnline(requireActivity())) {
            emrViewModel.getCustomerEMRRecordsDetails(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<CustomerEMRRecordResponse>
                        setDataInViews(response.data?.emrDetails)
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

    private fun saveRecordApi(){
        val request = EMRDetailsRequest(
            emrId = emrViewModel.emrID,
            type = emrViewModel.selectedEMRType?.key,
            modify = if (isModify) 1 else null
        )

        if (isOnline(requireActivity())) {
            emrViewModel.saveCustomerEMRRecord(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        findNavController().popBackStack()
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

    private fun deleteRecordApi(){
        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRRecord().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        findNavController().popBackStack(R.id.customerReportDetailsFragment, true)
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

    private fun deleteRecordItemApi(emrTypeId: Int, pos: Int){
        val request = EMRTypeDeleteRequest(
            type = emrViewModel.selectedEMRType?.key,
            emrTypeId = emrTypeId
        )
        if (isOnline(requireActivity())) {
            emrViewModel.deleteCustomerEMRRecordType(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        showToast(response.message.getSafe())
                        mBinding.caRecord.listItems.removeAt(pos)
                        mBinding.caRecord.mBinding.rvItems.adapter?.notifyDataSetChanged()
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