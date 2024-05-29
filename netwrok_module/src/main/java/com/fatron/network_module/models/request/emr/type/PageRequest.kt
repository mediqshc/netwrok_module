package com.fatron.network_module.models.request.emr.type

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PageRequest(
    @Json(name = "page")
    val page: Int? = null,
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "name")
    var displayName: String? = null,
)