package com.homemedics.app.utils

import androidx.paging.PagingSource
import com.bumptech.glide.load.HttpException
import com.fatron.network_module.models.request.appointments.AppointmentListRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.appointments.AppointmentListResponse
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import java.io.IOException

class AppointmentListingPagingSource(val appointmentType: Enums.AppointmentType?, val apiService: ApiRepository, val request: AppointmentListRequest) :
    PagingSource<Int, AppointmentResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AppointmentResponse> {
        //for first case it will be null, then we can pass some default value, in our case it's 1
        val DEFAULT_PAGE_INDEX = 1
        val page = params.key ?: DEFAULT_PAGE_INDEX
        return try {
            request.page = page.toString()
            val response = apiService.getAppointments(request)
            var data:ResponseGeneral<AppointmentListResponse>?=null
            val appointments = AppointmentListResponse()
            var appointmentsList = ArrayList<AppointmentResponse>()
                when (response) {
                is ResponseResult.Success -> {
                    data = response.data as ResponseGeneral<AppointmentListResponse>
                    data.let { appointmentList ->
                        appointmentsList =
                            when{
                                appointmentType == Enums.AppointmentType.UPCOMING ->
                                    appointmentList.data?.upcoming?.data as ArrayList<AppointmentResponse>
                                appointmentType == Enums.AppointmentType.UNREAD ->
                                    appointmentList.data?.unread?.data as ArrayList<AppointmentResponse>
                                appointmentType == Enums.AppointmentType.HISTORY ->
                                    appointmentList.data?.history?.data as ArrayList<AppointmentResponse>
                                else ->
                                    appointmentList.data?.upcoming?.data as ArrayList<AppointmentResponse>
                            }
                    }

                }
                is ResponseResult.Failure -> {

                }
                is ResponseResult.Pending -> {}
                is ResponseResult.ApiError -> {}
                is ResponseResult.Complete -> {}
            }
            LoadResult.Page(
                appointmentsList, prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1,
                nextKey = if (data?.data?.upcoming?.lastPage == page) null else page + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }


}