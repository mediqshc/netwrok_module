package com.fatron.network_module.models.response.email


import com.squareup.moshi.Json

data class VerifyEmailResponse(
    @Json(name = "email")
    val email: String? = null,
    @Json(name = "otp")
    val otp: String? = null,
    @Json(name = "code")
    val code: String? = null
)


data class HospitalDiscountCenter(
    @Json(name = "email")
    val email: String? = null,

)