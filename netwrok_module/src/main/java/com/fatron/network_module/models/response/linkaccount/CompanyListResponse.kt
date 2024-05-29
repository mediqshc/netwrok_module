package com.fatron.network_module.models.response.linkaccount


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompanyListResponse(
    @Json(name = "companies")
    val companies: List<GenericItem>? = null,
    @Json(name = "insurances")
    val insurances: List<CompanyResponse>? = null,
    @Json(name = "healthcares")
    val healthcare: List<GenericItem>? = null,
    @Json(name = "insurance_fields")
    val insuranceFields: List<InsuranceFields>? = null
)