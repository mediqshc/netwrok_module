package com.fatron.network_module.models.response.pharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Generic(
    @Json(name = "code")
    val code: String? = null,
    @Json(name = "contra_indications")
    val contraIndications: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "indications")
    val indications: String? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "side_effects")
    val sideEffects: String? = null
)