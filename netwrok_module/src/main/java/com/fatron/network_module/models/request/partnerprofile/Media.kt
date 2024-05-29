package com.fatron.network_module.models.request.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Media(
    @Json(name = "file")
    val `file`: String,
    @Json(name = "id")
    val id: Int
): java.io.Serializable