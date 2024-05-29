package com.fatron.network_module.models.request.emr.type

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRTypeDeleteRequest(
    @Json(name = "emr_type_id")
    val emrTypeId: Int? = null,
    @Json(name = "type")
    val type: Int? = null
)