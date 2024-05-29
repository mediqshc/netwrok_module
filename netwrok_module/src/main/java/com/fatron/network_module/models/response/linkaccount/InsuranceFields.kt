package com.fatron.network_module.models.response.linkaccount


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
 class InsuranceFields(
    @Json(name = "insurance_id")
   val insuranceId: Int? = null,
    @Json(name = "fields")
    val fields: List<String>? = null
)