package com.fatron.network_module.models.response.family


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FamilyResponse (
    @Json(name = "connected")
    var connected: List<FamilyConnection>? = arrayListOf(),

    @Json(name = "sent")
    var sent: List<FamilyConnection>? = arrayListOf(),

    @Json(name = "recieved")
    var recieved: List<FamilyConnection>? = arrayListOf(),
)