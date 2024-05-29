package com.fatron.network_module.models.request.walkin


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInConnectionRequest(
    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "filter")
    val filter: Int? = null,
    @Json(name = "service_id")
    val serviceId: Int? = null
)