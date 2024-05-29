package com.fatron.network_module.models.response.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDevice(
    @Json(name = "device_token")
    val deviceToken: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "user_id")
    val userId: Int?,
    @Json(name = "voip_token")
    val voipToken: String?
)