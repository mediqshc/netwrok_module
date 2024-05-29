package com.fatron.network_module.models.request.walkin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QRCodeRequest(
    @Json(name = "branch_id")
    val branch_id: Int? = null,
    @Json(name = "id")
    val id: Int? = null
)