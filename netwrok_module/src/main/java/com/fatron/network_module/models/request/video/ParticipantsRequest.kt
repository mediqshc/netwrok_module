package com.fatron.network_module.models.request.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ParticipantsRequest(
    @Json(name = "order_id")
    val orderId: Int?,
    @Json(name = "participant_id")
    var participantId: Int? = null,
    @Json(name = "drop_call")
    var dropCall: Int? = null,
    @Json(name = "is_rejected")
    var isRejected: Int? = null
)