package com.fatron.network_module.models.request.offdates


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteOffDatesRequest(
    @Json(name = "off_date_id")
    val offDateId: Int?
)