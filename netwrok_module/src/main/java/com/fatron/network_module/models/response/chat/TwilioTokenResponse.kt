package com.fatron.network_module.models.response.chat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwilioTokenResponse(
    @Json(name = "token")
    val token: String
)