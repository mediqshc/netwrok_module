package com.fatron.network_module.models.request.emr


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Vital(
    @Json(name = "key")
    val key: String? = null,
    @Json(name = "unit")
    val unit: String? = null,
    @Json(name = "value")
    val value: String? = null
)