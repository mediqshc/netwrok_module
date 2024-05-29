package com.fatron.network_module.models.response.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RejectionReason(
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "reason")
    val reason: String? = null
)