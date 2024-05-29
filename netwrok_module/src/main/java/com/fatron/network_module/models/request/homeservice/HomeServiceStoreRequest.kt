package com.fatron.network_module.models.request.homeservice


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeServiceStoreRequest(
    @Json(name = "booking_id")
    var bookingId: Int?=null,
    @Json(name = "instructions")
    var instructions: String?=null,
    @Json(name = "patient_id")
    var patientId: Int?=null,
    @Json(name = "user_location_id")
    var userLocationId: Int?=null,
    @Json(name = "visit_type")
    var visitType: Int?=null
)