package com.homemedics.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.walkin.WalkInListRequest
import com.fatron.network_module.models.request.walkin.WalkInServicesFilterRequest
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import okhttp3.MultipartBody

class WalkInPharmacyViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WalkInPharmacyViewModel() as T
        }
    }

    var walkInServicesFilterRequest = WalkInServicesFilterRequest()
    var bookingIdResponse = PartnerProfileResponse()
    var bookConsultationRequest = BookConsultationRequest()

    var fileList = ArrayList<MultipleViewItem>()

    fun getPharmacysList() = liveData<ResponseResult<*>> {

    }

    fun addConsultationAttachment(booking_id:Int?,attachment_type:String,document: ArrayList<MultipartBody.Part>?) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addConsultationAttachment(booking_id, attachment_type, document)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getWalkInPharmacyList(request: WalkInListRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getWalkInPharmacyList(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

}