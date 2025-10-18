package com.fatron.network_module.models.request.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimRequest(
    @Json(name = "claim_id")
    var claimId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null,
    @Json(name = "city_id")
    var cityId: Int? = null,
    @Json(name = "city_name")
    var cityName: String? = null,
    @Json(name = "country_id")
    var countryId: Int? = null,
    @Json(name = "country_name")
    var countryName: String? = null,
    @Json(name = "family_member_id")
    var familyMemberId: Int? = null,
    @Json(name = "amount")
    var amount: String? = null,
    @Json(name = "service_provider")
    var serviceProvider: String? = null,
    @Json(name = "comments")
    var comments: String? = null,
    @Json(name = "service_id")
    var serviceId: Int? = null,
    @Json(name = "prior_auth_id")
    var priorAuthId: Int? = null,

)