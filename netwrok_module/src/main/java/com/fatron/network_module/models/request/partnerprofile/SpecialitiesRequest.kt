package com.fatron.network_module.models.request.partnerprofile

import com.squareup.moshi.Json


data class SpecialitiesRequest(

    @Json(name = "specialities")

    val specialities: List<Int>?

)