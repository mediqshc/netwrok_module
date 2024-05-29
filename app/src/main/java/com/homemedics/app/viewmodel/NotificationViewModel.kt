package com.homemedics.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.request.notification.NotificationReadRequest
import com.fatron.network_module.models.request.notification.NotificationRequest
import com.fatron.network_module.models.request.orders.OrdersRequest
import com.fatron.network_module.models.response.notification.Notification
import com.fatron.network_module.models.response.notification.NotificationResponse
import com.fatron.network_module.models.response.ordersdetails.OrdersListResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult

class NotificationViewModel: ViewModel() {

    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MyOrderViewModel() as T
        }
    }
    var page = 1
    var categoryId = 0
    val notiListResponse = MutableLiveData<NotificationResponse>()
    var listItems = ArrayList<Notification>()

    fun getNoti(request: NotificationRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.getNotification(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun callReadNotif(request: NotificationReadRequest) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.callReadNotif(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}