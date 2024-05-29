package com.fatron.network_module.models.response.homeservice


import com.fatron.network_module.models.response.appointments.MedicalRecords
import com.fatron.network_module.models.response.family.FamilyConnection
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeServiceDetailResponse(
    @Json(name = "booking_id")
    val bookingId: Int?=null,
    @Json(name = "customer_user_id")
    val customerUserId: Int?=null,
    @Json(name = "partner_id")
    val partnerId: Int?=null,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int?=null,
    @Json(name = "partner_user_id")
    val partnerUserId: Int?=null,
    @Json(name = "patients")
    val patients: List<FamilyConnection>?=null,
    @Json(name = "medical_records")
    @SerializedName("medical_records")
    val medicalRecord: List<MedicalRecords>? = null
)