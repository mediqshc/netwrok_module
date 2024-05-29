package com.fatron.network_module.models.response.emr.customer.records


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerEMRRecordResponse(
    @Json(name = "emr_details")
    val emrDetails: CustomerRecordResponse?
)