package com.fatron.network_module.models.response.pharmacy

import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyCategoriesResponse(
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "data")
    val data: List<GenericItem>? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null
)