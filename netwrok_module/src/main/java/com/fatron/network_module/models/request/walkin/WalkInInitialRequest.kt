package com.fatron.network_module.models.request.walkin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInInitialRequest(
    @Json(name = "pharmacy_id")
    val pharmacyId: Int? = null,
    @Json(name = "lab_id")
    val labId: Int? = null,
    @Json(name = "healthcare_id")
    val healthcareId: Int? = null,
    @Json(name = "family_member_id")
    val familyMemberId: Int? = null
)