package com.fatron.network_module.models.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentStatusResponse(
    @Json(name = "appointment_status_changed")
    val appointmentStatusChanged: Boolean
)