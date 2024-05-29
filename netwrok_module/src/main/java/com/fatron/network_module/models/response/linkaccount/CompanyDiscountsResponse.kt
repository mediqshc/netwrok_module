package com.fatron.network_module.models.response.linkaccount


import com.fatron.network_module.models.response.claim.ReviewExclusion
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompanyDiscountsResponse(
    @Json(name = "discounts")
    val discounts: List<CompaniesDiscounts>? = null
)

@JsonClass(generateAdapter = true)
data class CompaniesDiscounts(
    @Json(name = "conditions_apply")
    val conditionsApply: Int? = null,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,
    @Json(name = "discount_type")
    val discountType: String? = null,
    @Json(name = "exclusions")
    val exclusions: ReviewExclusion? = null,
    @Json(name = "currency_id")
    val currencyId: Int? = null,
    @Json(name = "max_cap")
    val maxCap: Int? = null,
    @Json(name = "min_invoice")
    val minInvoice: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "number_of_transactions")
    val numberOfTransactions: Int? = null,
    @Json(name = "package_name")
    val packageName: String? = null,
    @Json(name = "parnter_service_id")
    val parnterServiceId: Int? = null,
    @Json(name = "promo_code")
    val promoCode: String? = null,
    @Json(name = "reset_after")
    val resetAfter: Int? = null,
    @Json(name = "value")
    val value: Int? = null
)