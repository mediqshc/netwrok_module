package com.fatron.network_module.models.response.emr.type


import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.emr.customer.medicine.MedicinesResponse
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.ordersdetails.CallUser
import com.fatron.network_module.models.response.video.BookedForUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmrDetails(
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "date")
    val date: String? = null,
    @Json(name = "created_by")
    val createdBy: Int? = null,
    @Json(name = "emr_attachments")
    val emrAttachments: List<Attachment>? = null,
    @Json(name = "emr_customer_attachments")
    var emrCustomerAttachments: List<Attachment>? = null,
    @Json(name = "emr_diagnosis")
    val emrDiagnosis: List<EmrGeneric>? = null,
    @Json(name = "emr_lab_tests")
    val emrLabTests: List<EmrGeneric>? = null,
    @Json(name = "emr_medical_healthcares")
    val emrMedicalHealthcares: List<EmrGeneric>? = null,
    @Json(name = "emr_number")
    val emrNumber: String? = null,
    @Json(name = "emr_products")
    val emrProducts: List<MedicinesResponse>? = null,
    @Json(name = "emr_symptoms")
    val emrSymptoms: List<EmrGeneric>? = null,
    @Json(name = "emr_vitals")
    val emrVitals: List<EmrVital>? = null,
    @Json(name = "customer_user")
    val customerUser: CallUser? = null,
    @Json(name = "booked_for_user")
    val bookedForUser: CallUser? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "is_draft")
    val isDraft: Int? = null,
    @Json(name = "status")
    val status: Int? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null,
    @Json(name = "user_id")
    val userId: Int? = null,

    @Json(name = "partner_name")
    val partnerName: String? = null,
    @Json(name = "partner_service_id")
    val partnerServiceId: String? = null,
    @Json(name = "speciality")
    val speciality: List<GenericItem>? = null,
    @Json(name = "partner_profile_picture")
    val partnerProfilePicture: String? = null,
    @Json(name = "booking_date")
    val bookingDate: String? = null,
    @Json(name = "shared")
    val shared: List<FamilyConnection>? = null,
    @Json(name = "is_lab_report")
    val isLabReport: Int? = null,
    @Json(name = "lab_icon_url")
    val labIconUrl: String? = null
)