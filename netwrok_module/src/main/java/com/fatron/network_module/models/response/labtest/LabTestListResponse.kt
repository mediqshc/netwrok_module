package com.fatron.network_module.models.response.labtest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LabTestPaginatedResponse(
    @Json(name = "data")
    val data: List<LabTestResponse>? = null,
)

@JsonClass(generateAdapter = true)
data class LabTestListResponse(
    @Json(name = "lab_tests")
    val labTests: LabTestPaginatedResponse? = null,
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null
)