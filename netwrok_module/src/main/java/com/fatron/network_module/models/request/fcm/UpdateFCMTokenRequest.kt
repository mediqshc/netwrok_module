package com.fatron.network_module.models.request.fcm


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdateFCMTokenRequest(
    @Json(name = "fcm_token")
    val fcmToken: String? = null,
    @Json(name = "device_token")
    val deviceToken: String? = null,
)