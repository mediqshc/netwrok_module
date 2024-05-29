package com.fatron.network_module.models.request.email


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmailSendRequest(
    @Json(name = "email")
    val email: String? = null
)