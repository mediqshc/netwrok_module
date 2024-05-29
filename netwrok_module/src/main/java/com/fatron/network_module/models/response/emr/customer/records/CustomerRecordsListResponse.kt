package com.fatron.network_module.models.response.emr.customer.records


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerRecordsListResponse(
    @Json(name = "records")
    var records: List<CustomerRecordResponse>? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null,
    @Json(name = "current_page")
    val currentPage: Int? = null,
)