package com.homemedics.app.viewmodel

import androidx.lifecycle.*
import com.fatron.network_module.NetworkModule
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.chat.*
import com.fatron.network_module.models.response.chat.ConversationResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.utils.Enums

class ChatViewModel: ViewModel() {

    var selectedBooking: ConversationResponse?=null
    var _twilioToken= MutableLiveData<String>()
    val twilioToken: LiveData<String>  =_twilioToken
    var conversationList = MutableLiveData<List<ConversationResponse>>()
    var selectedTab: Enums.ConversationType = Enums.ConversationType.CHATS
    var conversationRequest = ConversationListRequest()
    var pageNum:Int=1

    fun getPartnersList(conversationRequest: ConversationListRequest, type: Int) = liveData {
        emit(ResponseResult.Pending)
        var url =
            "${NetworkModule.networkModule.baseUrl}v1/chat/get/customers" //type==1 ,doctor
        if (type == 0) //customer
            url =
                "${NetworkModule.networkModule.baseUrl}v1/chat/get/partners"
        val result = ApiRepository.getPartnersList(conversationRequest, url)
        emit(result)
        emit(ResponseResult.Complete)
    }


    fun getTwilioChatToken(request: TwilioTokenRequest)=liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getTwilioChatToken(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callChatSession(request: ConversationSessionRequest)=liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callChatSession(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callChatDetailApi(chatDetailRequest: chatDetailRequest)=liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callChatDetail(chatDetailRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callSendEmrApi(sendEmrRequest: SendEmrRequest )=liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.callSendEmrApi(sendEmrRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel() as T
        }
    }

}