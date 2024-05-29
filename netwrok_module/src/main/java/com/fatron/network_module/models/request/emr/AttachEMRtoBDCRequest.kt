package com.fatron.network_module.models.request.emr


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AttachEMRtoBDCRequest(
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "emr_customer_type")
    val emrCustomerType: Int? = null,
    @Json(name = "emr_ids")
    val emrIds: List<Int>? = null,
    @Json(name = "emr_id")
    val emrId: Int? = null, //for detach
)