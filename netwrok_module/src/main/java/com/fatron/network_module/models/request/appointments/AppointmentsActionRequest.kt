package com.fatron.network_module.models.request.appointments


import com.squareup.moshi.Json

data class AppointmentsActionRequest(
    @Json(name = "booking_date")
    val bookingDate: String?,
    @Json(name = "booking_id")
    val bookingId: String?
)