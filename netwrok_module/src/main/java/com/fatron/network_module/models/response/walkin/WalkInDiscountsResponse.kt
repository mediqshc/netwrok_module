package com.fatron.network_module.models.response.walkin

import com.fatron.network_module.models.response.walkinpharmacy.WalkInItemResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInDiscountsResponse(
    @Json(name = "discount")
    val discount: WalkInDiscount? = null,
)

@JsonClass(generateAdapter = true)
data class WalkInQRCodeResponse(
    @Json(name = "qr_details")
    val qrDetails: WalkInItemResponse? = null,
)