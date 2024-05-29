package com.fatron.network_module.models.response.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Services(
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String,
    @Json(name = "offer_service")
    var offerService: Boolean

)

