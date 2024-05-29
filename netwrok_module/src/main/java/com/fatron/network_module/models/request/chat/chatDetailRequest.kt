package com.fatron.network_module.models.request.chat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class chatDetailRequest(
    @Json(name = "channel_sid")
    val channelSid: String?
)
