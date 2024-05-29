package com.fatron.network_module.models.request.activedays


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActiveDaysRequest(
    @Json(name = "active_days")
    val activeDays: List<Int>?,
    @Json(name = "interval_id")
    val intervalId: Int,
    @Json(name = "force_delete")
    var forceDelete: Int?=0
)