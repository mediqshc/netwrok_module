package com.fatron.network_module.models.response.linkaccount


import com.fatron.network_module.models.response.claim.ReviewExclusion
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompanyServicesResponse(
    @Json(name = "services")
    val services: List<CompaniesService>? = null
)

@JsonClass(generateAdapter = true)
data class CompaniesService(
    @Json(name = "conditions_apply")
    val conditionsApply: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,
    @Json(name = "mode")
    val mode: Int? = null,
    @Json(name = "sub_limit")
    val subLimit: Double? = null,
    @Json(name = "exclusions")
    val exclusions: ReviewExclusion? = null,
    @Json(name = "currency_id")
    val currencyId: Int? = null
)