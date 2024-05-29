package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScheduledSlot(
    @Json(name = "day_id")
    val dayId: Int,
    @Json(name = "end_time")
    val endTime: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int,
    @Json(name = "start_time")
    val startTime: String
)