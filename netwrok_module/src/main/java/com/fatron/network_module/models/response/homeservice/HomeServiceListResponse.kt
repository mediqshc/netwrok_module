package com.fatron.network_module.models.response.homeservice


import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.Services
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeServiceListResponse(
    @Json(name = "services")
    val services: List<GenericItem>
)
