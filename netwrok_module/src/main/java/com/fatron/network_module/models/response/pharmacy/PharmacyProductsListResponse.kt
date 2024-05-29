package com.fatron.network_module.models.response.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PharmacyProductsListResponse(
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "data")
    val products: List<PharmacyProduct>? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null,
)