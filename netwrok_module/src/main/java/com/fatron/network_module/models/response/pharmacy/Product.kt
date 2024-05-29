package com.fatron.network_module.models.response.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Product(
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "discount")
    val discount: Int? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "is_available")
    val isAvailable: Int? = null,
    @Json(name = "is_substitute")
    val isSubstitute: Int? = null,
    @Json(name = "price")
    val price: Double? = null,
    @Json(name = "product")
    val product: PharmacyProduct? = null,
    @Json(name = "product_id")
    val productId: Int? = null,
    @Json(name = "quantity")
    var quantity: Int? = null,
    @Json(name = "subtotal")
    var subtotal: Double? = null
)