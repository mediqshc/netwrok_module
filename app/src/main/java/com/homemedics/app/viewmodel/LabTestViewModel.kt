package com.homemedics.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.request.labtest.LabBranchListRequest
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.request.labtest.LabTestFilterRequest
import com.fatron.network_module.models.request.labtest.LabTestHomeCollectionRequest
import com.fatron.network_module.models.response.labtest.LabResponse
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.pharmacy.OrderDetailsResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import okhttp3.MultipartBody

class LabTestViewModel : ViewModel() {

    class ViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LabTestViewModel() as T
        }
    }

    fun flushData(removeBookingInfo: Boolean = true) {
        if(removeBookingInfo) {
            bookConsultationRequest = BookConsultationRequest()
//            labTestFilterRequest = LabTestFilterRequest()
            bookingIdResponse = PartnerProfileResponse()
            orderDetailsResponse = OrderDetailsResponse()
            TinyDB.instance.remove(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
        }
        selectedLabBranch = null
        selectedMainLab = null
        selectedLabTest = null
        selectedAddress = MutableLiveData<MultipleViewItem>()
    }

    fun removeSavedBookingId() {
        TinyDB.instance.remove(Enums.TinyDBKeys.LAB_TEST_BOOKING_ID.key)
    }

    var fromSearch = false

    var labTestCategories = MutableLiveData<ArrayList<GenericItem>>()

    var fromBLT = true //decides whether going with first flow or second flow .. true => first flow
    var fileList = ArrayList<MultipleViewItem>()
    var selectedLabBranch: LabResponse? = null
    var selectedMainLab: LabResponse? = null
    var selectedLabTest: LabTestResponse? = null
    var selectedAddress = MutableLiveData<MultipleViewItem>()

    var bookConsultationRequest = BookConsultationRequest()
    var labTestFilterRequest = LabTestFilterRequest()
    var bookingIdResponse = PartnerProfileResponse()
    var orderDetailsResponse = OrderDetailsResponse()

    fun getLabTests(request: LabTestFilterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getLabTests(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getLabTestCategories(request: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getLabTestCategories(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getLabList(request: LabTestFilterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getLabList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getCartDetails(request: LabTestCartRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getCartDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getLabBranchList(request: LabBranchListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getLabBranchList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun labTestAddToCart(request: LabTestCartRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.labTestAddToCart(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun labTestDeleteToCart(request: LabTestCartRequest? = null) = liveData {
        val ids = orderDetailsResponse.labCartItems?.map { it.id }
        val reqForAllDelete = LabTestCartRequest(item_id = ids as ArrayList<Int>?)

        emit(ResponseResult.Pending)
        val result = ApiRepository.labTestDeleteToCart(request ?: reqForAllDelete)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun updateHomeCollection(request: LabTestHomeCollectionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.updateHomeCollection(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //create booking
    fun createBookingId(request: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPartnerDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteDocumentApiCall(request: DeleteAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteDocumentApiCall(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addConsultationAttachment(booking_id:Int?,attachment_type:String,document: ArrayList<MultipartBody.Part>?) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addConsultationAttachment(booking_id, attachment_type, document)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetAttachments(appointmentDetailReq: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getAttachments(appointmentDetailReq)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun bookConsultation(request: BookConsultationRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.bookConsultation(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}