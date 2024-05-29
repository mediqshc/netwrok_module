package com.fatron.network_module.models.request.checkout


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkedCredit(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "amount")
    val amount: String?,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "icon_url")
    val iconUrl: String? = null,

    val promoCode: String? = null,
    var isPromo: Boolean? = false,
    var discount: String? = null
)