package com.fatron.network_module.models.response.emr.type


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenericMedicalRecord(
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "data")
    val `data`: List<GenericItem>? = null,
    @Json(name = "first_page_url")
    val firstPageUrl: String? = null,
    @Json(name = "from")
    val from: Int? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null,
    @Json(name = "last_page_url")
    val lastPageUrl: String? = null,
    @Json(name = "links")
    val links: List<Link>? = null,
    @Json(name = "next_page_url")
    val nextPageUrl: String? = null,
    @Json(name = "path")
    val path: String? = null,
    @Json(name = "per_page")
    val perPage: Int? = null,
    @Json(name = "prev_page_url")
    val prevPageUrl: String? = null,
    @Json(name = "to")
    val to: Int? = null,
    @Json(name = "total")
    val total: Int? = null
)