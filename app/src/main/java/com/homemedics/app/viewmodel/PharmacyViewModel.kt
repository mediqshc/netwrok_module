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
import com.fatron.network_module.models.request.pharmacy.*
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.partnerprofile.PatientResponse
import com.fatron.network_module.models.response.pharmacy.PharmacyProduct
import com.fatron.network_module.models.response.pharmacy.Product
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import okhttp3.MultipartBody

class PharmacyViewModel : ViewModel() {

    class ViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PharmacyViewModel() as T
        }
    }

    var fromSearch = false
    var isShowPrescription = MutableLiveData<Boolean>(false)
    var page: Int? = 1
    var cityId: Int? = 0
    var countryId: Int = 0
    var categoryId: Int = 0
    var pharmaProductId: Int = 0
    var prescriptionBookingId: Int? = null
    var patientIdSelected: Int = 0
    var country: String = ""
    var city: String = ""
    var categoryName: String = ""
    var updatedPrice: Double = 0.0
    var quantityLiveData: MutableLiveData<Int>? = MutableLiveData<Int>()
    var pharmacyCategories = MutableLiveData<ArrayList<GenericItem>>()
    var pharmacyProducts = MutableLiveData<ArrayList<PharmacyProduct>>()
    var selectedPharmacyCategory: GenericItem? = null
    var selectedAddress = MutableLiveData<MultipleViewItem>()
    var bookConsultationRequest = BookConsultationRequest()
    var bookingIdResponse = PartnerProfileResponse()
    var addToCart = PharmacyCartRequest()
    var categories = ArrayList<GenericItem>()
    var fileList = ArrayList<MultipleViewItem>()
    var familyMembers: List<FamilyConnection>? = null
    var patients: List<PatientResponse>? = null
    var products: ArrayList<Product>? = null
    var pharmaProductIdsList: ArrayList<Int> = arrayListOf()

    fun flushData() {
        fromSearch = false
        categoryId = 0
        page = 1
        pharmacyProducts.postValue(arrayListOf())
    }

    fun getPharmacyCategories(request: PharmacyCategoriesRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPharmacyCategories(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getPharmacyProductsList(request: PharmacyProductRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPharmacyProductsList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun pharmacyProductDetails(request: PharmacyProductDetailRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.pharmacyProductDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun pharmaAddToCart(request: PharmacyCartRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.pharmaAddToCart(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun pharmaClearCart(request: PharmacyOrderRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.pharmaClearCart(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun pharmacyQuantityUpdate(request: PharmacyCartRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.pharmacyQuantityUpdate(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun pharmacyOrderDetails(request: PharmacyOrderRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.pharmacyOrderDetails(request)
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

    fun bookConsultation(request: BookConsultationRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.bookConsultation(request)
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

    fun deleteDocumentApiCall(request: DeleteAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteDocumentApiCall(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}