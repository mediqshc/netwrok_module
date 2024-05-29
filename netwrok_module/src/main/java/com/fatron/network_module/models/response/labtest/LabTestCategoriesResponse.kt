package com.fatron.network_module.models.response.labtest

import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LabTestCategoriesResponse(
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "data")
    val data: List<LabResponse>? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null
)