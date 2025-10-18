package com.fatron.network_module.models.request.claim

import com.squareup.moshi.Json
import okhttp3.MultipartBody
import retrofit2.http.Part

class AddPriorAuthAttachmentRequest(
    @Json(name = "prior_authorization_id")
    var priorAuthorizationId: Int? = null,
    @Json(name = "attachment_type")
    var attachmentType: Int? = null,
    @Json(name = "document_type")
    var documentType: Int? = null,
    var attachments:  ArrayList<MultipartBody.Part>?
)
