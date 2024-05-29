package com.fatron.network_module.models.response.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompanyTermsAndConditionsResponse(
    @Json(name = "terms_and_conditions")
    val termsAndConditions: CompaniesTermsAndConditions? = null
)

@JsonClass(generateAdapter = true)
data class CompaniesTermsAndConditions(
    @Json(name = "action_type_id")
    val actionTypeId: Any? = null,
    @Json(name = "end_date")
    val endDate: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "number_of_action")
    val numberOfAction: Any? = null,
    @Json(name = "number_of_transaction")
    val numberOfTransaction: Any? = null,
    @Json(name = "promocode")
    val promocode: Any? = null,
    @Json(name = "promotion_ages")
    val promotionAges: List<PromotionAge>? = null,
    @Json(name = "promotion_cities")
    val promotionCity: List<PromotionCity>? = null,
    @Json(name = "promotion_countries")
    val promotionCountry: List<PromotionCountry>? = null,
    @Json(name = "promotion_type_id")
    val promotionTypeId: Int? = null,
    @Json(name = "reset_frequency")
    val resetFrequency: Any? = null,
    @Json(name = "start_date")
    val startDate: String? = null,
    @Json(name = "terms_and_conditions")
    val termsAndConditions: String? = null,
    @Json(name = "valid_days")
    val validDays: Any? = null
)