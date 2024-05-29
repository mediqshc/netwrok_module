package com.fatron.network_module.models.request.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class deletePartnerWork(
    @Json(name = "work_id")
    val workId: Int
)