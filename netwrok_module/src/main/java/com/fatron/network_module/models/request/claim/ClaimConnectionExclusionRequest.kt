package com.fatron.network_module.models.request.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimConnectionExclusionRequest(
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "connection_id")
    var connectionId: Int? = null,
    @Json(name = "partner_service_id")
    var partnerServiceId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null,
    @Json(name = "package_id")
    var packageId: Int? = null,
)