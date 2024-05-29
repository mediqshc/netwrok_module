package com.fatron.network_module.models.response.partnerprofile


import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.partnerprofile.MedicalStaffEducationAttachment
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EducationResponse(
    @Json(name = "country_id")
    @SerializedName("country_id")
    val countryId: Int? = null,
    @Json(name = "degree")
    @SerializedName("degree")
    val degree: String? = null,
    @Json(name = "id")
    @SerializedName("id")
    val id: String? = null,
    @Json(name = "partner_education_attachments")
    @SerializedName("partner_education_attachments")
    val medicalStaffEducationAttachments: List<MedicalStaffEducationAttachment>? = arrayListOf(),
    @Json(name = "school")
    @SerializedName("school")
    val school: String? = null,
    @Json(name = "user_id")
    @SerializedName("user_id")
    val userId: Int? = null,
    @Json(name = "year")
    @SerializedName("year")
    val year: String? = null
):MultipleViewItem(title = school, desc = "$degree | $countryId | $year"), java.io.Serializable