package com.fatron.network_module.models.request.emr.type


import com.fatron.network_module.models.request.emr.Vital
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRDetailsRequest(
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "type")
    val type: Int? = null,
    @Json(name = "vitals")
    val vitals: List<Vital>? = null,
    @Json(name = "date")
    val date: String? = null,
    @Json(name = "modify")
    val modify: Int? = null,
)