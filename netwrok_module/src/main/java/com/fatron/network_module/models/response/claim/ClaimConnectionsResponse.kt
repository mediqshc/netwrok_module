package com.fatron.network_module.models.response.claim

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimConnectionsResponse(
    @Json(name = "claim_connections")
    var claimConnections: List<ClaimConnection>? = null,
    @Json(name = "connections")
    var walkInConnections: List<ClaimConnection>? = null,
)