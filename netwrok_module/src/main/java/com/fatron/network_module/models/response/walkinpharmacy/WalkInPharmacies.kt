package com.fatron.network_module.models.response.walkinpharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInPharmacies(
    @Json(name = "data")
    val walkInList: List<WalkInItemResponse>? = null,
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null
)