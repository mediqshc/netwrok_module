package com.fatron.network_module.models.response.labtest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LabBranchListResponse {
    @Json(name = "lab_branches")
    var labBranches: List<LabResponse>? = null
}