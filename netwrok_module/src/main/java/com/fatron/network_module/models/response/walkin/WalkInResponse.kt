package com.fatron.network_module.models.response.walkin


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInResponse(
    @Json(name = "details")
    var details: WalkIn? = null,
)