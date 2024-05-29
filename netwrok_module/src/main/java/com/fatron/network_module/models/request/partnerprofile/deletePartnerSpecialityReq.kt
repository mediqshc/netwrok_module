package com.fatron.network_module.models.request.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class deletePartnerSpecialityReq(
    @Json(name = "speciality_id")
    val specialityId: Int
)