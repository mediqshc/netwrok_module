package com.fatron.network_module.models.request.notification


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationReadRequest(
    @Json(name = "notification_id")
    val notificationId: Int = 0,
    @Json(name = "user_id")
    val userId: Int? = null
)


