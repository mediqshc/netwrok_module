package com.fatron.network_module.models.response.checkout

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Promotions(
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
    @Json(name = "promotion_name")
    var promotionName: String? = null,
    @Json(name = "promotion_promocode")
    val promotionPromocode: String? = null,
    @Json(name = "promotion_reference_code")
    val promotionReferenceCode: String? = null,
    @Json(name = "discount_type_id")
    val discountTypeId: Int? = null,
    @Json(name = "discount_value")
    var discountValue: String? = null,
    @Json(name = "icon_url")
    val bannerImage: String? = null,
    @Json(name = "is_promotion")
    val isPromotion: Boolean? = null
)