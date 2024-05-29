package com.fatron.network_module.models.response.meta


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Country(
    @Json(name = "id")
    val id: Int?=null,
    @Json(name = "is_default")
    val isDefault: Int?=0,
    @Json(name = "name")
    val name: String?=null,
    @Json(name = "phone_no_limit")
    val phoneNoLimit: Int?=16,
    @Json(name = "short_name")
    val shortName: String?=null,
    @Json(name = "country_code")
    val phoneCode: String?=null,
    @Json(name = "number_starts_with")
    val phoneNoInitial: Int? = null
)