package com.homemedics.app.viewmodel

import android.os.CountDownTimer
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.request.auth.*
import com.fatron.network_module.models.request.chat.TwilioTokenRequest
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.utils.getSafe

class AuthViewModel: ViewModel() {

    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel() as T
        }
    }


    fun startTimer(
        view: TextView,
        second: String? = null,
        timer: Int?,
        onFinish: () -> Unit
    ): CountDownTimer {
        return object : CountDownTimer(timer.getSafe().toLong()*1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsInMilli: Long = 1000
                val elapsedSeconds = millisUntilFinished / secondsInMilli
                view.text = "($elapsedSeconds ${second})"
            }

            override fun onFinish() {
                onFinish.invoke()
            }
        }.start()
    }


    fun sendCodeApiCall(request: OtpRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.sendOtp(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun verifyCodeApiCall(request: ForgetPwdRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.verifyOtp(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun changePwdApiCall(request: ForgetPwdRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.changePwd(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun registerApiCall(request: SignupRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.register(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun loginApiCall(request: LoginRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.login(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun verifyPhoneNumApiCall(request: LoginRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.verifyPhoneNum(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteAccount(request: DeleteAccountRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteAccount(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun logout(request: LogoutRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.logout(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun unSubscribeEconPackage(request: EconOtpRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.unSubscribeEconPackage(request)
        emit(result)
        emit(ResponseResult.Complete)
    }


     fun getTwilioChatToken(request: TwilioTokenRequest)= liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getTwilioChatToken(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
    fun verifyEconOtpApiCall(request: EconSubscribeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.verifyEconOtp(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

}