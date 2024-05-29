package com.fatron.network_module.models.request.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddSlotRequest(
    @Json(name = "day_id")
    val dayId: String,
    @Json(name = "start_time")
    val startTime: String,
    @Json(name = "end_time")
    val endTime: String,
    @Json(name = "interval_id")
    val intervalId: String,
    @Json(name = "partner_service_id")
    val partnerServiceId: String
)