package com.homemedics.app.ui.fragment.medicalrecords.patient.medications

import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.medicine.StoreMedicineRequest
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentAddMedicineDetailsBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class AddMedicineDetailsFragment : BaseFragment() {

    private lateinit var mBinding: FragmentAddMedicineDetailsBinding
    private val emrViewModel: EMRViewModel by activityViewModels()
    private lateinit var hourlyDosageList: ArrayList<GenericItem>

    override fun onDetach() {
        super.onDetach()
        emrViewModel.storeMedicineRequest = StoreMedicineRequest()
        emrViewModel.medicineToModify = null
    }

    override fun setLanguageData() {
        mBinding.apply {
            langData = ApplicationClass.mGlobalData
            actionbar.title = langData?.emrScreens?.addDetails.getSafe()
            etNumberOfDays.hint = langData?.emrScreens?.numberOfDays.getSafe()
            tMorning.hint = langData?.emrScreens?.morning.getSafe()
            tAfternoon.hint = langData?.emrScreens?.afternoon.getSafe()
            tNight.hint = langData?.emrScreens?.evening.getSafe()
            etHourlyDosage.hint = langData?.emrScreens?.hours.getSafe()
            etDosageQuantity.hint = langData?.emrScreens?.dosageQuantity.getSafe()
            etSpecialInstructions.hint = langData?.emrScreens?.medicinesInstructions.getSafe()
        }
    }

    override fun init() {
        emrViewModel.storeMedicineRequest.modify = (emrViewModel.medicineToModify != null).getInt()
        if(emrViewModel.storeMedicineRequest.modify.getBoolean()){
            emrViewModel.storeMedicineRequest.emrItemId = emrViewModel.medicineToModify?.id
        }

        hourlyDosageList = metaData?.dosageQuantity as ArrayList<GenericItem>
        setDataInViews()
    }

    override fun getFragmentLayout() = R.layout.fragment_add_medicine_details

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentAddMedicineDetailsBinding
        emrViewModel.storeMedicineRequest.dosageType = if(mBinding.rbDaily.isChecked) Enums.DosageType.DAILY.key.toString() else Enums.DosageType.HOURLY.key.toString()
        emrViewModel.storeMedicineRequest.apply {
            morning = "0"
            afternoon = "0"
            evening = "0"
        }
        mBinding.dataManager = emrViewModel.storeMedicineRequest
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().navigateUp()
            }
            bAdd.setOnClickListener {
                emrViewModel.storeMedicineRequest.apply {
                    emrId = emrViewModel.emrID
                    type = emrViewModel.selectedEMRType?.key
                    emrTypeId = emrViewModel.selectedRecord?.genericItemId
                    name = emrViewModel.selectedRecord?.genericItemName
                    description = emrViewModel.selectedRecord?.description
                }

                addRecordApi()
            }

            etNumberOfDays.mBinding.editText.doAfterTextChanged {
                validate()
            }
            etMorning.doAfterTextChanged {
                validate()
            }
            etAfternoon.doAfterTextChanged {
                validate()
            }
            etEvening.doAfterTextChanged {
                validate()
            }
            etDosageQuantity.mBinding.editText.doAfterTextChanged {
                if(it.isNullOrEmpty().not()  && mBinding.etDosageQuantity.text.equals(".").not() ){
                    emrViewModel.storeMedicineRequest.dosageQuantity = mBinding.etDosageQuantity.text.toFloat()
                }
                else {
                    emrViewModel.storeMedicineRequest.dosageQuantity = null
                }

                validate()
            }

            etHourlyDosage.onItemSelectedListener = { item, pos ->
                emrViewModel.storeMedicineRequest.hourly = hourlyDosageList[pos].genericItemId.toString()
                validate()
            }
        }

        radioButtonListener()
    }

    private fun setDataInViews(){
        mBinding.apply {
            if(emrViewModel.medicineToModify != null){ //added medicine modification work start
                emrViewModel.medicineToModify?.let {
                    emrViewModel.selectedRecord = GenericItem(
                        genericItemId = it.productId,
                        genericItemName = it.name,
                        description =  it.description,
                        icon_url = it.imageUrl
                    )

                    rbHourly.isChecked = it.dosageQuantity.isNullOrEmpty().not()
                    etHourlyDosage.mBinding.dropdownMenu.setText(hourlyDosageList.find { item -> item.genericItemId ==  it.dosage?.hourly?.toInt()}?.genericItemName.getSafe())
                    etDosageQuantity.text = it.dosageQuantity.getSafe()

                    emrViewModel.storeMedicineRequest.apply {
                        this.emrId = it.emrId
                        this.emrTypeId = it.productId
                        this.emrProductId = it.productId
                        this.date = ""
                        this.dosageQuantity = if(it.dosageQuantity.isNullOrEmpty()) null else it.dosageQuantity?.toFloat()
                        this.name = it.name
                        this.type = emrViewModel.selectedEMRType?.key
                        this.description = it.description
                        this.dosageType = if(rbDaily.isChecked) Enums.DosageType.DAILY.key.toString() else Enums.DosageType.HOURLY.key.toString()
                        this.specialInstruction = it.specialInstruction
                        this.noOfDays = it.noOfDays
                        this.hourly = it.dosage?.hourly
                        this.morning = it.dosage?.morning
                        this.afternoon = it.dosage?.afternoon
                        this.evening = it.dosage?.evening
                    }

                    medicinesDosageToggle(it.dosageQuantity.isNullOrEmpty().not())
                    dataManager = emrViewModel.storeMedicineRequest
                }
            }
            //added medicine modification work end

            iDoctor.tvTitle.text = emrViewModel.selectedRecord?.title
            iDoctor.tvDesc.text = emrViewModel.selectedRecord?.description
            iDoctor.ivIcon.loadImage(emrViewModel.selectedRecord?.icon_url, R.drawable.ic_medication)
            iDoctor.ivThumbnail.invisible()

            etHourlyDosage.data = hourlyDosageList.map { it.genericItemName } as ArrayList<String>
        }
    }

    private fun radioButtonListener() {
        mBinding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.rbDaily -> {
                    medicinesDosageToggle(false)
                    emrViewModel.storeMedicineRequest.dosageQuantity = null
                    emrViewModel.storeMedicineRequest.hourly = null
                    emrViewModel.storeMedicineRequest.dosageType = Enums.DosageType.DAILY.key.toString()
                }
                R.id.rbHourly -> {
                    medicinesDosageToggle(true)
                    emrViewModel.storeMedicineRequest.dosageType = Enums.DosageType.HOURLY.key.toString()
                }
            }

            validate()
        }
    }

    private fun medicinesDosageToggle(isShow: Boolean) {
        mBinding.apply {
            tvDosage.text = if (isShow.not())
                langData?.emrScreens?.dosageDayWise.getSafe()
            else
                langData?.emrScreens?.dosageHourly.getSafe()

            llDosage.setVisible(isShow.not())
            etHourlyDosage.setVisible(isShow)
            etDosageQuantity.setVisible(isShow)
        }
    }

    private fun validate(): Boolean {
        var error = false
        emrViewModel.storeMedicineRequest.apply {
            if(isValid(noOfDays).not())
                error = true
            else if(noOfDays.isNullOrEmpty().not() && noOfDays?.toInt() == 0)
                error = true
            else if(mBinding.rbHourly.isChecked){
                error = isValid(mBinding.etDosageQuantity.text).not() || (emrViewModel.storeMedicineRequest.hourly == null)
            }
            else {
                error = (morning.isNullOrEmpty() && afternoon.isNullOrEmpty() && evening.isNullOrEmpty())
                        || (
                            (morning?.isEmpty().getSafe() || morning == "0")
                                    && (afternoon?.isEmpty().getSafe() || afternoon == "0")
                                    && (evening?.isEmpty().getSafe() || evening == "0")
                        )
            }

            mBinding.bAdd.isEnabled = error.not()
        }

        return error
    }

    private fun addRecordApi() {
        if (isOnline(requireActivity())) {
            emrViewModel.addCustomerMedicineRecord().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        if(findNavController().popBackStack(R.id.selectMedicineFragment, true).not()){ //coming from edit medicine
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
                    title = mBinding.langData?.errorMessages?.internetError.getSafe(),
                    message = mBinding.langData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {},
                )
        }
    }
}