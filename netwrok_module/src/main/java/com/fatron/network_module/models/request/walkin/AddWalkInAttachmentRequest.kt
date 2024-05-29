package com.fatron.network_module.models.request.walkin


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody

@JsonClass(generateAdapter = true)
data class AddWalkInAttachmentRequest(
    @Json(name = "walk_in_pharmacy_id")
    var walkInPharmacyId: Int? = null,
    @Json(name = "pharmacy_id")
    var pharmacyId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null,
    @Json(name = "attachment_type")
    var attachmentType: Int? = null,
    @Json(name = "document_type")
    var documentType: Int? = null,
    var attachments:  ArrayList<MultipartBody.Part>?,


    @Json(name = "walk_in_laboratory_id")
    var walkInLaboratoryId: Int? = null,
    @Json(name = "lab_id")
    var labId: Int? = null,

    @Json(name = "walk_in_hospital_id")
    var walkInHospitalId: Int? = null,
    @Json(name = "healthcare_id")
    var healthcareId: Int? = null,
)