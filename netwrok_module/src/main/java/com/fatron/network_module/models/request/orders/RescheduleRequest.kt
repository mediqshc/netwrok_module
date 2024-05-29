package com.fatron.network_module.models.request.orders


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RescheduleRequest(
    @Json(name = "booking_id")
    val bookingId: Int?,
    @Json(name = "booking_date")
    val date: String?,
    @Json(name = "end_time")
    val endTime: String?,
    @Json(name = "shift_id")
    val shiftId: Int?=null,
    @Json(name = "start_time")
    val startTime: String?
)