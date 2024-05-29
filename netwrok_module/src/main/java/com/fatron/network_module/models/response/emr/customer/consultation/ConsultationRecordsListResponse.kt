package com.fatron.network_module.models.response.emr.customer.consultation


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConsultationRecordsListResponse(
    @Json(name = "consultation_records")
    var consultationRecords: List<ConsultationRecordsResponse>? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null,
    @Json(name = "current_page")
    val currentPage: Int? = null,
)