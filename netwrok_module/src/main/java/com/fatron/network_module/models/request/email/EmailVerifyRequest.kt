package com.fatron.network_module.models.request.email


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmailVerifyRequest(
    @Json(name = "email")
    val email: String? = null,
    @Json(name = "otp")
    val otp: String? = null,
    @Json(name = "code")
    val code: String? = null
)