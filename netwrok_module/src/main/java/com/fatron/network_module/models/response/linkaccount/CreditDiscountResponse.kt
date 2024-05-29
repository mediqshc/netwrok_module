package com.fatron.network_module.models.response.linkaccount

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class  CreditDiscountResponse(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    var itemName: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "label")
    val label: String? = null,

)