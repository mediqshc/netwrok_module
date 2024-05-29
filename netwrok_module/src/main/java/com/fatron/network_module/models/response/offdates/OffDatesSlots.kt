package com.fatron.network_module.models.response.offdates

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OffDatesSlots(
    @Json(name = "video_call")
    val videoCall: String? = null,
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "home_visit")
    val homeVisit: String? = null,
    @Json(name = "end_time")
    val endTime: String? = null,
    @Json(name = "start_time")
    val startTime: String? = null
)