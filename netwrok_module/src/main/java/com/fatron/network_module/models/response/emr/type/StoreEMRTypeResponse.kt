package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoreEMRTypeResponse(
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "emr_id")
    val emrId: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "rr_log_request_id")
    val rrLogRequestId: String? = null,
    @Json(name = "type")
    val type: String? = null,
    @Json(name = "type_id")
    val typeId: String? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "user_type_id")
    val userTypeId: Int? = null,
    @Json(name = "x_local")
    val xLocal: String? = null,
    @Json(name = "x_tenant_id")
    val xTenantId: Int? = null
)