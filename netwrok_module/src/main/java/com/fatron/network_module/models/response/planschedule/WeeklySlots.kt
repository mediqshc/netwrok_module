package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeeklySlots(
    @Json(name = "title")
    var title: String,
    @Json(name = "slots")
    val slots: List<SlotByType>? = arrayListOf()
)