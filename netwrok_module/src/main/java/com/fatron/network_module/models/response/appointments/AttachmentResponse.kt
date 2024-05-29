package com.fatron.network_module.models.response.appointments


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AttachmentResponse(
    @Json(name = "attachments")
    val attachments: List<Attachment>?
) : Serializable
data class Attachment(
    @Json(name = "attachment_type_id")
    val attachmentTypeId: Int?,
    @Json(name = "attachments")
    val attachments: Attachments?,
    @Json(name = "request_document")
    val requestDocuments: GenericItem? = null,
    @Json(name = "booking_id")
    val bookingId: Int?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "media_id")
    val mediaId: Int?,
    @Json(name = "type")
    val type: String? = null,

    @Json(name = "emr_id")
    val emrId: Int? = null,
    @Json(name = "emr_type_id")
    val emrTypeId: Int? = null,

    @Json(name = "claim_id")
    val claimId: Int?,
    @Json(name = "document_type")
    val documentType: Int?,
) : Serializable

data class Attachments(
    @Json(name = "file")
    val `file`: String?,
    @Json(name = "id")
    val id: Int?
) : Serializable