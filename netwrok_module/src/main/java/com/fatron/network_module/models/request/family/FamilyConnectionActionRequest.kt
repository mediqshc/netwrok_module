package com.fatron.network_module.models.request.family


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FamilyConnectionActionRequest (
    @Json(name = "relation_id")
    var relationId: String? = null
)