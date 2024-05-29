package com.homemedics.app.ui.fragment.medicalrecords.patient.vitals

import android.text.format.DateFormat
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.Vital
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.records.CustomerEMRRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddVitalsDetailsBinding
import com.homemedics.app.ui.custom.CustomDefaultEditText
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class AddNewVitalsRecordFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentAddVitalsDetailsBinding

    private var isModify = false

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var timeFormat: String

    private val pipe = "\u007C"
    private val start = "\u2066"
    private val end = "\u2069"

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
            bSaveRecord.text = lang?.emrScreens?.saveRecord.getSafe()
        }
    }

    override fun init() {
        timeFormat =
            if (DateFormat.is24HourFormat(binding.root.context)) "$start HH:mm$end"
            else "hh:mm aa"

        timeFormat = "$start$timeFormat$end"

        addSuffixIntoFields()

        setupViews()
        setDataInViews(null)

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
                if(isModify){
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
                else
                    findNavController().safeNavigate(R.id.action_globalToShareRecordWith)
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
                                        etRecordDate.text = "$dateSelected $pipe$pipe $start$timeSelected$end"
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
                val heartRate = mBinding.etHeartRate.text
                val temperature = mBinding.etTemperature.text
                val systolic = mBinding.etSystolicBP.text
                val diastolic = mBinding.etDiastolicBP.text
                val oxygenLevel = mBinding.etOxygenLevel.text
                val bloodSugar = mBinding.etBloodSugar.text
                val vitalsUnits  = metaData?.emrVitalsUnits

                val vitals = arrayListOf<Vital>().apply {
                    if (heartRate.isNotEmpty()) {
                        add(Vital(
                            key = mBinding.etHeartRate.tag.toString(),
                            unit = vitalsUnits?.find { it.genericItemId == Enums.EMRVitalsUnits.HEART_RATE.key }?.genericItemId.toString(),
                            value = heartRate
                        ))
                    }
                    if (temperature.isNotEmpty()) {
                        add(Vital(
                            key = mBinding.etTemperature.tag.toString(),
                            unit = vitalsUnits?.find { it.genericItemId == Enums.EMRVitalsUnits.TEMPERATURE.key }?.genericItemId.toString(),
                            value = temperature
                        ))
                    }
                    if (systolic.isNotEmpty() || diastolic.isNotEmpty()) {
                        if (systolic.isEmpty()) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = lang?.labPharmacyScreen?.alert.getSafe(),
                                    message = lang?.labPharmacyScreen?.systolic.getSafe(),
                                )
                            return@setOnClickListener
                        } else if (diastolic.isEmpty()) {
                            DialogUtils(requireActivity())
                                .showSingleButtonAlertDialog(
                                    title = lang?.labPharmacyScreen?.alert.getSafe(),
                                    message = lang?.labPharmacyScreen?.diastolic.getSafe(),
                                )
                            return@setOnClickListener
                        }
                        add(Vital(
                            key = mBinding.etSystolicBP.tag.toString(),
                            unit = vitalsUnits?.find { it.genericItemId == Enums.EMRVitalsUnits.SYSTOLIC_DIASTOLIC.key }?.genericItemId.toString(),
                            value = systolic
                        ))
                        add(Vital(
                            key = mBinding.etDiastolicBP.tag.toString(),
                            unit = vitalsUnits?.find { it.genericItemId == Enums.EMRVitalsUnits.SYSTOLIC_DIASTOLIC.key }?.genericItemId.toString(),
                            value = diastolic
                        ))
                    }
                    if (oxygenLevel.isNotEmpty()) {
                        add(Vital(
                            key = mBinding.etOxygenLevel.tag.toString(),
                            unit = vitalsUnits?.find { it.genericItemId == Enums.EMRVitalsUnits.OXYGEN_LEVEL.key }?.genericItemId.toString(),
                            value = oxygenLevel
                        ))
                    }
                    if (bloodSugar.isNotEmpty()) {
                        add(Vital(
                            key = mBinding.etBloodSugar.tag.toString(),
                            unit = vitalsUnits?.find { it.genericItemId == Enums.EMRVitalsUnits.BLOOD_SUGAR.key }?.genericItemId.toString(),
                            value = bloodSugar
                        ))
                    }
                }
                val splitDate= etRecordDate.text.split("||")
                var date:String?=null
                if (splitDate.isNotEmpty() && splitDate.size > 1)
                { date= "${splitDate[0].trim()} ${splitDate[1].trim()}"

                    date=  getDateInFormat(date,"dd/MM/yyyy $timeFormat","yyyy-MM-dd HH:mm")
                }
                val request = EMRDetailsRequest(
                    emrId = emrViewModel.emrID,
                    type = emrViewModel.selectedEMRType?.key,
                    vitals = vitals,
                    date =  date,
                    modify = if (isModify) 1 else null
                )

                saveRecordApi(request)
            }
            editTextsChangeListener()
        }
    }

    override fun onClick(v: View?) {

    }

    private fun editTextsChangeListener() {
        mBinding.apply {
            etHeartRate.mBinding.editText.doAfterTextChanged { validateSendToPatientButton() }
            etTemperature.mBinding.editText.doAfterTextChanged { validateSendToPatientButton() }
            etSystolicBP.mBinding.editText.doAfterTextChanged { validateSendToPatientButton() }
            etDiastolicBP.mBinding.editText.doAfterTextChanged { validateSendToPatientButton() }
            etOxygenLevel.mBinding.editText.doAfterTextChanged { validateSendToPatientButton() }
            etBloodSugar.mBinding.editText.doAfterTextChanged { validateSendToPatientButton() }
        }
    }

    private fun validateSendToPatientButton() {
        mBinding.apply {
            val heartRate = etHeartRate.text
            val temperature = etTemperature.text
            val systolicBP = etSystolicBP.text
            val diastolicBP = etDiastolicBP.text
            val oxygenLevel = etOxygenLevel.text
            val bloodSugar = etBloodSugar.text

            val error = ((heartRate.isNotEmpty() && heartRate.toFloatOrDefault() == 0f) || heartRate.isEmpty()) &&
                    ((temperature.isNotEmpty() && temperature.toFloatOrDefault() == 0f)|| temperature.isEmpty()) &&
                    ((systolicBP.isNotEmpty() && systolicBP.toFloatOrDefault() == 0f)|| systolicBP.isEmpty()) &&
                    ((diastolicBP.isNotEmpty() && diastolicBP.toFloatOrDefault() == 0f)|| diastolicBP.isEmpty()) &&
                    ((oxygenLevel.isNotEmpty() && oxygenLevel.toFloatOrDefault() == 0f)|| oxygenLevel.isEmpty()) &&
                    ((bloodSugar.isNotEmpty() && bloodSugar.toFloatOrDefault() == 0f) || bloodSugar.isEmpty())

            bSaveRecord.isEnabled = error.not()
        }
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
        mBinding.apply {
            bSaveRecord.isEnabled = isValid(etRecordDate.text) && (
                    isValid(etHeartRate.text)
                            || isValid(etHeartRate.text)
                            || isValid(etTemperature.text)
                            || isValid(etSystolicBP.text)
                            || isValid(etDiastolicBP.text)
                            || isValid(etOxygenLevel.text)
                            || isValid(etBloodSugar.text)
                    )
        }
    }

    private fun setupViews(){
        mBinding.apply {
            if(arguments?.containsKey(getString(R.string.modify)).getSafe()){
                isModify = true
                actionbar.action2Res = R.drawable.ic_delete_white
            }

            llSharedWithMain.gone()
            sharedDivider1.gone()
        }
    }

    private fun setUserDetails(){
        mBinding.apply {
            emrViewModel.selectedFamily?.let {
                iDoctor.tvTitle.text = it.fullName
                iDoctor.tvDesc.text = getCurrentDateTime("dd MMMM, yy")
                iDoctor.ivThumbnail.loadImage(it.profilePicture, getGenderIcon(it.genderId))
            } ?: kotlin.run {
                val user = DataCenter.getUser()
                iDoctor.tvTitle.text = user?.fullName
                iDoctor.tvDesc.text = getCurrentDateTime("dd MMMM, yy")
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

            details?.let {
                iDoctor.apply {
                    iDoctor.tvDesc.text = getDateInFormat(
                        details.date.getSafe(),
                        "yyyy-MM-dd",
                        "dd MMMM yyyy"
                    )

                    if(details.serviceTypeId.isNullOrEmpty() || details.serviceTypeId == "0"){
                        setUserDetails()
                    }
                    else {
                        tvTitle.text = details.partnerName.toString()
                        ivIcon.setImageResource(CustomServiceTypeView.ServiceType.getServiceById(details.serviceTypeId.getSafe().toInt())?.icon.getSafe())
                        ivThumbnail.invisible()
                    }
                }

                it.vitals?.forEach { vital ->
                    val editText: CustomDefaultEditText = mBinding.root.findViewWithTag(vital.key)

                    editText.text = vital.value.getSafe()
                }
                if(it.originalDate!=null && isModify) {
                    var dateFormat = "dd/MM/yyyy || $timeFormat"
                    etRecordDate.text =
                        getDateInFormat(it.originalDate.getSafe(), "yyyy-MM-dd HH:mm", dateFormat)
                }

                if (isModify){
                    val hash = "\u0023"
                    actionbar.title = "${lang?.emrScreens?.modifyText.getSafe()} ${lang?.emrScreens?.record?.lowercase()} $hash ${it.emrNumber}"
                }
            }
        }

        validate()
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
                        findNavController().popBackStack(R.id.customerVitalsRecordDetailsFragment, true)
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