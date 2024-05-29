package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRTypeEditResponse(
    @Json(name = "description")
    val description: String?,
    @Json(name = "emr_type_id")
    val emrTypeId: String?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "rr_log_request_id")
    val rrLogRequestId: String?,
    @Json(name = "type")
    val type: String?,
    @Json(name = "user_id")
    val userId: Int?,
    @Json(name = "user_type_id")
    val userTypeId: Int?,
    @Json(name = "x_local")
    val xLocal: String?,
    @Json(name = "x_tenant_id")
    val xTenantId: Int?
)