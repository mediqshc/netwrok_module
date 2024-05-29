package com.fatron.network_module.models.request.emr


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoreEMRRequest(
    @Json(name = "booking_id")
    val bookingId: String? = null,
    @Json(name = "customer_id")
    val customerId: String? = null,
    @Json(name = "emr_id")
    val emrId: String? = null,
    @Json(name = "is_draft")
    val isDraft: Int? = null,
    @Json(name = "emr_chat")
    val emrChat: Int? = null,
    @Json(name = "vitals")
    val vitals: List<Vital>? = null,
    @Json(name = "medical_advice")
    val medicalAdvice: String? = null
)