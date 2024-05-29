package com.fatron.network_module.models.response.meta

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SlotIntervals(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String?=null,
    @Json(name = "value")
    val value: Int? = null
)