package com.fatron.network_module.models.response.pharmacy


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyCartResponse(
    @Json(name = "booking_id")
    val bookingId: Int? = null
)