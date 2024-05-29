package com.homemedics.app.viewmodel

import android.location.Location
import android.os.CountDownTimer
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.claim.ClaimStatusRequest
import com.fatron.network_module.models.request.walkin.*
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.claim.ClaimConnection
import com.fatron.network_module.models.response.claim.ClaimConnectionsResponse
import com.fatron.network_module.models.response.meta.RequiredDocumentType
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.pharmacy.HospitalDiscountCenterRequest
import com.fatron.network_module.models.response.walkin.WalkInResponse
import com.fatron.network_module.models.response.walkin.services.WalkInService
import com.fatron.network_module.models.response.walkinpharmacy.WalkInInitialResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInItemResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.utils.getSafe
import okhttp3.MultipartBody

class WalkInViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WalkInViewModel() as T
        }
    }

    var fromFilter = false
    var noReload = false
    var fromCode = false
    var isDiscountCenter = false
    var isPharmacy = false
    var isLab = false
   var labDiscountCenterBooked=false
    var hospitalDiscountCenterBooked=false

    var isSubmitReviewAttachment = false
    var isHospital = false
   var documentTypeid:Int=0
    var familyMemberId: Int = 0
    var partnerServiceId: Int = 0
    var packageAccount: String? = null
    var hosiptalService: String? = null
    var packageAccountId: Int = 0
    var hosiptalServiceId: Int = 0
    var walkInInitialResponse = WalkInInitialResponse()
    var walkInAttachments = ArrayList<Attachment>()
    var bookingId: Int = 0
    var cityId: Int = 0
    var isFamilyMemberSelected=false
    var pharmacyId: Int? = null
    var labId: Int? = null
    var hospitalId: Int? = null
    var walkInPharmacyName: String? = null
    var walkInLabName: String? = null
    var walkInHospitalName: String? = null
    var page: Int? = 1
    var currentLatLng: Location? = null
    var mapLatLng: Location = Location("")
    var walkInServicesFilterRequest = WalkInServicesFilterRequest()
    var bookingIdResponse = PartnerProfileResponse()
    var serviceName: String? = null
    var bookConsultationRequest = BookConsultationRequest()
    var walkInItem = WalkInItemResponse()
    var walkInLabDiscountCenter =false

    var walkInService: WalkInService? = null
    var walkInResponse: WalkInResponse? = null
    var walkInRequest = WalkInRequest()
    var walkInStoreRequest = WalkInStoreRequest()
    var selectedConnection: ClaimConnection? = null
    var filterConnection: List<ClaimConnection>? = null
    var documentTypes: List<RequiredDocumentType>? = null
    var filterClaimConnectionsResponse: ClaimConnectionsResponse? = null

    var selectedOrder: OrderResponse? = null
    var fileList = ArrayList<MultipleViewItem>()

    var walkInPharmacyListLiveData = MutableLiveData<ArrayList<WalkInItemResponse>>()
    var walkInPharmacyMapLiveData = MutableLiveData<ArrayList<WalkInItemResponse>>()

    var walkInLaboratoryListLiveData = MutableLiveData<ArrayList<WalkInItemResponse>>()
    var walkInLaboratoryMapLiveData = MutableLiveData<ArrayList<WalkInItemResponse>>()

    var walkInHospitalListLiveData = MutableLiveData<ArrayList<WalkInItemResponse>>()
    var walkInHospitalMapLiveData = MutableLiveData<ArrayList<WalkInItemResponse>>()

    var walkInHospitalServices = MutableLiveData<ArrayList<WalkInService>>()

    var fromDetails = false
    var isAttachment = false

    fun startTimer(
        view: TextView,
        second: String? = null,
        timer: Int?,
        onTick: (String) -> Unit,
        onFinish: () -> Unit
    ): CountDownTimer {
        return object : CountDownTimer(timer.getSafe().toLong()*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsInMilli: Long = 1000
                val elapsedSeconds = millisUntilFinished / secondsInMilli
                onTick.invoke(elapsedSeconds.toString())
            }

            override fun onFinish() {
                onFinish.invoke()
            }
        }.start()
    }

    fun addConsultationAttachment(booking_id:Int?,attachment_type:String,document: ArrayList<MultipartBody.Part>?) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addConsultationAttachment(booking_id, attachment_type, document)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addWalkInAttachment(request: AddWalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addWalkInAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteWalkInAttachment(request: WalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteWalkInAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callWalkInGetAttachments(request: WalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callWalkInGetAttachments(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun walkInDetails(request: WalkInRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.walkInDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun walkInStatus(request: ClaimStatusRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.walkInStatus(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInPharmacyList(request: WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInPharmacyList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInPharmacyConnections(request: WalkInConnectionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInPharmacyConnections(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun initialWalkInPharmacy(request: WalkInInitialRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.initialWalkInPharmacy(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeWalkInPharmacy(request: WalkInStoreRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeWalkInPharmacy(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun resendWalkInConfirmation(request: WalkInRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.resendWalkInConfirmation(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInPharmaServiceTypes() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInPharmaServiceTypes()
        emit(result)
        emit(ResponseResult.Complete)
    }

    //walkin lab

    fun addWalkInLabAttachment(request: AddWalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addWalkInLabAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteWalkInLabAttachment(request: WalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteWalkInLabAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetWalkInLabAttachments(request: WalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callGetWalkInLabAttachments(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun walkInLabDetails(request: WalkInRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.walkInLabDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun walkInLabStatus(request: ClaimStatusRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.walkInLabStatus(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun resendWalkInLabConfirmation(request: WalkInRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.resendWalkInLabConfirmation(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInLaboratoryList(request: WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInLaboratoryList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInLaboratoryConnections(request: WalkInConnectionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInLaboratoryConnections(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun initialWalkInLaboratory(request: WalkInInitialRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.initialWalkInLaboratory(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeWalkInLaboratory(request: WalkInStoreRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeWalkInLaboratory(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInLabServiceTypes() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInLabServiceTypes()
        emit(result)
        emit(ResponseResult.Complete)
    }

    //walkin hospital

    fun addWalkInHospitalAttachment(request: AddWalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addWalkInHospitalAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteWalkInHospitalAttachment(request: WalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteWalkInHospitalAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetWalkInHospitalAttachments(request: WalkInAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callGetWalkInHospitalAttachments(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun walkInHospitalDetails(request: WalkInRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.walkInHospitalDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun walkInHospitalStatus(request: ClaimStatusRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.walkInHospitalStatus(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun resendWalkInHospitalConfirmation(request: WalkInRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.resendWalkInHospitalConfirmation(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInHospitalList(request: WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInHospitalList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInHospitalConnections(request: WalkInConnectionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInHospitalConnections(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun initialWalkInHospital(request: WalkInInitialRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.initialWalkInHospital(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeWalkInHospital(request: WalkInStoreRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeWalkInHospital(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInHospitalServicesList(request: WalkInInitialRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInHospitalServicesList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInHospitalServiceTypes() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInHospitalServiceTypes()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInPharmacyDiscount(request:WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInPharmacyDiscount(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun scanPharmacyQRCode(request: QRCodeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.scanPharmacyQRCode(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun scanLaboratoryQRCode(request: QRCodeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.scanLaboratoryQRCode(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun scanHospitalQRCode(request: QRCodeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.scanHospitalQRCode(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInLaboratoryDiscount(request:WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInLaboratoryDiscount(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInHospitalDiscount(request:WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInHospitalDiscount(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getHospitalDiscountCenter(request: HospitalDiscountCenterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.HospitalDiscountCenter(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun getLabDiscountCenter(request: HospitalDiscountCenterRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.LabDiscountCenter(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}