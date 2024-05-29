package com.fatron.network_module.models.request.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SymptomsRequest(
    @Json(name = "customer_id")
    val customerId: String? = null,
    @Json(name = "partner_service_id")
    val partnerServiceId: String? = null
)