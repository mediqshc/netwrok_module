package com.fatron.network_module.models.response.becomepartner


import com.squareup.moshi.Json

data class PartnerCnicResponse(
    @Json(name = "cnic_back")
    val cnicBack: String?,
    @Json(name = "cnic_front")
    val cnicFront: String?
)