package com.fatron.network_module.models.request.emr


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EMRDownloadRequest (
    @Json(name = "emr_id")
    var emrId: Int? = null,

    @Json(name = "type")
    var type: Int? = null,

    @Json(name = "emr_attachment_id")
    var emrAttachmentId: Int? = null,

    @Json(name = "report")
    var report: Int? = 1
)