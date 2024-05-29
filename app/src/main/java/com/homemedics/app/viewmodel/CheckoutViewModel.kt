package com.homemedics.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.bdc.BDCFilterRequest
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.checkout.paymob.PaymobPaymentRequest
import com.fatron.network_module.models.request.checkout.paymob.PaymobPaymentStatusRequest
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.checkout.CheckoutDetailResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult

class CheckoutViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CheckoutViewModel() as T
        }
    }

    fun flushData(){

    }

    var fromCheckout: Boolean = false
    var bookedOrderId: Int? = null
    var bdcFilterRequest = BDCFilterRequest()
    var partnerProfileResponse = PartnerProfileResponse()
    var partnerSlotsResponse = PartnerSlotsResponse()
    var bookConsultationRequest = BookConsultationRequest()
    var  checkoutDetailResponse = CheckoutDetailResponse()
    var discountOnPackageAmount=0.00
    var payAbleAmount=0.00

    fun checkoutDetails(request: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.checkoutDetails(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun applyPackage(request: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.applyPackage(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun removePackage(request: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result= ApiRepository.removePackage(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun bookConsultation(request: BookConsultationRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.bookConsultation(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getPaymobToken() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getPaymobToken()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun paymobOrders(request: PaymobPaymentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.paymobOrders(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun paymobPayment(request: PaymobPaymentRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.paymobPayment(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun paymobPaymentStatus(request: PaymobPaymentStatusRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.paymobPaymentStatus(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}