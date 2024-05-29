package com.fatron.network_module.models.request.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ServiceRequest(
    @Json(name = "services")
    val services: List<Int>
)