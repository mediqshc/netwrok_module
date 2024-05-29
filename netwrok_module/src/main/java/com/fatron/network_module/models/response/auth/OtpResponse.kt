package com.fatron.network_module.models.response.auth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
 data class OtpResponse(
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "expired_at")
    val expiredAt: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "otp")
    val otp: String,
    @Json(name = "phone_number")
    val phoneNumber: String,
    @Json(name = "updated_at")
    val updatedAt: String
)