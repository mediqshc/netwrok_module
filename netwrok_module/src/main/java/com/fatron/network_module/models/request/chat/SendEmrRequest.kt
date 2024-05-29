package com.fatron.network_module.models.request.chat

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendEmrRequest(
    @Json(name = "emr_id")
    val emrId: Int?=null,
    @Json(name = "customer_id")
    val customerId: Int?=null,
    @Json(name = "doctor_id")
    val doctorId: Int?=null
)