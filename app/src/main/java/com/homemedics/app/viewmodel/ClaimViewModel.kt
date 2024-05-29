package com.homemedics.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.claim.*
import com.fatron.network_module.models.request.emr.type.PageRequest
import com.fatron.network_module.models.request.orders.OrdersRequest
import com.fatron.network_module.models.response.claim.ClaimConnection
import com.fatron.network_module.models.response.claim.ClaimConnectionsResponse
import com.fatron.network_module.models.response.claim.ClaimResponse
import com.fatron.network_module.models.response.claim.ClaimServicesResponse
import com.fatron.network_module.models.response.meta.RequiredDocumentType
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.models.response.ordersdetails.OrdersListResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.ui.custom.CustomServiceTypeView
import com.homemedics.app.utils.Enums

class ClaimViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClaimViewModel() as T
        }
    }

    fun flushData(){
        claimRequest = ClaimRequest()
        claimConnectRequest = null
        claimResponse = null
        claimServicesResponse = null
        claimConnectionsResponse = null
        selectedConnection = null
        fromDetails = false
        fileList = arrayListOf()
    }
    var reason:String? = null
    var claimBookingId:Int?=null
    var cityId = 0
    var claimRequest = ClaimRequest()
    var claimConnectRequest: ClaimConnectRequest? = null
    var claimResponse: ClaimResponse? = null
    var  comment:String?=null
    var claimServicesResponse: ClaimServicesResponse? = null
    var claimConnectionsResponse: ClaimConnectionsResponse? = null
    var claimListRequest = OrdersRequest().apply { serviceTypeId = CustomServiceTypeView.ServiceType.Claim.id }
    var claimListResponse = MutableLiveData<OrdersListResponse>()
    var documentTypes: List<RequiredDocumentType>? = null

    var countryId: Int? = null

    var selectedConnection: ClaimConnection? = null

    var fileList = ArrayList<MultipleViewItem>()

    var selectedTab = MutableLiveData(Enums.OrdersType.CURRENT)
    var page = 1
    var bookingId: Int = 0
    var listItems = ArrayList<OrderResponse>()
    var fromDetails = false
    var isAttachment = false
    var category: String? = null

    var partnerServiceId: Int = 0

    fun createClaimId() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.createClaimId(claimRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getClaimServices(request: PageRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getClaimServices(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun claimDetails() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.claimDetails(claimRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getClaimConnections(request: ClaimConnectionExclusionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getClaimConnections(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getConnectionExclusions(request: ClaimConnectionExclusionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getConnectionExclusions(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun connectClaim(request: ClaimConnectRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.connectClaim(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun storeClaimRequest() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.storeClaimRequest(claimRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun cancelClaim(request: ClaimStatusRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.cancelClaim(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addClaimAttachment(request: AddClaimAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addClaimAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteClaimAttachment(request: DeleteAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteClaimAttachment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetAttachments(request: ClaimAttachmentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callGetAttachments(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getClaims(request: OrdersRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.getOrders(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}