package com.fatron.network_module.models.response.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoTokenResponse(
    @Json(name = "order")
    val order: Order?,
    @Json(name = "room_name")
    val roomName: String?,
    @Json(name = "sid")
    val sid: String?,
    @Json(name = "token")
    val token: String?
)