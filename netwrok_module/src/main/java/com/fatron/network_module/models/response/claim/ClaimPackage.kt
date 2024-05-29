package com.fatron.network_module.models.response.claim


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimPackage(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "code")
    var code: String? = null,
    @Json(name = "type_id")
    var type_id: Int? = null,
    @Json(name = "start_date")
    var start_date: String? = null,
    @Json(name = "end_date")
    var end_date: String? = null,
    @Json(name = "promotion_user_credit")
    var credit: Credit? = null,
    @Json(name = "currency_id")
    var currencyId: Int? = null,
    @Json(name = "reference_code")
    var referenceCode: String? = null,
)