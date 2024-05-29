package com.fatron.network_module.models.response.emr


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRDownloadResponse(
    @Json(name = "reports")
    val reports: List<String>? = null,
)