package com.fatron.network_module.models.request.user


import com.fatron.network_module.models.generic.MultipleViewItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserLocation(
    @Json(name = "address")
    val address: String?=null,
    @Json(name = "category")
    val category: String?=null,
    @Json(name = "floor_unit")
    val floorUnit: String?=null,
    @Json(name = "lat")
    val lat: String?=null,
    @Json(name = "long")
    val long: String?=null,
    @Json(name = "other")
    val other: String?=null,
    @Json(name = "street")
    val street: String?=null,
    @Json(name = "region")
    val region: String?=null,
    @Json(name = "user_id")
    val userId: String?=null,
    @Json(name = "id")
    val id: Int?=null,
    @Json(name = "sublocality")
    val sublocality: String?=null

):MultipleViewItem(title = category, desc = address)