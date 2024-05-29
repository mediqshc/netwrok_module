package com.fatron.network_module.models.request.claim


import com.fatron.network_module.models.response.claim.ClaimPackage
import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody

@JsonClass(generateAdapter = true)
data class AddClaimAttachmentRequest(
    @Json(name = "claim_id")
    var claimId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null,
    @Json(name = "attachment_type")
    var attachmentType: Int? = null,
    @Json(name = "document_type")
    var documentType: Int? = null,
    var attachments:  ArrayList<MultipartBody.Part>?
)