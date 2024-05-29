package com.fatron.network_module.models.request.walkin


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInListRequest(
    @Json(name = "latitude")
    val latitude: Double? = null,
    @Json(name = "longitude")
    val longitude: Double? = null,
    @Json(name = "search")
    val search: String? = null,
    @Json(name = "discount_center")
    val discountCenter: Int? = null,
    @Json(name = "center_id")
    val centerId: Int? = null,
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "service")
    val service: Int? = null,
    @Json(name = "package")
    val filterPackage: Int? = null,
    @Json(name = "page")
    val page: Int? = null
)