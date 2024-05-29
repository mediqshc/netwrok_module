package com.fatron.network_module.models.request.chat


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TwilioTokenRequest(
    @Json(name = "device_type")
    val deviceType: String,
    @Json(name = "participant_id")
    val participantId: Int
)