package com.fatron.network_module.models.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteSlotsResponse(
    @Json(name = "slotExists")
    val slotExists: Int
)