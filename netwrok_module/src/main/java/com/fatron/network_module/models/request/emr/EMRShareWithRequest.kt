package com.fatron.network_module.models.request.emr


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRShareWithRequest(
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "share_with")
    val shareWith: List<Int>? = null
)