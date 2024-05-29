package com.fatron.network_module.models.request.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyProductDetailRequest(
    @Json(name = "product_id")
    val productId: Int? = null,
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "country_id")
    val countryId: Int? = null
)