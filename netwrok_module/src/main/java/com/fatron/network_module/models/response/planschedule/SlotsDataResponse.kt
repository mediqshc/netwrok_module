package com.fatron.network_module.models.response.planschedule


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SlotsDataResponse(
    @Json(name = "active_days")
    val activeDays: List<Int>,
    @Json(name = "scheduled_slots")
    val scheduledSlots: List<ScheduledSlot>
)