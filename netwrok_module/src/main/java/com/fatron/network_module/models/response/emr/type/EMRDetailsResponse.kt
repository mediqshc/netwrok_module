package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRDetailsResponse(
    @Json(name = "emr_details")
    val emrDetails: EmrDetails?,
    @Json(name = "emr_draft")
    val emrDraft: EmrDetails?
)