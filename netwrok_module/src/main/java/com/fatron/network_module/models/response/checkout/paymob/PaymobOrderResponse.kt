package com.fatron.network_module.models.response.checkout.paymob


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymobOrderResponse(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "currency")
    val currency: String? = null,
    @Json(name = "token")
    val token: String? = null,
)