package com.homemedics.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.request.video.ParticipantsRequest
import com.fatron.network_module.models.request.video.RoomRequest
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult

class CallViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CallViewModel() as T
        }
    }

    var fromPush = false
    var bookingId = ""
    var orderId = 0
    var appointmentResponse: AppointmentResponse? = null
    var appointmentAttachments: ArrayList<Attachment>? = null
    var orderDetailsResponse: OrderResponse? = null

    var orderDetailsResponse2: OrderResponse? = null

    fun getTwilioVideoCallToken(tokenRequest: TokenRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getTwilioVideoCallToken(tokenRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addParticipantsToVideoCall(requestParticipantsRequest: ParticipantsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addParticipantsToVideoCall(requestParticipantsRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun destroyRoom(requestParticipantsRequest: ParticipantsRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.destroyRoom(requestParticipantsRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun destroyRoom(requestRoomRequest: RoomRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.isRoomExist(requestRoomRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun orderDetails(request: TokenRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.orderDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}