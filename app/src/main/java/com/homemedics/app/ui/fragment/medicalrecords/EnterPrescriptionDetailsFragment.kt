package com.homemedics.app.ui.fragment.medicalrecords

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentPrescriptionEnterDetailsBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class EnterPrescriptionDetailsFragment : BaseFragment(), View.OnClickListener {

    private val emrViewModel: EMRViewModel by activityViewModels()

    private lateinit var mBinding: FragmentPrescriptionEnterDetailsBinding

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.emrScreens?.enterDetails.getSafe()
            etName.hint = lang?.globalString?.name.getSafe()
            textInputLayout.hint = lang?.emrScreens?.descriptions.getSafe()
            etNumberOfDays.hint = lang?.emrScreens?.noOfDays.getSafe()
            tMorning.hint = lang?.emrScreens?.morning.getSafe()
            tAfternoon.hint = lang?.emrScreens?.afternoon.getSafe()
            tNight.hint = lang?.emrScreens?.evening.getSafe()
            etHourlyDosage.hint = lang?.emrScreens?.hours.getSafe()
            etDosageQuantity.hint = lang?.emrScreens?.dosageQuantity.getSafe()
            etSpecialInstructions.hint = lang?.emrScreens?.medicinesInstructions.getSafe()
        }
    }

    override fun init() {
        emrViewModel.storeMedicineRequest.dosageType =
            if(mBinding.rbDaily.isChecked)
                Enums.DosageType.DAILY.key.toString()
            else
                Enums.DosageType.HOURLY.key.toString()

        setMedicinesDetail()
    }

    override fun getFragmentLayout() = R.layout.fragment_prescription_enter_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentPrescriptionEnterDetailsBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }
            bSave.setOnClickListener(this@EnterPrescriptionDetailsFragment)
        }
        radioButtonListener()
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.bSave -> {
                medicineValidationsApiCall()
            }
        }
    }

    private var dosagelist: List<GenericItem>? = null

    private fun setMedicinesDetail() {
        dosagelist = metaData?.dosageQuantity
        val dosageList = dosagelist?.map { it.genericItemName }

        if (dosageList?.size.getSafe() > 0) {
            mBinding.etHourlyDosage.data = dosageList as ArrayList<String>

            if (emrViewModel.dosageId != 0) {
                val index = dosagelist?.indexOfFirst { it.genericItemId == emrViewModel.dosageId }
                if (index != -1)
                    mBinding.etHourlyDosage.selectionIndex = index.getSafe()
            }

            mBinding.etHourlyDosage.onItemSelectedListener = { _, position: Int ->
                emrViewModel.dosageId = dosagelist?.get(position)?.genericItemId.getSafe()
                mBinding.etHourlyDosage.clearFocus()
            }
        }

        if (emrViewModel.isEdit) {
            mBinding.apply {
                emrViewModel.medicineProduct?.apply {
                    etName.apply {
                        text = name.getSafe()
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
                    etDescription.setText(description.getSafe())
                    etNumberOfDays.text = noOfDays.getSafe()
                    etMorning.setText(dosage?.morning.getSafe())
                    etAfternoon.setText(dosage?.afternoon.getSafe())
                    etEvening.setText(dosage?.evening.getSafe())
                    etHourlyDosage.data = dosageList as ArrayList<String>
                    etDosageQuantity.text = dosageQuantity.getSafe()
                    etSpecialInstructions.setText(specialInstruction.getSafe())
                }
            }
        } else {
            mBinding.apply {
                etName.text = emrViewModel.medicine.genericItemName.getSafe()
                etDescription.setText(emrViewModel.medicine.description.getSafe())
            }
        }

        if (emrViewModel.medicineProduct?.dosage?.hourly != null) {
            mBinding.rbHourly.isChecked = true
            medicinesDosageToggle(true)
        }
    }

    private fun radioButtonListener() {
        mBinding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.rbDaily -> {
                    emrViewModel.storeMedicineRequest.apply {
                        dosageType = Enums.DosageType.DAILY.key.toString()
                        hourly = null
                    }
                    medicinesDosageToggle(false)
                }
                R.id.rbHourly -> {
                    emrViewModel.storeMedicineRequest.apply {
                        dosageType = Enums.DosageType.HOURLY.key.toString()
                        morning = null
                        afternoon = null
                        evening = null
                    }
                    medicinesDosageToggle(true)
                }
            }
        }
    }

    private fun medicinesDosageToggle(isShow: Boolean) {
        mBinding.apply {
            tvDosage.text = if (isShow.not())
                lang?.emrScreens?.dosageDayWise?.getSafe()
            else
                lang?.emrScreens?.dosageHourly?.getSafe()

            llDosage.setVisible(isShow.not())
            etHourlyDosage.setVisible(isShow)
            etDosageQuantity.setVisible(isShow)
        }
    }

    private fun medicineValidationsApiCall() {
        val etName = mBinding.etName.text
        val noOfDay = mBinding.etNumberOfDays.text
        val daily = mBinding.rbDaily.isChecked
        val hourlyDosage = mBinding.rbHourly.isChecked
        val etMorning = mBinding.etMorning.text.toString()
        val etAfternoon = mBinding.etAfternoon.text.toString()
        val etEvening = mBinding.etEvening.text.toString()
        val etDescription = mBinding.etDescription.text.toString()
        var specialInstructions: String? = mBinding.etSpecialInstructions.text.toString()
        val etHourly = emrViewModel.dosageId
        val quantity = mBinding.etDosageQuantity.text

        if (isValid(etName).not()) {
            mBinding.etName.errorText = mBinding.lang?.fieldValidationStrings?.nameEmpty.getSafe()
            return
        }
        if (isValid(noOfDay).not() || (noOfDay.isEmpty().not() && noOfDay.toInt() == 0)) {
            mBinding.etNumberOfDays.errorText = mBinding.lang?.fieldValidationStrings?.numberEmpty.getSafe()
            return
        }
        if (mBinding.rbHourly.isChecked) {
            if (emrViewModel.dosageId == 0) {
                mBinding.etHourlyDosage.errorText = mBinding.lang?.fieldValidationStrings?.hourlyEmpty.getSafe()
                return
            }
            if (isValid(quantity).not()) {
                mBinding.etDosageQuantity.errorText = mBinding.lang?.fieldValidationStrings?.dosageQuantityEmpty.getSafe()
                return
            }
            if (quantity == "0") {
                mBinding.etDosageQuantity.errorText = mBinding.lang?.fieldValidationStrings?.dosageQuantityZero.getSafe()
                return
            }
        }
        if (isValid(specialInstructions).not()) {
            specialInstructions = null
        }
        if (daily) {
            if ((etMorning.isEmpty() || etMorning == "0") && (etAfternoon.isEmpty() || etAfternoon == "0") && (etEvening.isEmpty() || etEvening == "0")) {
                DialogUtils(requireActivity())
                    .showSingleButtonAlertDialog(
                        title = mBinding.lang?.globalString?.warning.getSafe(),
                        message = mBinding.lang?.dialogsStrings?.pleaseAddDosage.getSafe(),
                        buttonCallback = {},
                    )
                return
            }
        }

        if (emrViewModel.isEdit) {
            emrViewModel.storeMedicineRequest.apply {
                emrId = null
                productId = null
                emrProductId = emrViewModel.medicineProduct?.id
                name = etName
                description = etDescription
                specialInstruction = specialInstructions
                noOfDays = noOfDay
                morning = etMorning
                afternoon = etAfternoon
                evening = etEvening
                if (hourlyDosage) {
                    hourly = etHourly.toString()
                    dosageQuantity = quantity.toFloat()
                    morning = null
                    afternoon = null
                    evening = null
                } else {
                    hourly = null
                }
            }
            editMedicineApi()
        } else {
            emrViewModel.storeMedicineRequest.apply {
                emrId = emrViewModel.emrID
                productId = emrViewModel.medicine.genericItemId.getSafe().toString()
                emrProductId = null
                name = etName
                description = etDescription
                specialInstruction = specialInstructions
                noOfDays = noOfDay
                morning = etMorning.ifEmpty { "0" }
                afternoon = etAfternoon.ifEmpty { "0" }
                evening = etEvening.ifEmpty { "0" }
                if (hourlyDosage) {
                    hourly = etHourly.toString()
                    dosageQuantity = quantity.toFloat()
                    morning = null
                    afternoon = null
                    evening = null
                } else {
                    hourly = null
                }
            }
            storeMedicineApi()
        }
    }

    private fun storeMedicineApi() {
        if (isOnline(requireActivity())) {
            emrViewModel.storeMedicine().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        emrViewModel.isDraft = false
                        findNavController().popBackStack(R.id.addMedicinesFragment, true)
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

    private fun editMedicineApi() {
        if (isOnline(requireActivity())) {
            emrViewModel.editMedicine(emrViewModel.storeMedicineRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<*>
                        if (response.status in 200..299) {
                            findNavController().popBackStack()
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