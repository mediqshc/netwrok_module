package com.fatron.network_module.models.response.linkaccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class  DependentResponse(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "age")
    val age: String? = null,
)

data class CityCountry(
    val city: String? = null,
    val country: String? = null
)