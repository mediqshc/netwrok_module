package com.fatron.network_module.models.request.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkInsuranceRequest(
    @Json(name = "insurance_id")
    val insuranceId: String? = null,
    @Json(name = "policy")
    val policy: String = "",
    @Json(name = "certid")
    val certId: String = "",
    @Json(name = "patient_name")
    val patientName: String = ""
)