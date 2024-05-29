package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TimeSlots(
    @Json(name = "id")
    val id: Int?=0,
    @Json(name = "title")
    val title: String,

)