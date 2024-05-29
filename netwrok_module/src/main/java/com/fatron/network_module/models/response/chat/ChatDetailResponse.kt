package com.fatron.network_module.models.response.chat


import com.fatron.network_module.models.response.ordersdetails.CallUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatDetailResponse(
    @Json(name = "channel")
    val channel: Channel?
)

@JsonClass(generateAdapter = true)
data class Channel(
    @Json(name = "booked_for_id")
    val bookedForId: Int?,
    @Json(name = "booked_for_user")
    val bookedForUser: CallUser?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "partner_twilio_member_id")
    val partnerTwilioMemberId: String?,
    @Json(name = "partner_twilio_user_id")
    val partnerTwilioUserId: String?,
    @Json(name = "partner_user")
    val partnerUser: CallUser?,
    @Json(name = "partner_user_id")
    val partnerUserId: Int?,
    @Json(name = "twilio_channel_service_id")
    val twilioChannelServiceId: String?,
    @Json(name = "twilio_member_id")
    val twilioMemberId: String?,
    @Json(name = "twilio_user_id")
    val twilioUserId: String?,
    @Json(name = "updated_at")
    val updatedAt: String?
)