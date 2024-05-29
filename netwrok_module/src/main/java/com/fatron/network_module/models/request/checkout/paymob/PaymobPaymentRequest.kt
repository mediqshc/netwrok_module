package com.fatron.network_module.models.request.checkout.paymob


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymobPaymentRequest(
    @Json(name = "response")
    var response: String? = null,
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "order_id")
    var orderId: Int? = null,
    @Json(name = "status")
    var status: Int? = null,
    @Json(name = "integration_id")
    var integrationId: Int? = null,
    @Json(name = "amount_cents")
    var amountCents: String? = null,
    @Json(name = "currency")
    var currency: String? = null,
    @Json(name = "auth_token")
    var authToken: String? = null,
)