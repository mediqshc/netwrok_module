package com.homemedics.app.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.fatron.network_module.models.request.activedays.ActiveDaysRequest
import com.fatron.network_module.models.request.offdates.DeleteOffDatesRequest
import com.fatron.network_module.models.request.offdates.OffDatesRequest
import com.fatron.network_module.models.request.planschedule.AddSlotRequest
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.offdates.OffDate
import com.fatron.network_module.models.response.offdates.OffDatesSlots
import com.fatron.network_module.models.response.planschedule.WeeklySlots
import com.fatron.network_module.models.response.planschedule.deleteSlotRequest
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.utils.DataCenter

class PlanScheduleViewModel: ViewModel() {

    class ViewModelFactory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlanScheduleViewModel() as T
        }
    }

    fun flushData(){
        selectedDays.clear()
    }

    var offDatesRequest = OffDatesRequest()

    var selectedDays = arrayListOf<GenericItem>()

    var selectedDaysId = arrayListOf<Int>()

    var intervalId: Int = 0

    var offDates = MutableLiveData<ArrayList<OffDate>>(arrayListOf())
    var timeSlots = MutableLiveData<ArrayList<WeeklySlots>>()


    fun getSlotsApiCall()= liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getSlotsApiCall()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addSlotsApiCall(request:AddSlotRequest)= liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addSlotsApiCall(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteSlotsApiCall(request:deleteSlotRequest)= liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteSlotsApiCall(request)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getActiveDays() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getActiveDays()
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addActiveDay(activeDaysRequest: ActiveDaysRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addActiveDays(activeDaysRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun addOffDates(offDatesRequest: OffDatesRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.addOffDates(offDatesRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun deleteOffDate(deleteOffDatesRequest: DeleteOffDatesRequest) = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.deleteOffDates(deleteOffDatesRequest)
        emit(result)
        emit(ResponseResult.Complete)
    }

    fun getOffDates() = liveData {
        emit(ResponseResult.Pending)
        val result = ApiRepository.getOffDates()
        emit(result)
        emit(ResponseResult.Complete)
    }
}