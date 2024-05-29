package com.fatron.network_module.models.response.offdates

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OffDate(
    @Json(name = "end_date")
    val endDate: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "services_ids")
    val servicesIds: List<Int>?,
    @Json(name = "start_date")
    val startDate: String?
)