package com.fatron.network_module.models.request.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyCategoriesRequest(
    @Json(name = "category_name")
    val categoryName: String? = null,
    @Json(name = "page")
    var page: Int? = null,
    @Json(name = "city_id")
    val cityId: Int? = null
)