package com.fatron.network_module.models.response.checkout


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartnerDetails(
    @Json(name = "id")
    val id: Int?,
    @Json(name = "license_code")
    val licenseCode: String?,
    @Json(name = "status_id")
    val statusId: Int?,
    @Json(name = "user_id")
    val userId: Int?
)