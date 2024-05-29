package com.fatron.network_module.models.request.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicalStaffEducationAttachment(
    @Json(name = "education_id")
    val educationId: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "media")
    val media: List<Media>,
    @Json(name = "media_id")
    val mediaId: Int,
    @Json(name = "user_id")
    val userId: Int
): java.io.Serializable