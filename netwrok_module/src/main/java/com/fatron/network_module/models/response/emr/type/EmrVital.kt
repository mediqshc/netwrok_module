package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmrVital(
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "key")
    val key: String? = null,
    @Json(name = "unit_id")
    val unitId: Int? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "value")
    val value: String? = null,
    @Json(name = "sort_by")
    val sortBy: Int? = null,
)