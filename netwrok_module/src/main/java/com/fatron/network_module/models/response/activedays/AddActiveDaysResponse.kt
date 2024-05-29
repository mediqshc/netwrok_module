package com.fatron.network_module.models.response.activedays


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddActiveDaysResponse(
    @Json(name = "active_days")
    val activeDays: List<Int>?,
    @Json(name = "user_id")
    val userId: Int?,
    @Json(name = "interval_id")
    val intervalId: Int?,
    @Json(name = "force_delete")
    val forceDelete: Int?,
    @Json(name = "type")
    val type: String?,
    @Json(name = "x_local")
    val x_local: String?,
    @Json(name = "x_tenant_id")
    val xTenantId: Int?,
    @Json(name = "user_type_id")
    val userTypeId: Int?,
    @Json(name = "slotExists")
    val slotExists: Int

)