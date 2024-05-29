package com.fatron.network_module.models.request.labtest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LabTestHomeCollectionRequest(
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "home_collection")
    var homeCollection: Int? = null,
    @Json(name = "branch_id")
    var branchId: Int? = null
)