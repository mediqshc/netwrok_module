package com.fatron.network_module.models.response.claim


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimConnectionExclusionResponse(
    @Json(name = "review_exclusion")
    var reviewExclusion: ReviewExclusion? = null
)

@JsonClass(generateAdapter = true)
data class ReviewExclusion(
    @Json(name = "excluded_items")
    val excludedItems: List<String>? = null,
    @Json(name = "excluded_categories")
    val excludedCategories: List<String>? = null,
    @Json(name = "specialities")
    val specialities: List<String>? = null,
    @Json(name = "doctors")
    val doctors: List<Map<String, Any>>? = null,
    @Json(name = "exclusion")
    val exclusion: String? = null,
    @Json(name = "categories")
    val categories: List<List<String>>? = null,

    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
    @Json(name = "model_id")
    val modelId: Int? = null,
    @Json(name = "model")
    val model: Int? = null,
    @Json(name = "lab_tests")
    val labTests: List<GenericItem>? = null,
    @Json(name = "promotion_lab_test_categories")
    val promotionLabTestCategories: List<PromotionLabTestCategories>? = null,
    @Json(name = "pharmacy")
    val pharmacy: List<GenericItem>? = null,
    @Json(name = "promotion_pharmacy_categories")
    val promotionPharmacyCategories: List<PharmacyLabTestCategories>? = null,
)

@JsonClass(generateAdapter = true)
data class PromotionLabTestCategories(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
    @Json(name = "lab_test_category_id")
    val labTestCategoryId: Int? = null,
    @Json(name = "lab_tests")
    val labTests: List<GenericItem>? = null,
)

@JsonClass(generateAdapter = true)
data class PharmacyLabTestCategories(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
    @Json(name = "pharmacy_category_id")
    val pharmacyCategoryId: Int? = null,
    @Json(name = "product_category")
    val pharmacy: List<GenericItem>? = null,
)