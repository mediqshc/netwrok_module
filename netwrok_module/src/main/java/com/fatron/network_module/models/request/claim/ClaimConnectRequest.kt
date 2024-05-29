package com.fatron.network_module.models.request.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimConnectRequest(
    @Json(name = "claim_id")
    var claimId: Int? = null,
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "claim_connection_id")
    var claimConnectionId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null
)