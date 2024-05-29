package com.homemedics.app.viewmodel

import android.content.Intent
import androidx.lifecycle.*
import com.fatron.network_module.models.request.chat.TwilioTokenRequest
import com.fatron.network_module.models.request.fcm.UpdateFCMTokenRequest
import com.fatron.network_module.models.request.notification.CreateNotifRequest
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.response.linkaccount.CompanyResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.homemedics.app.utils.getAndroidID
import com.homemedics.app.utils.getAppContext
import kotlinx.coroutines.launch
import timber.log.Timber


class HomeViewModel: ViewModel() {
    val _homeActivityIntent = MutableLiveData<Intent>()
    val homeActivityIntent: LiveData<Intent> = _homeActivityIntent
    val isDoctorViewEnabled = MutableLiveData<Boolean>()
    var linkedAccounts = arrayListOf<CompanyResponse>()

    fun updateFCMToken() {
        Timber.e("updateFCMToken")
        val token = TinyDB.instance.getString(Enums.TinyDBKeys.FCM_TOKEN.key)
        if(TinyDB.instance.getString(Enums.TinyDBKeys.TOKEN_USER.key).isNotEmpty()){

            val request = UpdateFCMTokenRequest(
                fcmToken = token,
                deviceToken = getAndroidID(getAppContext())
            )

            viewModelScope.launch {
                ApiRepository.updateFCMToken(request)
            }
        }
    }

    fun addReview(request: MyOrdersRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addReview(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun isDoctorViewEnabled(checked: Boolean) {
        isDoctorViewEnabled.postValue(checked)
    }

}