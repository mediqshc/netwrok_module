package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddSlotResponse(
    @Json(name = "day_id")
    val dayId: String,
    @Json(name = "end_time")
    val endTime: String,
    @Json(name = "interval_id")
    val intervalId: String,
    @Json(name = "partner_service_id")
    val partnerServiceId: String,
    @Json(name = "rr_log_request_id")
    val rrLogRequestId: String,
    @Json(name = "start_time")
    val startTime: String,
    @Json(name = "user_id")
    val userId: Int
)