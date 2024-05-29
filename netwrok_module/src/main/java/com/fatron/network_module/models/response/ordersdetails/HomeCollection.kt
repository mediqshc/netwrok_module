package com.fatron.network_module.models.response.ordersdetails

import com.fatron.network_module.models.response.labtest.LabResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeCollection(
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "home_collection")
    val homeCollection: Int? = null,
    @Json(name = "branch_id")
    val preferredLaboratory: Int? = null,
    @Json(name = "branch")
    val branch: LabResponse? = null
)