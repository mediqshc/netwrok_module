package com.homemedics.app.utils

import androidx.paging.PagingSource
import com.bumptech.glide.load.HttpException
import com.fatron.network_module.models.request.emr.customer.records.EMRRecordsFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordsListResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import java.io.IOException

class CustomerEMRRecordPagingSource(val apiService: ApiRepository, val request: EMRRecordsFilterRequest) :
    PagingSource<Int, CustomerRecordResponse>() {
    val DEFAULT_PAGE_INDEX = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CustomerRecordResponse> {

        val page = params.key ?: DEFAULT_PAGE_INDEX
        return try {
            request.page = page
            val response = apiService.getCustomerEMRRecords(request)
            var data:ResponseGeneral<CustomerRecordsListResponse>?=null
            var itemsList = arrayListOf<CustomerRecordResponse>()
            if (isOnline(getAppContext())) {
                when (response) {
                    is ResponseResult.Success -> {
                        data = response.data as ResponseGeneral<CustomerRecordsListResponse>
                        itemsList = data.data?.records as ArrayList<CustomerRecordResponse>
                    }
                    is ResponseResult.Failure -> {

                    }
                    is ResponseResult.Pending -> {}
                    is ResponseResult.ApiError -> {}
                    is ResponseResult.Complete -> {}
                }
            }

            LoadResult.Page(
                itemsList, prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1,
                nextKey = if (data?.data?.lastPage == page) null else page + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }


}