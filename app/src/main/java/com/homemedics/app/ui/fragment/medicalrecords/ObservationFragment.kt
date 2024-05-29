package com.homemedics.app.ui.fragment.medicalrecords

import android.app.AlertDialog
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.emr.StoreEMRRequest
import com.fatron.network_module.models.request.emr.Vital
import com.fatron.network_module.models.request.emr.type.EMRDetailsRequest
import com.fatron.network_module.models.request.emr.type.EMRTypeDeleteRequest
import com.fatron.network_module.models.request.emr.type.StoreEMRTypeRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.type.EMRDetailsResponse
import com.fatron.network_module.models.response.emr.type.EmrVital
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.DialogAddEmrBinding
import com.homemedics.app.databinding.FragmentObservationBinding
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel


class ObservationFragment : BaseFragment(), View.OnClickListener {

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentObservationBinding

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            etHeartRate.hint = lang?.emrScreens?.heartRate.getSafe()
            etTemperature.hint = lang?.emrScreens?.temperature.getSafe()
            etSystolicBP.hint = lang?.emrScreens?.systolicBp.getSafe()
            etDiastolicBP.hint = lang?.emrScreens?.diastolicBp.getSafe()
            etOxygenLevel.hint = lang?.emrScreens?.oxygenLevel.getSafe()
            etBloodSugar.hint = lang?.emrScreens?.bloodSugarLevel.getSafe()
            caSymptoms.apply {
                title = lang?.emrScreens?.symptoms.getSafe()
                custDesc = lang?.emrScreens?.symptomsDescription.getSafe()
            }
        }
    }

    override fun init() {
        addSuffixIntoFields()

        emrViewModel.emrId.observe(this) {
            it ?: return@observe
            emrViewModel.emrID = it
            getEmrDetails(it)
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_observation

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentObservationBinding
    }

    override fun setListeners() {

        mBinding.apply {
            caSymptoms.apply {
                onAddItemClick = {
                    findNavController().safeNavigate(
                        MedicalRecordsFragmentDirections.actionMedicalRecordsFragmentToSymptomsFragment()
                    )
                }
                onEditItemCall = {
                    showAddEmrBindingDialog(it)
                }
                onDeleteClick = { item, _ ->
                    val type = metaData?.emrTypes?.find { it.genericItemId == Enums.EMRTypesMeta.SYMPTOMS.key }?.genericItemId
                    val request = EMRTypeDeleteRequest(emrTypeId = item.itemId?.toInt(), type = type)
                    DialogUtils(requireActivity()).showDoubleButtonsAlertDialog(
                        title = lang?.dialogsStrings?.confirmDelete.getSafe(),
                        message = lang?.dialogsStrings?.deleteDesc.getSafe(),
                        positiveButtonStringText = lang?.globalString?.yes.getSafe(),
                        negativeButtonStringText = lang?.globalString?.no.getSafe(),
                        buttonCallback = {
                            deleteEMRType(request)
                        }
                    )
                }
            }
            bSaveDraft.setOnClickListener(this@ObservationFragment)
            bSendToPatient.setOnClickListener(this@ObservationFragment)
            editTextsChangeListener()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSaveDraft -> {
                storeApiCall(1)
            }
            R.id.bSendToPatient -> {
                storeApiCall(0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
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
                emrViewModel.systolic = systolic
                emrViewModel.diastolic = diastolic
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
        if (vitals.isNotEmpty()) {
            emrViewModel.vitals = vitals
        } else {
            emrViewModel.vitals = arrayListOf()
        }
    }

    private fun storeApiCall(isDrafted: Int) {
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
                            title = mBinding.lang?.labPharmacyScreen?.alert.getSafe(),
                            message = mBinding.lang?.labPharmacyScreen?.systolic.getSafe(),
                        )
                    return
                } else if (diastolic.isEmpty()) {
                    DialogUtils(requireActivity())
                        .showSingleButtonAlertDialog(
                            title = mBinding.lang?.labPharmacyScreen?.alert.getSafe(),
                            message = mBinding.lang?.labPharmacyScreen?.diastolic.getSafe(),
                        )
                    return
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

        val request = StoreEMRRequest(
            bookingId = emrViewModel.bookingId.toString(),
            customerId = emrViewModel.customerId,
            emrId = emrViewModel.emrID.toString(),
            isDraft = isDrafted,
            emrChat = if (emrViewModel.emrChat) 1 else 0,
            vitals = vitals
        )
        storeEMR(request)
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

            val error = ((heartRate.isNotEmpty() && heartRate.toFloat() == 0f) || heartRate.isEmpty()) &&
                    ((temperature.isNotEmpty() && temperature.toFloat() == 0f)|| temperature.isEmpty()) &&
                    ((systolicBP.isNotEmpty() && systolicBP.toFloat() == 0f)|| systolicBP.isEmpty()) &&
                    ((diastolicBP.isNotEmpty() && diastolicBP.toFloat() == 0f)|| diastolicBP.isEmpty()) &&
                    ((oxygenLevel.isNotEmpty() && oxygenLevel.toFloat() == 0f)|| oxygenLevel.isEmpty()) &&
                    ((bloodSugar.isNotEmpty() && bloodSugar.toFloat() == 0f) || bloodSugar.isEmpty())

            bSendToPatient.isEnabled = error.not()
            bSaveDraft.isEnabled = error.not()
        }
    }

    private fun showAddEmrBindingDialog(item: MultipleViewItem? = null) {
        val dialog: AlertDialog
        val emrDialogBuilder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme).apply {
            val addEmrBinding = DialogAddEmrBinding.inflate(layoutInflater)
            if (item?.title != null || item?.desc != null) {
                addEmrBinding.apply {
                    etTitle.apply {
                        text = item.title.getSafe()
                        mBinding.editText.apply {
                            isEnabled = false
                            isClickable = false
                            setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                        }
                    }
                    etDescription.setText(item.desc.getSafe())
                }
            }
            setView(addEmrBinding.root)
            setCancelable(false)
            setTitle(mBinding.lang?.emrScreens?.editSymptoms.getSafe())
            setPositiveButton(mBinding.lang?.globalString?.add.getSafe()) { _, _ ->
                closeKeypad()
                val type = metaData?.emrTypes?.find { it.genericItemId == Enums.EMRTypesMeta.SYMPTOMS.key }?.genericItemId

                val name = addEmrBinding.etTitle.text
                val description = addEmrBinding.etDescription.text

                val storeEMRRequest = StoreEMRTypeRequest(
                    description = description.toString(),
                    name = name,
                    type = type.toString(),
                    emrTypeId = item?.itemId
                )
                editEMRType(storeEMRRequest)
                this.create().dismiss()
            }
            setNegativeButton(mBinding.lang?.globalString?.cancel.getSafe(), null)
        }

        dialog = emrDialogBuilder.create()
        dialog.show()
    }

    private fun getEmrDetails(emrId: Int? = null) {
        val emrDetailsRequest = EMRDetailsRequest(emrId = emrId)
        if (isOnline(requireActivity())) {
            emrViewModel.emrDetails(emrDetailsRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<EMRDetailsResponse>
                        response.data?.let { emrDetailsResponse ->
                            emrViewModel.emrDetails = emrDetailsResponse.emrDetails
                            mBinding.apply {
                                caSymptoms.listItems = ((emrDetailsResponse.emrDetails?.emrSymptoms as ArrayList<MultipleViewItem?>?)?.map {
                                    it?.hasRoundLargeIcon = false
                                    it?.hasSecondAction = true
                                    it
                                } as ArrayList<MultipleViewItem>?) ?: arrayListOf()

                                bSendToPatient.isEnabled = emrDetailsResponse.emrDetails?.emrSymptoms?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrVitals?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrDiagnosis?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrProducts?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrMedicalHealthcares?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrLabTests?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrAttachments?.isNotEmpty().getSafe()

                                bSaveDraft.isEnabled = emrDetailsResponse.emrDetails?.emrSymptoms?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrVitals?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrDiagnosis?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrProducts?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrMedicalHealthcares?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrLabTests?.isNotEmpty().getSafe() ||
                                        emrDetailsResponse.emrDetails?.emrAttachments?.isNotEmpty().getSafe()
                            }
                            if (emrDetailsResponse.emrDetails?.emrVitals?.isNotEmpty().getSafe()) {
                                setVitals(emrDetailsResponse.emrDetails?.emrVitals)
                            }
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
                    message = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun setVitals(emrVitals: List<EmrVital>?) {
        mBinding.apply {
            etHeartRate.text = emrVitals?.find { it.unitId == Enums.EMRVitalsUnits.HEART_RATE.key }?.value.getSafe()
            etTemperature.text = emrVitals?.find { it.unitId == Enums.EMRVitalsUnits.TEMPERATURE.key }?.value.getSafe()
            etSystolicBP.text = emrVitals?.find { it.unitId == Enums.EMRVitalsUnits.SYSTOLIC_DIASTOLIC.key }?.value.getSafe()
            etDiastolicBP.text = emrVitals?.find { it.key == Enums.EMRVitalsUnits.DIASTOLIC.label }?.value.getSafe()
            etOxygenLevel.text = emrVitals?.find { it.unitId == Enums.EMRVitalsUnits.OXYGEN_LEVEL.key }?.value.getSafe()
            etBloodSugar.text = emrVitals?.find { it.unitId == Enums.EMRVitalsUnits.BLOOD_SUGAR.key }?.value.getSafe()
        }
    }

    private fun editEMRType(storeEMRTypeRequest: StoreEMRTypeRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.editEMRType(storeEMRTypeRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        getEmrDetails(emrViewModel.emrID)
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
                    message = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun deleteEMRType(request: EMRTypeDeleteRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.deleteEMRType(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        getEmrDetails(emrViewModel.emrID)
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
                    message = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    buttonCallback = {},
                )
        }
    }

    private fun storeEMR(request: StoreEMRRequest) {
        if (isOnline(requireActivity())) {
            emrViewModel.storeEMR(request).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        mBinding.apply {
                            etHeartRate.text = ""
                            etTemperature.text = ""
                            etSystolicBP.text = ""
                            etDiastolicBP.text = ""
                            etOxygenLevel.text = ""
                            etBloodSugar.text = ""
                        }
                        getEmrDetails(emrViewModel.emrID)

                        if(requireActivity() is CallActivity) {
                            (requireActivity() as CallActivity).goBackDuringCall()
                        } else {
                            emrViewModel.docEmr.value=true
                            findNavController().popBackStack(R.id.medical_records_navigation, true)
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
                    message = mBinding.lang?.errorMessages?.internetError.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}