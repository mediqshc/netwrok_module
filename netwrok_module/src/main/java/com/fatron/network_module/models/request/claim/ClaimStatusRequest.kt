package com.fatron.network_module.models.request.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimStatusRequest(
    @Json(name = "claim_id")
    var claimId: Int? = null,
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "status_id")
    var statusId: Int? = null,
    @Json(name = "walk_in_pharmacy_id")
    var walkInPharmacyId: Int? = null,
    @Json(name = "walk_in_laboratory_id")
    var walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_hospital_id")
    var walkInHospitalId: Int? = null,
)