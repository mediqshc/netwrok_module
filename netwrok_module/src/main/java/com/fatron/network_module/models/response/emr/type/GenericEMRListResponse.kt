package com.fatron.network_module.models.response.emr.type


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenericEMRListResponse(
    @Json(name = "symptoms")
    val symptoms: GenericMedicalRecord? = null,
    @Json(name = "lab_tests")
    val labTests: GenericMedicalRecord? = null,
    @Json(name = "medical_healthcares")
    val medicalHealthCares: GenericMedicalRecord? = null,
    @Json(name = "diagnosis")
    val diagnosis: GenericMedicalRecord? = null,
    @Json(name = "products")
    val products: GenericMedicalRecord? = null,
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null
)