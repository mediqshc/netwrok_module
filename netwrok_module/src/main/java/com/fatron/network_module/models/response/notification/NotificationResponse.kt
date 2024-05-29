package com.fatron.network_module.models.response.notification

import com.fatron.network_module.models.request.notification.PropertiesObj
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class NotificationResponse(
    @Json(name = "notifications")
    val notifications: List<Notification>? = null,

    @Json(name = "current_page")
    val currentPage: Int? = null,

    @Json(name = "last_page")
    val lastPage: Int? = null,
    @Json(name = "total")
    val total: Int? = null
)

@JsonClass(generateAdapter = true)
data class Notification(
    @Json(name = "id")
    val id: Int? = null,

    @Json(name = "category_id")
    val categoryID: Int? = null,
    @Json(name = "category")
    val category: String? = null,

    @Json(name = "category_icon")
    var categoryIcon: Int? = null,

    @Json(name = "entity_type")
    val entityType: String? = null,

    @Json(name = "entity_id")
    val entityId: String? = null,

    @Json(name = "type_id")
    val typeID: Int? = null,

    @Json(name = "title")
    val title: String? = null,

    @Json(name = "body")
    val body: String? = null,

    @Json(name = "properties")
    val properties: PropertiesObj? = null,

    @Json(name = "is_read")
    val isRead: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null

)

@JsonClass(generateAdapter = true)
data class NotificationCountResponse(
    @Json(name = "unread_notifications")
    val unreadNotifications: Int? = null
)
