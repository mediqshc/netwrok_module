package com.fatron.network_module.models.request.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteLinkRequest(
    @Json(name = "company_id")
    val companyId: String? = null,
    @Json(name = "insurance_id")
    val insuranceId: String? = null,
    @Json(name = "healthcare_id")
    val healthcareId: String? = null,
)