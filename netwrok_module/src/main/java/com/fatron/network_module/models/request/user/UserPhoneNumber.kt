package com.fatron.network_module.models.request.user


import com.fatron.network_module.models.generic.MultipleViewItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserPhoneNumber(
    @Json(name = "country_code")
    val countryCode: String?=null,
    @Json(name = "phone_number")
    val phoneNumber: String?=null,
    @Json(name = "category")
    val category: String?=null,
    @Json(name = "other")
    val other: String?=null
):MultipleViewItem(title = category, desc = countryCode+phoneNumber)