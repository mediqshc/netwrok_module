package com.fatron.network_module.models.response.walkin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInDiscount(
    @Json(name = "icon_url")
    val iconUrl: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "kilometer")
    val kilometer: Double? = null,
    @Json(name = "latitude")
    val latitude: Double? = null,
    @Json(name = "longitude")
    val longitude: Double? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "promotion_discount_center")
    val promotionDiscountCenter: PromotionDiscountCenter? = null,
    @Json(name = "registered_name")
    val registeredName: String? = null,
    @Json(name = "street_address")
    val streetAddress: String? = null,
    @Json(name = "branch")
    val branch: Branch? = null,
    @Json(name = "city_id")
    val cityId: Int? = null
)

@JsonClass(generateAdapter = true)
data class PromotionDiscountCenter(
    @Json(name = "percentage")
    val percentage: Int? = null,
    @Json(name = "service_id")
    val serviceId: Int? = null,
    @Json(name = "center_id")
    val centerId: Int? = null,
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
)

@JsonClass(generateAdapter = true)
data class Branch(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "company_id")
    val companyId: Int? = null,
    @Json(name = "display_name")
    val displayName: String? = null,
    @Json(name = "street_address")
    val streetAddress: String? = null
)