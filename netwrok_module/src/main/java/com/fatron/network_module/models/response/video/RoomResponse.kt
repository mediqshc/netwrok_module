package com.fatron.network_module.models.response.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RoomResponse(
    @Json(name = "room_exists")
    val roomExists: Boolean?
)