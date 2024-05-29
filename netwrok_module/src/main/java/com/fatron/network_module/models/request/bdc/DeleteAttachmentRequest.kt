package com.fatron.network_module.models.request.bdc


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeleteAttachmentRequest (

    @Json(name = "attachment_id")
    var attachmentId: Int? = null, // this id is user to delete bdc document

    @Json(name = "emr_attachment_id")
    var emrAttachmentId: Int? = null, // this id is user to delete emr document

    @Json(name = "claim_attachment_id")
    var claimAttachmentId: Int? = null // this id is user to delete claim document
)