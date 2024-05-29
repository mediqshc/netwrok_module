package com.fatron.network_module.models.request.notification

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class NotificationRequest (
    @Json(name = "category_id")
    var categoryId: Int? = null,
    @Json(name = "page")
    var page: Int? = 1
)