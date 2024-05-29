package com.fatron.network_module.models.response.emr


import com.fatron.network_module.models.request.emr.Vital
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoreEMRResponse(
    @Json(name = "customer_id")
    val customerId: String? = null,
    @Json(name = "emr_id")
    val emrId: String? = null,
    @Json(name = "is_draft")
    val isDraft: Int? = null,
    @Json(name = "rr_log_request_id")
    val rrLogRequestId: String? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "user_type_id")
    val userTypeId: Int? = null,
    @Json(name = "vitals")
    val vitals: List<Vital>? = null,
    @Json(name = "x_local")
    val xLocal: String? = null,
    @Json(name = "x_tenant_id")
    val xTenantId: Int? = null
)