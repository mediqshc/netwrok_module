package com.fatron.network_module.models.request.pharmacy


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyCartRequest(
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "price")
    var price: String? = null,
    @Json(name = "product_id")
    var productId: Int? = null,
    @Json(name = "quantity")
    var quantity: Int? = null
)