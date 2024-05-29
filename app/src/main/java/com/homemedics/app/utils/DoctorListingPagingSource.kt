package com.homemedics.app.utils

import androidx.appcompat.app.AlertDialog
import androidx.paging.PagingSource
import com.bumptech.glide.load.HttpException
import com.fatron.network_module.models.request.bdc.BDCFilterRequest
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.bdc.BDCFilterResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.repository.ResponseResult
import com.homemedics.app.R
import java.io.IOException

class DoctorListingPagingSource(val apiService: ApiRepository, val request: BDCFilterRequest) :
    PagingSource<Int, PartnerProfileResponse>() {
    val DEFAULT_PAGE_INDEX = 1
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PartnerProfileResponse> {
        //for first case it will be null, then we can pass some default value, in our case it's 1

        val page = params.key ?: DEFAULT_PAGE_INDEX
        return try {
            request.page = page.toString()
            val response = apiService.getDoctors(request)
            var data:ResponseGeneral<BDCFilterResponse>?=null
            var partners = arrayListOf<PartnerProfileResponse>()
            if (isOnline(getAppContext())) {
                when (response) {
                    is ResponseResult.Success -> {

                        data = response.data as ResponseGeneral<BDCFilterResponse>
                        partners = data.data?.partners as ArrayList<PartnerProfileResponse>


                    }
                    is ResponseResult.Failure -> {

                    }
                    is ResponseResult.Pending -> {}
                    is ResponseResult.ApiError -> {}
                    is ResponseResult.Complete -> {}
                }
            }

            LoadResult.Page(
                partners, prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1,
                nextKey = if (data?.data?.lastPage == page) null else page + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }


}