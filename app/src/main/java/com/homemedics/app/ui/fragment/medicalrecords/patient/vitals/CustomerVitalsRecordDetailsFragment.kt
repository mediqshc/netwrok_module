package com.homemedics.app.ui.fragment.medicalrecords.patient.vitals

import android.text.InputType
import android.text.format.DateFormat
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.EMRDownloadRequest
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.records.CustomerEMRRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVitalsDetailsBinding
import com.homemedics.app.databinding.ItemSharedUserViewBinding
import com.homemedics.app.ui.activity.EMRActivity
import com.homemedics.app.ui.custom.CustomDefaultEditText
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class CustomerVitalsRecordDetailsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAddVitalsDetailsBinding
    private var isSharedPerson = false
    private val emrViewModel: EMRViewModel by activityViewModels()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.newMedicalRecord.getSafe()
            etRecordDate.hint = lang?.emrScreens?.dateAndTime.getSafe()
            etHeartRate.hint = lang?.emrScreens?.heartRate.getSafe()
            etTemperature.hint = lang?.emrScreens?.temperature.getSafe()
            etSystolicBP.hint = lang?.emrScreens?.systolicBp.getSafe()
            etDiastolicBP.hint = lang?.emrScreens?.diastolicBp.getSafe()
            etOxygenLevel.hint = lang?.emrScreens?.oxygenLevel.getSafe()
            etBloodSugar.hint = lang?.emrScreens?.bloodSugarLevel.getSafe()
        }
    }

    override fun init() {
        addSuffixIntoFields()

        setupViews()
        setDataInViews(null)

        if(emrViewModel.emrID != 0) //syncing tempEmrID and emrID
            emrViewModel.tempEmrID = emrViewModel.emrID

        if (emrViewModel.tempEmrID != 0)
            emrViewModel.emrID = emrViewModel.tempEmrID

        getRecordsDetailsApi()
    }

    override fun getFragmentLayout() = R.layout.fragment_add_vitals_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddVitalsDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction1Click = {
                findNavController().popBackStack()
            }
            actionbar.onAction2Click = {
                findNavController().safeNavigate(R.id.action_globalToShareRecordWith)
            }
            actionbar.onAction3Click = {
                val request = EMRDownloadRequest(
                    emrId = emrViewModel.emrID,
                    type = emrViewModel.selectedEMRType?.key
                )
                downloadReportApi(request)
            }

            etRecordDate.clickCallback = {
                openCalender(
                    etRecordDate.mBinding.editText,
                    valueOnlyReturn = true,
                    onDismiss = { dateSelected ->
                        if(dateSelected.isNotEmpty()){
                            openTimeDialog(
                                etRecordDate.mBinding.editText,
                                valueOnlyReturn = true,
                                onDismiss = { timeSelected ->
                                    if(timeSelected.isNotEmpty()) {
                                        etRecordDate.text = "$dateSelected || $timeSelected"
                                    }
                                },
                                parentFragment = parentFragmentManager
                            )
                        }
                    }
                )
            }

            etRecordDate.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etHeartRate.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etTemperature.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etSystolicBP.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etDiastolicBP.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etOxygenLevel.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etBloodSugar.mBinding.editText.doAfterTextChanged {
                validate()
            }

            bSaveRecord.setOnClickListener {
                findNavController().safeNavigate(CustomerVitalsRecordDetailsFragmentDirections.actionCustomerVitalsRecordDetailsFragmentToAddNewVitalsRecordFragment2())
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun addSuffixIntoFields() {
        mBinding.apply {
            etHeartRate.mBinding.textInputLayout.suffixText = resources.getString(R.string.bpm)
            etTemperature.mBinding.textInputLayout.suffixText = resources.getString(R.string.frenheight)
            etSystolicBP.mBinding.textInputLayout.suffixText = resources.getString(R.string.bp_unit)
            etDiastolicBP.mBinding.textInputLayout.suffixText = resources.getString(R.string.bp_unit)
            etOxygenLevel.mBinding.textInputLayout.suffixText = resources.getString(R.string.oxygen_unit)
            etBloodSugar.mBinding.textInputLayout.suffixText = resources.getString(R.string.sugar_unit)
        }
    }

    private fun validate(){
//        mBinding.apply {
//            bSaveRecord.isEnabled = isValid(etRecordDate.text) && (
//                        isValid(etHeartRate.text)
//                            || isValid(etHeartRate.text)
//                            || isValid(etTemperature.text)
//                            || isValid(etSystolicBP.text)
//                            || isValid(etDiastolicBP.text)
//                            || isValid(etOxygenLevel.text)
//                            || isValid(etBloodSugar.text)
//                    )
//        }
    }

    private fun setupViews(){
        mBinding.apply {
            actionbar.action2Res = R.drawable.ic_share
            actionbar.action3Res = R.drawable.ic_download_white

            bSaveRecord.isEnabled = true
            bSaveRecord.text = lang?.emrScreens?.modifyText.getSafe()

            mBinding.etRecordDate.inputType = InputType.TYPE_NULL
            mBinding.etHeartRate.inputType = InputType.TYPE_NULL
            mBinding.etTemperature.inputType = InputType.TYPE_NULL
            mBinding.etSystolicBP.inputType = InputType.TYPE_NULL
            mBinding.etDiastolicBP.inputType = InputType.TYPE_NULL
            mBinding.etOxygenLevel.inputType = InputType.TYPE_NULL
            mBinding.etBloodSugar.inputType = InputType.TYPE_NULL

            llRecordDate.gone()
        }
    }

    private fun setDataInViews(details: CustomerRecordResponse?){
        val timeFormat = if (DateFormat.is24HourFormat(binding.root.context))
            "HH:mm"
        else "hh:mm aa"

        val hash = "\u0023"
        mBinding.apply {
            details?.let {
                actionbar.title = "${lang?.emrScreens?.record} $hash ${details.emrNumber}"

                isSharedPerson = details.customerUser?.id != DataCenter.getUser()?.id
                if(isSharedPerson)
                    mBinding.actionbar.action2Res = 0

                iDoctor.apply {
                    iDoctor.tvTitle.text = details.partnerName

                    var date = ""
                    if(details.originalDate.isNullOrEmpty()){
                        date = details.date.getSafe()
                    }
                    else{
                        date = details.originalDate.getSafe()
                        date = getDateInFormat(date, "yyyy-MM-dd hh:mm:ss", "dd MMMM yyyy ${Constants.PIPE}${Constants.PIPE} ${Constants.START}$timeFormat${Constants.END}")
                    }

                    if(details.speciality.isNullOrEmpty())
                        tvDesc.text = date
                    else
                        tvDesc.text = "${details.speciality?.get(0)?.genericItemName} ${Constants.PIPE} $date"


                    if(details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0"){
                        val user = DataCenter.getUser()
                        ivThumbnail.loadImage(user?.profilePicture, getGenderIcon(user?.genderId.toString()))
                        ivThumbnail.visible()
                        ivIcon.invisible()
                    }
                    else {
                        ivIcon.setImageResource(CustomServiceTypeView.ServiceType.getServiceById(details.serviceTypeId.getSafe().toInt())?.icon.getSafe())
                        ivThumbnail.invisible()
                    }
                }

                it.vitals?.forEach { vital ->
                    val editText: CustomDefaultEditText = mBinding.root.findViewWithTag(vital.key)

                    editText.text = vital.value.getSafe()
                }

                if(details.originalDate!=null) {
                    val dateFormat = "dd/MM/yyyy ${Constants.PIPE}${Constants.PIPE} ${Constants.START}$timeFormat${Constants.END}"
                    etRecordDate.text =
                        getDateInFormat(details.originalDate.getSafe(), "yyyy-MM-dd HH:mm", dateFormat)
                }
                else {
                    etRecordDate.text = details.date.getSafe()
                }

                bSaveRecord.setVisible(details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0")
            }

            emrViewModel.selectedFamilyForShare = details?.shared as ArrayList<FamilyConnection>? ?: arrayListOf()
            setSharedFamily()
        }

        validate()
    }

    private fun setSharedFamily() {
        mBinding.apply {
            llSharedWithMain.setVisible(emrViewModel.selectedFamilyForShare.isNotEmpty() && isSharedPerson.not())
            sharedDivider.setVisible(emrViewModel.selectedFamilyForShare.isNotEmpty() && isSharedPerson.not())
            val showLimit = 3

            if(emrViewModel.selectedFamilyForShare.isNotEmpty()){
                val remainingCount = emrViewModel.selectedFamilyForShare.size - showLimit

                run breaking@{
                    emrViewModel.selectedFamilyForShare.forEachIndexed { index, familyConnection ->
                        val itemBinding = ItemSharedUserViewBinding.inflate(layoutInflater)
                        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)

                        if(index != 0)
                            params.marginStart = resources.getDimensionPixelSize(R.dimen.dp8)

                        if(index < showLimit){
                            itemBinding.tvTitle.text = familyConnection.fullName
                            itemBinding.ivThumbnail.loadImage(familyConnection.userProfilePicture?.file, R.drawable.ic_profile_placeholder)

                            params.weight = 1.3f

                            itemBinding.root.layoutParams = params
                            llSharedWith.addView(itemBinding.root)
                        }
                        else{
                            itemBinding.tvTitle.text = "$remainingCount+"
//                            itemBinding.tvTitle.text = "10+"
                            itemBinding.tvTitle.setPadding(
                                resources.getDimensionPixelSize(R.dimen.dp8),
                                0,
                                resources.getDimensionPixelSize(R.dimen.dp8),
                                0
                            )
                            itemBinding.ivThumbnail.gone()
                            params.width = LinearLayout.LayoutParams.WRAP_CONTENT
                            itemBinding.root.layoutParams = params
                            llSharedWith.addView(itemBinding.root)
                            return@breaking
                        }
                    }
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

    private fun saveRecordApi(request: EMRDetailsRequest){
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

    private fun downloadReportApi(request: EMRDownloadRequest){
        if (isOnline(requireActivity())) {
            emrViewModel.downloadEMR(request, (requireActivity() as EMRActivity).fileUtils).observe(this){
                when (it) {
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