package com.fatron.network_module.models.request.walkin


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInAttachmentRequest(
    @Json(name = "walk_in_pharmacy_id")
    var walkInPharmacyId: Int? = null,
    @Json(name = "walk_in_pharmacy_attachment_id")
    var walkInPharmacyAttachmentId: Int? = null,

    @Json(name = "walk_in_laboratory_id")
    var walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_laboratory_attachment_id")
    var walkInLaboratoryAttachmentId: Int? = null,

    @Json(name = "walk_in_hospital_id")
    var walkInHospitalId: Int? = null,
    @Json(name = "walk_in_hospital_attachment_id")
    var walkInHospitalAttachmentId: Int? = null,
)