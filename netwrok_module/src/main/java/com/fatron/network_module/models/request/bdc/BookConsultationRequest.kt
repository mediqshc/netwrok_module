package com.fatron.network_module.models.request.bdc


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BookConsultationRequest {
    @Json(name = "booking_id")
    var bookingId: Int? = null

    @Json(name = "patient_id")
    var patientId: Int? = null

    @Json(name = "booking_date")
    var bookingDate: String? = null

    @Json(name = "start_time")
    var startTime: String? = null

    @Json(name = "shift_id")
    var shiftId: Int? = null

    @Json(name = "end_time")
    var endTime: String? = null

    @Json(name = "user_location_id")
    var userLocationId: Int? = null

    @Json(name = "instructions")
    var instructions: String? = null

    @Json(name = "fee")
    var fee: String? = null

    @Json(name = "service_id")
    var serviceId: Int? = null

    @Json(name = "speciality_id")
    var specialityId: Int? = null

    @Json(name = "home_collection")
    var homeCollection: Int? = null

    @Json(name = "branch_id")
    var preferredLaboratory: Int? = null

    @Json(name = "prescription_flow")
    var prescriptionFlow: Int? = null

    @Json(name = "book_available_now")
    var bookAvailableNow: Int? = null
}