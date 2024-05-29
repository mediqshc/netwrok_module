package com.fatron.network_module.models.request.orders


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MyOrdersRequest(
    @Json(name = "booking_id")
    val bookingId: Int?,
    @Json(name = "rating")
    val rating: Double?,
    @Json(name = "review")
    val review: String?,
    @Json(name = "duty_id")
    val dutyId: Int? = null,
)