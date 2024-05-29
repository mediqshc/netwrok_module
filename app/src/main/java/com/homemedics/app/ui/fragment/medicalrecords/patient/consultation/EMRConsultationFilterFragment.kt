package com.homemedics.app.ui.fragment.medicalrecords.patient.consultation

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.customer.consultation.EMRConsultationFilterRequest
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.type.GenericEMRListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentEmrConsultationFilterBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class EMRConsultationFilterFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentEmrConsultationFilterBinding
    private val emrViewModel: EMRViewModel by activityViewModels()
    private var consultationFilterRequest = EMRConsultationFilterRequest()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            actionbar.title = lang?.globalString?.filter.getSafe()
            etStartDate.hint = lang?.globalString?.startDate.getSafe()
            etEndDate.hint = lang?.globalString?.endDate.getSafe()
            cdServiceType.hint = lang?.emrScreens?.serviceType.getSafe()
            cdDiagnosis.hint = lang?.emrScreens?.diagnosis.getSafe()
            cdSymptoms.hint = lang?.emrScreens?.symptoms.getSafe()
            cdSpeciality.hint = lang?.globalString?.speciality.getSafe()
            layoutButtons.apply {
                bClearFilter.text = lang?.globalString?.clearFilter.getSafe()
                bSave.text = lang?.globalString?.applyFilter.getSafe()
            }
        }
    }

    override fun init() {
        consultationFilterRequest = Gson().fromJson(
            Gson().toJson(emrViewModel.consultationFilterRequest),
            EMRConsultationFilterRequest::class.java
        )
        observe()

        if(emrViewModel.symptomList.isEmpty())
            getSymptoms()
        if(emrViewModel.diagnosisList.isEmpty())
            getDiagnosisList()

        initDropDowns()
    }

    private fun initDropDowns(){
        mBinding.apply {

            consultationFilterRequest.startDate?.let {
                etStartDate.text = getDateInFormat(it, "yyyy-MM-dd", "dd/MM/yyyy")
            }

            consultationFilterRequest.endDate?.let {
                etEndDate.text = getDateInFormat(it, "yyyy-MM-dd", "dd/MM/yyyy")
            }

            cbSharedRecords.isChecked = consultationFilterRequest.sharedRecord.getBoolean()

            var selectionIndex = 0

            //services

            val services = metaData?.partnerServiceTypeForCMR
            var serviceIndex = -1
            if(consultationFilterRequest.serviceType != null){
                serviceIndex = services?.indexOfFirst { it.id == consultationFilterRequest.specialityId.getSafe() }.getSafe()
            }
            val servicesNames = services?.map { it.name }
            cdServiceType.data = servicesNames as ArrayList<String>
            if(serviceIndex > -1){
                cdServiceType.selectionIndex = serviceIndex
                val service = services[serviceIndex]
                consultationFilterRequest.serviceType = service.id.getSafe()
                consultationFilterRequest.serviceName = service.name.getSafe()
            }
            cdServiceType.onItemSelectedListener = { _, position: Int ->
                val service = services[position]
                consultationFilterRequest.serviceType = service.id.getSafe()
                consultationFilterRequest.serviceName = service.name.getSafe()
            }

            //specialities

            val specialities = metaData?.specialties?.doctorSpecialties
            var specialityIndex = -1
            if(consultationFilterRequest.specialityId != null){
                specialityIndex = specialities?.indexOfFirst { it.genericItemId == consultationFilterRequest.specialityId.getSafe() }.getSafe()
            }
            val specialityNames = specialities?.map { it.genericItemName }
            cdSpeciality.data = specialityNames as ArrayList<String>
            if(specialityIndex > -1){
                cdSpeciality.selectionIndex = specialityIndex
                val speciality = specialities[specialityIndex]
                consultationFilterRequest.specialityId = speciality.genericItemId.getSafe()
            }
            cdSpeciality.onItemSelectedListener = { _, position: Int ->
                val speciality = specialities[position]
                consultationFilterRequest.specialityId = speciality.genericItemId.getSafe()
            }

            //symptoms

            val symptons = emrViewModel.symptomList
            var symptonIndex = -1
            if(consultationFilterRequest.symptomId != null){
                symptonIndex = symptons.indexOfFirst { it.genericItemId == consultationFilterRequest.symptomId.getSafe() }.getSafe()
            }
            selectionIndex = symptonIndex
            val symptonNames = symptons.map { it.genericItemName }
            cdSymptoms.data = symptonNames as ArrayList<String>
            if(selectionIndex > -1){
                cdSymptoms.selectionIndex = selectionIndex
                val sympton = symptons[selectionIndex]
                consultationFilterRequest.symptomId = sympton.genericItemId.getSafe()
            }
            cdSymptoms.onItemSelectedListener = { _, position: Int ->
                val sympton = symptons[position]
                consultationFilterRequest.symptomId = sympton.genericItemId.getSafe()
            }


            //diagnosis

           val diagnosis = emrViewModel.diagnosisList
            var diagnosisIndex = -1
            if(consultationFilterRequest.diagnosisId != null){
                diagnosisIndex = diagnosis.indexOfFirst { it.genericItemId == consultationFilterRequest.diagnosisId.getSafe() }.getSafe()
            }
            selectionIndex = diagnosisIndex
            val diagnosisNames = diagnosis.map { it.genericItemName }
            cdDiagnosis.data = diagnosisNames as ArrayList<String>
            if(selectionIndex > -1){
                cdDiagnosis.selectionIndex = selectionIndex
                val diagnose = diagnosis[selectionIndex]
                consultationFilterRequest.diagnosisId = diagnose.genericItemId.getSafe()
            }
            cdDiagnosis.onItemSelectedListener = { _, position: Int ->
                val diagnose = diagnosis[position]
                consultationFilterRequest.diagnosisId = diagnose.genericItemId.getSafe()
            }
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_emr_consultation_filter

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentEmrConsultationFilterBinding
    }

    override fun setListeners() {
        mBinding.apply {
            actionbar.onAction2Click = {
                findNavController().popBackStack()
            }

            etStartDate.clickCallback = {
                openCalender(mBinding.etStartDate.mBinding.editText)
            }

            etEndDate.clickCallback = {
                openCalender(mBinding.etEndDate.mBinding.editText)
            }

            layoutButtons.bSave.setOnClickListener {
                if(etStartDate.text.isNotEmpty())
                    consultationFilterRequest.startDate = getDateInFormat(etStartDate.text, "dd/MM/yyyy", "yyyy-MM-dd")

                if(etEndDate.text.isNotEmpty())
                    consultationFilterRequest.endDate = getDateInFormat(etEndDate.text, "dd/MM/yyyy", "yyyy-MM-dd")

                consultationFilterRequest.sharedRecord = cbSharedRecords.isChecked.getInt()

                emrViewModel.consultationFilterRequest = consultationFilterRequest
                findNavController().popBackStack()
            }

            layoutButtons.bClearFilter.setOnClickListener {
                emrViewModel.consultationFilterRequest = EMRConsultationFilterRequest()
                consultationFilterRequest = emrViewModel.consultationFilterRequest

                etStartDate.text = ""
                etEndDate.text = ""

                cdServiceType.data = arrayListOf()
                cdDiagnosis.data = arrayListOf()
                cdSymptoms.data = arrayListOf()
                cdSpeciality.data = arrayListOf()

                cdSpeciality.mBinding.dropdownMenu.setText("")
                cdDiagnosis.mBinding.dropdownMenu.setText("")
                cdSymptoms.mBinding.dropdownMenu.setText("")
                cdSpeciality.mBinding.dropdownMenu.setText("")

                cbSharedRecords.isChecked = false

                llContainer.requestFocus()

                initDropDowns()
            }
        }
    }

    override fun onClick(v: View?) {

    }

    private fun observe() {

    }

    private fun getSymptoms() {
        val pageRequest = PageRequest()
        if (isOnline(requireActivity())) {
            emrViewModel.getSymptoms(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<GenericEMRListResponse>
                        response.data?.let {
                            val tempList = it.symptoms?.data as ArrayList<GenericItem>
                            emrViewModel.symptomList = tempList.filter { it.genericItemName != null } as ArrayList<GenericItem>
                            initDropDowns()
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

    private fun getDiagnosisList() {
        val pageRequest = PageRequest()
        if (isOnline(requireActivity())) {
            emrViewModel.getDiagnosisList(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<GenericEMRListResponse>
                        response.data?.let {
                            val tempList = it.diagnosis?.data as ArrayList<GenericItem>
                            emrViewModel.diagnosisList = tempList.filter { it.genericItemName != null } as ArrayList<GenericItem>
                            initDropDowns()
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