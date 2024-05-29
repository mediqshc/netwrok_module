package com.fatron.network_module.models.response.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimResponse(
    @Json(name = "claim")
    var claim: Claim? = null,

    //for details screen
    @Json(name = "details")
    var details: Claim? = null,
)