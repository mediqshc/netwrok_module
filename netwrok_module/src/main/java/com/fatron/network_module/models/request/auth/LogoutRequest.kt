package com.fatron.network_module.models.request.auth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LogoutRequest(
    @Json(name = "device_token")
    var deviceToken: String? = null
)