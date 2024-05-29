package com.fatron.network_module.models.request.orders


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class ScheduledVisitsRequest(
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "page")
    var page: Int? = 1
)