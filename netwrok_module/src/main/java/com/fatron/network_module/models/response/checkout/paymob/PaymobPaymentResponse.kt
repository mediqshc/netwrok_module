package com.fatron.network_module.models.response.checkout.paymob


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymobPaymentResponse(
    @Json(name = "iframe")
    val iframe: String? = null,
)