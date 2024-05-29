package com.fatron.network_module.models.response.appointments

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicalRecords(
    @Json(name = "emr_customer_type")
    val emrType: Int? = null,
    @Json(name = "emr_name")
    val emrName: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "emr_number")
    val emrNumber: String? = null,
    @Json(name = "date")
    val date: String? = null
): java.io.Serializable