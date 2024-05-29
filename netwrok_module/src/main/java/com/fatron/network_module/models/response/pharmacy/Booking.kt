package com.fatron.network_module.models.response.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Booking(
    @Json(name = "booked_for_id")
    val bookedForId: Int?,
    @Json(name = "booking_status_id")
    val bookingStatusId: Int?,
    @Json(name = "currency_id")
    val currencyId: Int?,
    @Json(name = "fee")
    val fee: Any?,
    @Json(name = "instructions")
    val instructions: Any?,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int?,
    @Json(name = "unique_identification_number")
    val uniqueIdentificationNumber: String?,
    @Json(name = "user_id")
    val userId: Int?,
    @Json(name = "user_location_id")
    val userLocationId: Any?
)

@JsonClass(generateAdapter = true)
data class HospitalDiscountCenterRequest(
    @Json(name="healthcare_id")
    val healthcare_id:Int?=null,
    @Json(name="lab_id")
    val lab_id:Int?=null



)
