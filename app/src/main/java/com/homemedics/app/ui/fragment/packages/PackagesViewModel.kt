package com.homemedics.app.ui.fragment.packages

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.fatron.network_module.models.request.auth.EconOtpRequest
import com.fatron.network_module.models.request.auth.EconSubscribeRequest
import com.fatron.network_module.models.response.packages.Package
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult

class PackagesViewModel : ViewModel() {
    val isPackagesScreenCancelled = MutableLiveData<Boolean>()
    val selectedPackage = MutableLiveData<Package>()
    // for otp apis
    val phone = MutableLiveData<String>()
    val network = MutableLiveData<String>()

    fun getPackagesApiCall(network: String, phone: String) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPackages(network, phone)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun sendOtpApiCall(request: EconOtpRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.sendEconOtp(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun subscribeEconPackageApiCall(request: EconSubscribeRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.subscribeEconPackage(request)
        emit(result)
        emit(ResponseResult.Complete)
    }




}