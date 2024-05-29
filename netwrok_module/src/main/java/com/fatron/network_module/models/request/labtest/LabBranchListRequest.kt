package com.fatron.network_module.models.request.labtest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LabBranchListRequest(
    @Json(name = "lab_id")
    var labId: Int? = null,
    @Json(name = "city_id")
    var cityId: Int? = null
)