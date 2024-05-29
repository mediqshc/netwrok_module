package com.fatron.network_module.models.response.walkin.services


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInService(
    @Json(name = "branch_id")
    val branchId: Int? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "service_category")
    val serviceCategory: Int? = null,
    @Json(name = "services")
    val services: GenericItem? = null
)