package com.fatron.network_module.models.response.bdc


import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BDCFilterResponse {
    @Json(name = "partners")
    var partners: List<PartnerProfileResponse>? = arrayListOf()

    @Json(name = "last_page")
    var lastPage: Int=0
}