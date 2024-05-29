package com.fatron.network_module.models.response.ordersdetails


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Review(
    @Json(name = "rating")
    val rating: Double?,
    @Json(name = "review")
    val review: String?
)