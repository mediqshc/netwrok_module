package com.fatron.network_module.models.request.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoreEMRTypeRequest(
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "emr_id")
    val emrId: String? = null,
    @Json(name = "emr_type_id")
    val emrTypeId: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "type")
    val type: String? = null,
    @Json(name = "type_id")
    val typeId: String? = null
)