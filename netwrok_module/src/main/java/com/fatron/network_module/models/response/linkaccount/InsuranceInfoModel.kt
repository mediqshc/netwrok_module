package com.fatron.network_module.models.response.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InsuranceInfoModel(
    @Json(name = "CERTID")
    val CERTID: String?,
    @Json(name = "NAME")
    val NAME: String?,
    @Json(name = "POLICY")
    val POLICY: String?
)