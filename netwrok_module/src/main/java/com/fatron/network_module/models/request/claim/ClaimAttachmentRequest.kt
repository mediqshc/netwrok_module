package com.fatron.network_module.models.request.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimAttachmentRequest(
    @Json(name = "claim_id")
    var claimId: Int? = null,
)