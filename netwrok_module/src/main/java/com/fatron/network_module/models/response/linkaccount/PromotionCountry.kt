package com.fatron.network_module.models.response.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PromotionCountry(
    @Json(name = "country_id")
    val countryId: Int?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "promotion_id")
    val promotionId: Int?
)