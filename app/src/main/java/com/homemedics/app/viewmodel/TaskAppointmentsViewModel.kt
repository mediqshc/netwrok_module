package com.homemedics.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.fatron.network_module.models.request.AppointmentStatusRequest
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.appointments.AppointmentListRequest
import com.fatron.network_module.models.request.appointments.AppointmentsActionRequest
import com.fatron.network_module.models.request.partnerprofile.PartnerAvailabilityRequest
import com.fatron.network_module.models.response.appointments.AppointmentListResponse
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.partnerprofile.PartnerAvailabilityResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.R
import com.homemedics.app.utils.AppointmentListingPagingSource
import com.homemedics.app.utils.Enums
import com.homemedics.app.utils.getString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class TaskAppointmentsViewModel: ViewModel() {


    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskAppointmentsViewModel() as T
        }
    }

    var isClearedRequired: Boolean=false
    var bookingId:String=""
    var partnerServiceId: Int = 0
    var dutyId: Int? = null
    var appointmentListRequest = AppointmentListRequest()
    var fromSelect = false
    var fromDetail = false
    var listItems :ArrayList<AppointmentResponse> = arrayListOf<AppointmentResponse>()
    val appointmentListResponse = MutableLiveData<AppointmentListResponse>()
    var partnerAvailabilityResponse: PartnerAvailabilityResponse? = null
    var appointmentResponse: AppointmentResponse? = null
    var appointmentAttachments: ArrayList<Attachment>? = null
      var page = 1
    var selectedTab = MutableLiveData(Enums.AppointmentType.UPCOMING)

    val items: Flow<PagingData<AppointmentResponse>> = Pager(
        config = PagingConfig(pageSize = 100, enablePlaceholders = false),
        pagingSourceFactory = {
            AppointmentListingPagingSource(
                selectedTab.value,
                ApiRepository,
                request = appointmentListRequest
            )
        }
    ).flow.distinctUntilChanged()

    fun getAppointments(page: Int = 1) = liveData {
        emit(ResponseResult.Pending)
        appointmentListRequest.page = page.toString()
        if(appointmentListRequest.appointmentType.isNullOrEmpty())
            appointmentListRequest.appointmentType = getString(R.string.upcoming).lowercase()
        val result = ApiRepository.getAppointments(appointmentListRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getAppointmentsServices() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getAppointmentsServices()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun appointmentsReschedule(request: AppointmentsActionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.appointmentsReschedule(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun appointmentsCompleted(request: AppointmentsActionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.appointmentsCompleted(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun appointmentsReject(request: AppointmentsActionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.appointmentsReject(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun appointmentsAccept(request: AppointmentsActionRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.appointmentsAccept(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetApptDetail(appointmentDetailReq: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getApptDetail(appointmentDetailReq)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun callGetAttachments(appointmentDetailReq: AppointmentDetailReq) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getAttachments(appointmentDetailReq)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun changeStatusApiCall(appointmentStatusRequest: AppointmentStatusRequest) = liveData {
        emit(ResponseResult.Pending)
        val result=ApiRepository.callChangeStatus(appointmentStatusRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun setPartnerAvailability(request: PartnerAvailabilityRequest) = liveData {
        emit(ResponseResult.Pending)
        val result=ApiRepository.setPartnerAvailability(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getPartnerAvailability(request: PartnerAvailabilityRequest) = liveData {
        emit(ResponseResult.Pending)
        val result=ApiRepository.getPartnerAvailability(request)
        emit(result)
        emit(ResponseResult.Complete)
    }
}