package com.fatron.network_module.models.request


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentStatusRequest(
    @Json(name = "action")
    val action: String?=null,
    @Json(name = "booking_completed_reasons_id")
    val bookingCompletedReasonsId: String?=null,
    @Json(name = "booking_id")
    val bookingId: String?=null,
    @Json(name = "fee_collected")
    val feeCollected: Int?=null,
    @Json(name = "followup_date")
    val followupDate: String?=null,
    @Json(name = "journey_time_id")
    val journeyTimeId: String?=null,
    @Json(name = "duty_id")
    val dutyId: Int? = null
)