package com.fatron.network_module.models.response.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DateSlotResponse(
    @Json(name = "timestamp")
    val timestamp: String? = null,
    @Json(name = "day")
    val day: String? = null,
    @Json(name = "date")
    val date: String? = null,
    @Json(name = "is_active")
    val isActive: Boolean? = false,
    @Json(name = "slots")
    val slots: List<List<TimeSlot>?>? = null,
){
    var isChecked: Boolean = false
}

@JsonClass(generateAdapter = true)
data class TimeSlot(
    @Json(name = "shift_id")
    val shiftId: Int? = null,
    @Json(name = "shift")
    val shift: String? = null,
    @Json(name = "start")
    val start: String? = null,
    @Json(name = "end")
    val end: String? = null,
){
    var isChecked = false
    var isAvailableNowSlot = false
}