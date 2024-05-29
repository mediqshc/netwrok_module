package com.fatron.network_module.models.response.meta


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Keys(
    @Json(name = "google_map_key")
    val googleMapKey: String?
)