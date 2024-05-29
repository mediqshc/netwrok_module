package com.fatron.network_module.models.request.appointments


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AppointmentListRequest {
    @Json(name = "page")
    var page: String? = "1"
    @Json(name = "appointment_type")
    var appointmentType: String? = null
    @Json(name = "services")
    var services: List<Int>? = null
}