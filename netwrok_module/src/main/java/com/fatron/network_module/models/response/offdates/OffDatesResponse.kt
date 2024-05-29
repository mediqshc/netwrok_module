package com.fatron.network_module.models.response.offdates

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OffDatesResponse(
    @Json(name = "off_dates")
    val offDates: List<OffDate>?
)