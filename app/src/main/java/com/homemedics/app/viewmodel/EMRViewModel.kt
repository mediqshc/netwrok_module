package com.homemedics.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.emr.*
import com.fatron.network_module.models.request.emr.customer.consultation.EMRConsultationFilterRequest
import com.fatron.network_module.models.request.emr.customer.records.EMRRecordsFilterRequest
import com.fatron.network_module.models.request.emr.medicine.MedicineDeleteRequest
import com.fatron.network_module.models.request.emr.medicine.StoreMedicineRequest
import com.fatron.network_module.models.request.emr.type.*
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.EMRDownloadResponse
import com.fatron.network_module.models.response.emr.customer.medicine.MedicinesResponse
import com.fatron.network_module.models.response.emr.type.EmrDetails
import com.fatron.network_module.models.response.emr.type.StoreEMRTypeResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.utils.Enums
import com.homemedics.app.utils.FileUtils
import okhttp3.MultipartBody

class EMRViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EMRViewModel() as T
        }
    }

    var vitals: ArrayList<Vital>? = null
    var systolic = ""
    var diastolic = ""
    var emrChat = false
    var emrId =  MutableLiveData<Int>()
    var tempEmrID: Int = 0
    var emrID: Int = 0
    var docEmr=MutableLiveData<Boolean>()
    var emrNum: String = ""
    var bookingId: Int = 0
    var customerId: String? = null
    var partnerServiceId: String? = null
    var isEdit: Boolean = false
    var isPatient: Boolean = false
    var isPopStack = false
    var isDraft = true
    var selectedEMRType: Enums.EMRType? = null
    var selectedFamily: FamilyConnection? = null
    var selectedRecord: GenericItem? = null
    var selectedFamilyForShare: ArrayList<FamilyConnection> = arrayListOf()
    var attachmentsToUpload = ArrayList<MultipleViewItem>()
    var medicineToModify: MedicinesResponse? = null
    var fromBDC = false
    var fromChat = false
    var fromPush = false

    var request = EMRDownloadRequest()
    var consultationFilterRequest: EMRConsultationFilterRequest = EMRConsultationFilterRequest()
    var storeEMR = StoreEMRTypeResponse()
    var storeMedicineRequest = StoreMedicineRequest()
    var emrDetails: EmrDetails? = EmrDetails()
    var medicine = GenericItem()
    var medicineProduct: MedicinesResponse? = MedicinesResponse()
    var emrFilterRequest: EMRRecordsFilterRequest = EMRRecordsFilterRequest()
    var referredList = ArrayList<GenericItem>()
    var labTestList = ArrayList<GenericItem>()
    var symptomList = ArrayList<GenericItem>()
    var diagnosisList = ArrayList<GenericItem>()
    var dosageId: Int = 0

    var fileList = ArrayList<MultipleViewItem>()

    var diagnosisAttachmentList = ArrayList<MultipleViewItem>()
    var prescriptionAttachmentList = ArrayList<MultipleViewItem>()
    var pharmacyProducts = MutableLiveData<ArrayList<GenericItem>>()

    fun getFamilyConnectionsApiCall(emr: Boolean? = null) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getFamilyConnections(emr)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun emrCreate(symptomsRequest: SymptomsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.emrCreate(symptomsRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getSymptoms(pageRequest: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getSymptoms(pageRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeEMRType(typeRequest: StoreEMRTypeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeEMRType(typeRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun emrDetails(emrDetailsRequest: EMRDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.emrDetails(emrDetailsRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun emrDrafts(request: SymptomsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.emrDrafts(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteEMRType(request: EMRTypeDeleteRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteEMRType(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun editEMRType(storeEMRTypeRequest: StoreEMRTypeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.editEMRType(storeEMRTypeRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeEMR(storeEMRRequest: StoreEMRRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeEMR(storeEMRRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getLabTests(pageRequest: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getLabTests(pageRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getMedicalHealthCares(pageRequest: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getMedicalHealthCares(pageRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getDiagnosisList(pageRequest: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getDiagnosisList(pageRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addEMRAttachment(
        emr_id: Int,
        attachment_type:String,
        emr_type: Int,
        document: ArrayList<MultipartBody.Part>?
    ) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addEMRAttachment(emr_id, attachment_type, emr_type, document)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteEMRDocument(request: DeleteAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteEMRDocument(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //medicine

    fun getMedicineList(pageRequest: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getMedicineList(pageRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeMedicine() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeMedicine(storeMedicineRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun editMedicine(request: StoreMedicineRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.editMedicine(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteMedicine(request: MedicineDeleteRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteMedicine(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //customer consultation

    fun getCustomerConsultationRecords(request: EMRConsultationFilterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCustomerConsultationRecords(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getCustomerConsultationRecordDetails(request: EMRDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCustomerConsultationRecordDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun customerEMRRecordShare(request: EMRShareWithRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.customerEMRRecordShare(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addCustomerMedicineRecord() = liveData {
        storeMedicineRequest.apply {
            name = selectedRecord?.genericItemName
            description = selectedRecord?.description
        }
        emit(ResponseResult.Pending)
        val result = ApiRepository.addCustomerMedicineRecord(storeMedicineRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getCustomerEMRRecords(request: EMRRecordsFilterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCustomerEMRRecords(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getCustomerEMRRecordsDetails(request: EMRDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCustomerEMRRecordsDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addCustomerEMRAttachment(
        emr_id: Int,
        emr_customer_type: Int? = null,
        emr_type_id: Int? = null,
        attachment_type:String,
        document: ArrayList<MultipartBody.Part>?
    ) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addCustomerEMRAttachment(emr_id, emr_customer_type, emr_type_id, attachment_type, document)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteCustomerEMRAttachment(request: DeleteAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteCustomerEMRAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteCustomerEMRRecordType(request: EMRTypeDeleteRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteCustomerEMRRecordType(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun saveCustomerEMRRecord(request: EMRDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.saveCustomerEMRRecord(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteCustomerEMRRecord() = liveData {
        val request = EMRDetailsRequest(emrId = emrID)
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteCustomerEMRRecord(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getReferredList() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getReferredList()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun attachEMRtoBDC() = liveData {
        val request = AttachEMRtoBDCRequest(
            bookingId = bookingId,
            emrCustomerType = selectedEMRType?.key,
            emrIds = arrayListOf(emrID)
        )
        emit(ResponseResult.Pending)
        val result = ApiRepository.attachEMRtoBDC(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun detachEMRtoBDC(request: AttachEMRtoBDCRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.detachEMRtoBDC(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun downloadEMR(request: EMRDownloadRequest, fileUtils: FileUtils) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.downloadEMR(request)
        when(result){
            is ResponseResult.Success -> {
                val response = result.data as ResponseGeneral<EMRDownloadResponse>
                val reports = response.data?.reports

                reports?.forEach {
                    fileUtils.downloadFile(it)
                }
            }
        }
        emit(result)
        emit(ResponseResult.Complete)
    }
}