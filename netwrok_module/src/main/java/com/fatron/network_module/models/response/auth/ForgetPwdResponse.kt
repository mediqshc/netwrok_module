package com.fatron.network_module.models.response.auth


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForgetPwdResponse(
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "password")
    val password: String,
    @Json(name = "password_confirmation")
    val passwordConfirmation: String,
    @Json(name = "phone_number")
    val phoneNumber: String,
    @Json(name = "rr_log_request_id")
    val rrLogRequestId: String,
    @Json(name = "user_id")
    val userId: Int
)