package com.fatron.network_module.models.request.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomRequest(
    @Json(name = "room_name")
    val roomName: String?
)