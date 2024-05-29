package com.fatron.network_module.models.response.meta


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class City(
    @Json(name = "country_id")
    val countryId: Int? = null,
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "is_active")
    val isActive: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null
)