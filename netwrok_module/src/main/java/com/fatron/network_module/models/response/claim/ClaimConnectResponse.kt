package com.fatron.network_module.models.response.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimConnectResponse(
    @Json(name = "unique_identification_number")
    var uniqueIdentificationNumber: String? = null,
)