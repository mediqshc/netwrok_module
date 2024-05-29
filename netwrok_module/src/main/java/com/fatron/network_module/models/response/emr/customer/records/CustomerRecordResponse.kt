package com.fatron.network_module.models.response.emr.customer.records


import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.emr.customer.medicine.MedicinesResponse
import com.fatron.network_module.models.response.emr.type.EmrVital
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.ordersdetails.CallUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerRecordResponse(
    @Json(name = "partner_name")
    var partnerName: String? = null,
    @Json(name = "lab_icon_url")
    var labIconUrl: String? = null,
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "emr_number")
    val emrNumber: String? = null,
    @Json(name = "customer_user")
    val customerUser: CallUser? = null,
    @Json(name = "speciality")
    val speciality: List<GenericItem>? = null,
    @Json(name = "booking_date")
    val bookingDate: String? = null,
    @Json(name = "service_type")
    val serviceTypeId: String? = null,
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "products")
    val products: List<MedicinesResponse>? = null,
    @Json(name = "vitals")
    val vitals: List<EmrVital>? = null,
    @Json(name = "diagnosis")
    val diagnosis: List<MedicinesResponse>? = null,
    @Json(name = "lab_tests")
    val labTests: List<MedicinesResponse>? = null,
    @Json(name = "medical_healthcares")
    val medical_healthcares: List<MedicinesResponse>? = null,
    @Json(name = "attachments")
    val attachments: List<Attachment>? = null,
    @Json(name = "is_shared")
    val isShared: Int? = null,
    @Json(name = "date")
    val date: String? = null,
    @Json(name = "original_date")
    val originalDate: String? = null,
    @Json(name = "shared")
    val shared: List<FamilyConnection>? = null
){
    var isSelected = false
}