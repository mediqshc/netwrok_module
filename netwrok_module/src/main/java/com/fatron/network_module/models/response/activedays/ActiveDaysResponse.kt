package com.fatron.network_module.models.response.activedays


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActiveDaysResponse(
    @Json(name = "active_days")
    val activeDays: List<Int>?,
    @Json(name = "interval_id")
    val intervalId: Int? = null
)