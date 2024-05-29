package com.fatron.network_module.models.response.appointments


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentServicesResponse(
    @Json(name = "services")
    val services: List<Service>?
)