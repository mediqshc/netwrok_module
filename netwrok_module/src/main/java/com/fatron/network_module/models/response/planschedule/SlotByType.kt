package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SlotByType(
    @Json(name = "title")
    val title: String,
    @Json(name = "slots")
    val slots: List<TimeSlots>? = arrayListOf(),
    @Json(name="drawable")
    val drawable:Int?=null

)