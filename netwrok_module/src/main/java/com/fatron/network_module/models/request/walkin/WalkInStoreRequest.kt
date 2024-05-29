package com.fatron.network_module.models.request.walkin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInStoreRequest(
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "amount")
    val amount: Double? = null,
    @Json(name = "connection_id")
    val connectionId: Int? = null,
    @Json(name = "family_member_id")
    var familyMemberId: Int? = null,
    @Json(name = "pharmacy_id")
    val pharmacyId: Int? = null,
    @Json(name = "walk_in_pharmacy_id")
    val walkInPharmacyId: Int? = null,
    @Json(name = "lab_id")
    val laboratoryId: Int? = null,
    @Json(name = "walk_in_laboratory_id")
    val walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_hospital_id")
    val walkInHospitalId: Int? = null,
    @Json(name = "healthcare_id")
    val healthCareId: Int? = null,
    @Json(name = "service_id")
    val serviceId: Int? = null,
    @Json(name = "comments")
    val comments: String? = null
)