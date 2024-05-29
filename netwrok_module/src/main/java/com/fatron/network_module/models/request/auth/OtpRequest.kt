package com.fatron.network_module.models.request.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpRequest(
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "phone_number")
    val phoneNumber: String
)

@JsonClass(generateAdapter = true)
data class DeleteAccountRequest(
    @Json(name = "user_account_id")
    val userAccountId: Int? = null
)

data class EconOtpRequest(
    @Json(name = "network")
    val network: String,
    @Json(name = "phone")
    val phone: String
)

data class EconSubscribeRequest(
    @Json(name = "network")
    val network: String,
    @Json(name = "phone")
    val phone: String,
    @Json(name = "otp")
    val otp: String,
    @Json(name = "package_id")
    val packageId: String?
)

