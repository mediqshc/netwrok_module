package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmrCreateResponse(
    @Json(name = "customer_id")
    val customerId: String?,
    @Json(name = "emr_id")
    val emrId: Int?
)