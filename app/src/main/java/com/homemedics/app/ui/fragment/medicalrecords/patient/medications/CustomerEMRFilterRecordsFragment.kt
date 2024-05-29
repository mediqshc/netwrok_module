package com.homemedics.app.ui.fragment.medicalrecords.patient.medications

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.fatron.network_module.models.request.emr.customer.records.EMRRecordsFilterRequest
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.medicine.ReferredListResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordsListResponse
import com.fatron.network_module.models.response.emr.type.GenericEMRListResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ResponseResult
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.base.BaseFragment
import com.homemedics.app.databinding.FragmentCustomerEmrFilterRecordsBinding
import com.homemedics.app.utils.*
import com.homemedics.app.viewmodel.EMRViewModel

class CustomerEMRFilterRecordsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentCustomerEmrFilterRecordsBinding
    private val emrViewModel: EMRViewModel by activityViewModels()
    private var emrFilterRequest = EMRRecordsFilterRequest()

    override fun setLanguageData() {
        mBinding.apply {
            lang = ApplicationClass.mGlobalData
            layoutButtons.apply {
                actionbar.title = lang?.globalString?.filter.getSafe()
                etStartDate.hint = lang?.globalString?.startDate.getSafe()
                etEndDate.hint = lang?.globalString?.endDate.getSafe()
                cdReferredBy.hint = lang?.emrScreens?.referredBy.getSafe()
                cdMedicineType.hint = lang?.emrScreens?.medicineType.getSafe()
                cdTests.hint = lang?.emrScreens?.testAndProcedure.getSafe()
                bClearFilter.text = lang?.globalString?.clearFilter.getSafe()
                bSave.text = lang?.globalString?.applyFilter.getSafe()
            }
        }
    }

    override fun init() {
        emrFilterRequest = Gson().fromJson(
            Gson().toJson(emrViewModel.emrFilterRequest),
            EMRRecordsFilterRequest::class.java
        )
        observe()

        when (emrViewModel.emrFilterRequest.type){
            Enums.EMRType.REPORTS.key -> {
                mBinding.cdMedicineType.gone()
            }
            Enums.EMRType.MEDICATION.key -> {
                mBinding.cdTests.gone()
            }
            Enums.EMRType.VITALS.key -> {
                mBinding.cdMedicineType.gone()
                mBinding.cdTests.gone()
            }
        }

        if(emrViewModel.referredList.isEmpty())
            getReferredList()
        else
            initDropDowns()

        if(emrViewModel.labTestList.isEmpty())
            getLabTests()
        else
            initDropDowns()
    }

    private fun initDropDowns(){
        mBinding.apply {
            emrFilterRequest.startDate?.let {
                etStartDate.text = getDateInFormat(it, "yyyy-MM-dd", "dd/MM/yyyy")
            }

            emrFilterRequest.endDate?.let {
                etEndDate.text = getDateInFormat(it, "yyyy-MM-dd", "dd/MM/yyyy")
            }

            cbSharedRecords.isChecked = emrFilterRequest.sharedRecord.getBoolean()

            //referred by
            var selectionIndex = 0
            val referredList = emrViewModel.referredList
            var referredIndex = -1
            if(emrFilterRequest.referredBy != null){
                referredIndex = referredList.indexOfFirst { it.genericItemId == emrFilterRequest.referredBy.getSafe() }.getSafe()
            }
            selectionIndex = referredIndex
            val referredNames = referredList.map { it.genericItemName }
            cdReferredBy.data = referredNames as ArrayList<String>
            if(selectionIndex > -1){
                cdReferredBy.selectionIndex = selectionIndex
                val speciality = referredList[selectionIndex]
                emrFilterRequest.referredBy = speciality.genericItemId.getSafe()
            }
            cdReferredBy.onItemSelectedListener = { _, position: Int ->
                val referred = referredList[position]
                emrFilterRequest.referredBy = referred.genericItemId.getSafe()
            }

            //medicine type
            val medicineTypeList = metaData?.productTypes ?: arrayListOf()
            var medicineTypeIndex = -1
            if(emrFilterRequest.medicineType != null){
                medicineTypeIndex = medicineTypeList.indexOfFirst { it.genericItemId == emrFilterRequest.medicineType.getSafe() }.getSafe()
            }
            selectionIndex = medicineTypeIndex
            val medicineTypeNames = medicineTypeList.map { it.genericItemName }
            cdMedicineType.data = medicineTypeNames as ArrayList<String>
            if(selectionIndex > -1){
                cdMedicineType.selectionIndex = selectionIndex
                val speciality = medicineTypeList[selectionIndex]
                emrFilterRequest.medicineType = speciality.genericItemId.getSafe()
            }
            cdMedicineType.onItemSelectedListener = { _, position: Int ->
                val medicineType = medicineTypeList[position]
                emrFilterRequest.medicineType = medicineType.genericItemId.getSafe()
            }

            //procedure type
            val testList = emrViewModel.labTestList
            var testIndex = -1
            if(emrFilterRequest.procedureType != null){
                testIndex = testList.indexOfFirst { it.genericItemId == emrFilterRequest.procedureType.getSafe() }.getSafe()
            }
            selectionIndex = testIndex
            val testNames = testList.map { it.genericItemName }
            cdTests.data = testNames as ArrayList<String>
            if(selectionIndex > -1){
                cdTests.selectionIndex = selectionIndex
                val speciality = testList[selectionIndex]
                emrFilterRequest.procedureType = speciality.genericItemId.getSafe()
            }
            cdTests.onItemSelectedListener = { _, position: Int ->
                val testType = testList[position]
                emrFilterRequest.procedureType = testType.genericItemId.getSafe()
            }
        }
    }

    override fun getFragmentLayout() = R.layout.fragment_customer_emr_filter_records

    override fun getViewModel() {

    }

    override fun getViewBinding() {
        mBinding = binding as FragmentCustomerEmrFilterRecordsBinding

        mBinding.etStartDate.mBinding.editText.setTextColor(resources.getColor(R.color.black90, null))
        mBinding.etEndDate.mBinding.editText.setTextColor(resources.getColor(R.color.black90, null))
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
                    emrFilterRequest.startDate = getDateInFormat(etStartDate.text, "dd/MM/yyyy", "yyyy-MM-dd")

                if(etEndDate.text.isNotEmpty())
                    emrFilterRequest.endDate = getDateInFormat(etEndDate.text, "dd/MM/yyyy", "yyyy-MM-dd")

                emrFilterRequest.sharedRecord = cbSharedRecords.isChecked.getInt()

                emrViewModel.emrFilterRequest = emrFilterRequest
                findNavController().popBackStack()
            }

            layoutButtons.bClearFilter.setOnClickListener {
                emrViewModel.emrFilterRequest = EMRRecordsFilterRequest()
                emrFilterRequest = emrViewModel.emrFilterRequest

                etStartDate.text = ""
                etEndDate.text = ""

                cdReferredBy.data = arrayListOf()
                cdMedicineType.data = arrayListOf()
                cdTests.data = arrayListOf()

                cdReferredBy.mBinding.dropdownMenu.setText("")
                cdMedicineType.mBinding.dropdownMenu.setText("")
                cdTests.mBinding.dropdownMenu.setText("")

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

    private fun getReferredList(){
        if (isOnline(requireActivity())) {
            emrViewModel.getReferredList().observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<ReferredListResponse>
                        response.data?.let {
                            val tempList = it.referredBy as ArrayList<GenericItem>
                            emrViewModel.referredList = tempList.filter { it.genericItemName != null } as ArrayList<GenericItem>
                            initDropDowns()
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

    private fun getLabTests() {
        val pageRequest = PageRequest(page = 1)
        if (isOnline(requireActivity())) {
            emrViewModel.getLabTests(pageRequest).observe(this){
                when (it) {
                    is ResponseResult.Success -> {
                        val response = it.data as ResponseGeneral<GenericEMRListResponse>
                        response.data?.let {
                            val tempList = it.labTests?.data as ArrayList<GenericItem>
                            emrViewModel.labTestList = tempList.filter { it.genericItemName != null } as ArrayList<GenericItem>
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