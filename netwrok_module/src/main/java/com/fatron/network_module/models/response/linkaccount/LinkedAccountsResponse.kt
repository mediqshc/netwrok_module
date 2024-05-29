package com.fatron.network_module.models.response.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LinkedAccountsResponse(
    @Json(name = "companies")
    val companies: List<CompanyResponse>? = arrayListOf(),
    @Json(name = "insurances")
    val insurances: List<CompanyResponse>? = arrayListOf(),
    @Json(name = "healthcares")
    val healthcares: List<CompanyResponse>? = arrayListOf(),
)