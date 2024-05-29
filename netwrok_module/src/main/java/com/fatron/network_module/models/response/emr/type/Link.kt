package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Link(
    @Json(name = "active")
    val active: Boolean? = null,
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "url")
    val url: String? = null
)