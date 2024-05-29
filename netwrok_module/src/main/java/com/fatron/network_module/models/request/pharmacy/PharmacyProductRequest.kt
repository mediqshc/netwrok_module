package com.fatron.network_module.models.request.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyProductRequest(
    @Json(name = "category_id")
    val categoryId: Int? = null,
    @Json(name = "display_name")
    val displayName: String? = null,
    @Json(name = "over_the_counter")
    val overTheCounter: Int? = null,
    @Json(name = "page")
    val page: Int? = null,
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "country_id")
    val countryId: Int? = null
)