package com.fatron.network_module.models.response.emr.customer.medicine


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReferredListResponse(
    @Json(name = "referred_by")
    val referredBy: List<GenericItem>? = null,
)