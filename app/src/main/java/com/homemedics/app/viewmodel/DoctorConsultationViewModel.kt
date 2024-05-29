package com.homemedics.app.viewmodel

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BDCFilterRequest
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceDetailRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceListRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceStoreRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.bdc.BDCFilterResponse
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.utils.DataCenter
import com.homemedics.app.utils.DoctorListingPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import okhttp3.MultipartBody

class DoctorConsultationViewModel : ViewModel() {
    var fileList = ArrayList<MultipleViewItem>()
    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DoctorConsultationViewModel() as T
        }
    }

    fun flushData(){
        fromSearch = false
        bdcFilterRequest = BDCFilterRequest()
        partnerProfileResponse = PartnerProfileResponse()
        bookConsultationRequest = BookConsultationRequest()
        bookConsultationRequest = BookConsultationRequest()
//        doctorListing.value =  arrayListOf<PartnerProfileResponse>()
        selectedAddress = MutableLiveData<MultipleViewItem>()
    }

    var fromSearch = false
    var cityIdGlobal = 0
    var bdcFilterRequest = BDCFilterRequest()
    var partnerProfileResponse = PartnerProfileResponse()
    var partnerSlotsResponse = PartnerSlotsResponse()
    var bookConsultationRequest = BookConsultationRequest()

    var specialities = MutableLiveData<ArrayList<GenericItem>>(DataCenter.getDoctorSpecialities())

    //    var doctorListing = MutableLiveData<ArrayList<PartnerProfileResponse>>(arrayListOf())
    var dateSlots = MutableLiveData<ArrayList<GenericItem>>(DataCenter.getDates(7))
    val familyConnections = MutableLiveData(FamilyResponse())
    var selectedAddress = MutableLiveData<MultipleViewItem>()


    //home service
    var homeConsultationRequest = HomeServiceStoreRequest()

     var countryName =MutableLiveData<String>()
    var cityName=MutableLiveData<String>()

    var cityId: Int? =0
    var countryId: Int =0
    var medSpecialities = MutableLiveData<ArrayList<GenericItem>>()
    var homeServiceRequest: HomeServiceDetailRequest?=null


    fun getFamilyConnectionsApiCall() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getFamilyConnections()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getDoctors(request: BDCFilterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getDoctors(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    val items: Flow<PagingData<PartnerProfileResponse>> = Pager(
        config = PagingConfig(pageSize = 100, enablePlaceholders = false),
        pagingSourceFactory = {
            DoctorListingPagingSource(
                ApiRepository,
                request = bdcFilterRequest
            )
        }
    ).flow.distinctUntilChanged()

    val _partnersList = MutableLiveData<List<PartnerProfileResponse>>()
    var partnerProfile: LiveData<List<PartnerProfileResponse>> = _partnersList
    suspend fun getDoctorsData() {
        val result =
            ApiRepository.getDoctors(bdcFilterRequest) as ResponseResult<ResponseGeneral<BDCFilterResponse>>
        when (result) {
            is ResponseResult.Success -> {
                val data = result.data
                _partnersList.postValue(data.data?.partners)
            }
        }
    }

//        .cachedIn(viewModelScope)

    //other then myself
    fun getPartnerDetails(request: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPartnerDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getPartnerAbout(request: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPartnerAbout(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getPartnerReviews(request: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPartnerReviews(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun bookConsultation(request: BookConsultationRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.bookConsultation(request)
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

    fun getSlots(request: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getSlots(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetAttachments(appointmentDetailReq: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getAttachments(appointmentDetailReq)
        emit(result)
        emit(ResponseResult.Complete)
    }

    //get details of homeservice
    fun getHomeServiceList(homeServiceRequest: HomeServiceListRequest?) = liveData {
        emit(ResponseResult.Pending)
        val result = homeServiceRequest?.let { ApiRepository.getHomeServiceList(it) }
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getHomeServiceDetails(homeServiceRequest: HomeServiceDetailRequest?) = liveData {
        emit(ResponseResult.Pending)
        val result = homeServiceRequest?.let { ApiRepository.getHomeServiceDetails(it) }
        emit(result)
        emit(ResponseResult.Complete)
    }



    fun callApiBookService(request: HomeServiceStoreRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callApiBookService(request)
        emit(result)
        emit(ResponseResult.Complete)
    }


}