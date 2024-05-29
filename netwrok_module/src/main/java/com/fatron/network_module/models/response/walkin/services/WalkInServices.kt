package com.fatron.network_module.models.response.walkin.services


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInServices(
    @Json(name = "hospital_services")
    val walkInServices: List<WalkInService>? = null
)