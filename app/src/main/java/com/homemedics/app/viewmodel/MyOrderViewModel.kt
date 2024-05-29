package com.homemedics.app.viewmodel

import androidx.lifecycle.*
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.PartnerDetailsRequest
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.request.orders.OrdersRequest
import com.fatron.network_module.models.request.orders.RescheduleRequest
import com.fatron.network_module.models.request.orders.ScheduledVisitsRequest
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.orders.ScheduledDutyResponse
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.models.response.ordersdetails.OrdersListResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.model.AddressModel
import com.homemedics.app.utils.Enums

class MyOrderViewModel : ViewModel() {

    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyOrderViewModel() as T
        }
    }

    val _address = MutableLiveData<AddressModel>()
    val address: LiveData<AddressModel> = _address

    var fileList = ArrayList<MultipleViewItem>()

    var visitDate: String? = null

    var bookingId: String = "0"

    var dutyId: Int? = null

    var serviceId: Int = 0

    var partnerUserId: Int = 0

    var ordersListResponse = MutableLiveData<OrdersListResponse>()

    var orderDetailsResponse: OrderResponse? = null

    var partnerSlotsResponse = PartnerSlotsResponse()

    var bookConsultationRequest = BookConsultationRequest()

    var scheduledDuty = ScheduledDutyResponse()

    var selectedOrder: OrderResponse? = null

    var selectedTab = MutableLiveData(Enums.OrdersType.CURRENT)
    var page = 1
    var listItems = ArrayList<OrderResponse>()

    var ordersRequest = OrdersRequest()

    fun flushData(){
//        _address.postValue(AddressModel())
//        fileList = ArrayList<MultipleViewItem>()
//        bookingId = ""
//        serviceId = 0
//        partnerUserId = 0
//        orderDetailsResponse = null
//        partnerSlotsResponse = PartnerSlotsResponse()
//        bookConsultationRequest = BookConsultationRequest()
        selectedTab = MutableLiveData(Enums.OrdersType.CURRENT)
        page = 1
        ordersRequest = OrdersRequest()
    }

    fun getOrders(request: OrdersRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.getOrders(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun orderDetails(request: TokenRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.orderDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addReview(request: MyOrdersRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addReview(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun cancelOrder(request: TokenRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.cancelOrder(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getHHCDuties(request: ScheduledVisitsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getHHCDuties(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun rescheduleOrder(request: RescheduleRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.rescheduleOrder(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getSlots(request: PartnerDetailsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getSlots(request)
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