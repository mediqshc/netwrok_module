package com.fatron.network_module.models.response.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Credit(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "promotion_id")
    var promotion_id: Int? = null,
    @Json(name = "amount")
    var amount: Double? = null,
    @Json(name = "allow_credits")
    var allowCredits: Int? = null,
)