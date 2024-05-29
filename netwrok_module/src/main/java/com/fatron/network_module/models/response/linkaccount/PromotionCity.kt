package com.fatron.network_module.models.response.linkaccount


import com.fatron.network_module.models.response.meta.City
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PromotionCity(
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
    @Json(name = "city")
    val city: City? = null
)