package com.fatron.network_module.models.response.aiPrescription

import com.squareup.moshi.Json

data class AiPrescription(
    @Json(name = "text")
    val text:String,
    @Json(name = "audio_url")
    val audio_url:String
)
data class ORCRequest(
    @Json(name = "image_url")
    val image_url: String,

    )