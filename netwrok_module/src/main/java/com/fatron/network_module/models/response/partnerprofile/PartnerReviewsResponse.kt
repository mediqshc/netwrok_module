package com.fatron.network_module.models.response.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartnerReviewsResponse(
    @Json(name = "reviews")
    val reviews: List<PartnerReviews>? = arrayListOf(),
)

@JsonClass(generateAdapter = true)
data class PartnerReviews(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "service_id")
    val serviceId: Int? = null,
    @Json(name = "rating")
    val rating: String? = null,
    @Json(name = "review")
    val review: String? = null,
){
    var serviceName: String = ""
    var ratingStars: String = ""
}