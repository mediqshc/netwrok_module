package com.fatron.network_module.models.request.checkout.paymob


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PaymobPaymentStatusRequest(
    @Json(name = "amount_cents")
    var amountCents: Int?=null,
    @Json(name = "booking_id")
    var bookingId: Int?=null,
    @Json(name = "currency")
    var currency: String?=null,
    @Json(name = "integration_id")
    var integrationId: Int?=null,
    @Json(name = "response")
    var response: String?=null,
    @Json(name = "status")
    var status: Int?=null,
    @Json(name = "request_id")
    var requestId: Int?=null,
    @Json(name = "currency_id")
    var currencyId: Int?=null,
    @Json(name = "payment_method_id")
    var paymentMethodId: Int?=null,
    @Json(name = "promotion_id")
    var promotionId: Int? = null,
    @Json(name = "discount")
    var discount: String? = null,
)