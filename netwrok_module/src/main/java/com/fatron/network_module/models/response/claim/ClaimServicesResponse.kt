package com.fatron.network_module.models.response.claim


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimServicesResponse(
    @Json(name = "claim_services")
    var claimServices: List<GenericItem>? = null,
)