package com.fatron.network_module.models.request.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompanyRequest(
    @Json(name = "package_id")
    val packageId: Int? = null,
    @Json(name = "home_page")
    val homePage: Int? = null,
    @Json(name = "x_tenant_currency")
    val x_tenant_currency: String? = "1"
)