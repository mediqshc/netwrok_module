package com.fatron.network_module.models.response.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PromotionAge(
    @Json(name = "family_member_relation_id")
    val familyMemberRelationId: Int?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "max_age")
    val maxAge: Int?,
    @Json(name = "min_age")
    val minAge: Int?,
    @Json(name = "promotion_id")
    val promotionId: Int?,
    @Json(name = "gender_id")
    val genderId: Int? = null
)