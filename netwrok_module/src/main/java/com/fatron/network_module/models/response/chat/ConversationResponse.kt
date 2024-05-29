package com.fatron.network_module.models.response.chat


import com.fatron.network_module.models.response.ordersdetails.CallUser
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
data class ConversationResponse(
    @Json(name = "booked_for_id")
    val bookedForId: Int?=null,
    @Json(name = "booked_for_user")
    val bookedForUser: CallUser?=null,
    @Json(name = "customer_user")
    val customerUser: CallUser?,
    @Json(name = "booking_message")
    val bookingMessage: BookingMessage?=null,
    @Json(name = "booking_status_id")
    val bookingStatusId: Int?=null,
    @Json(name = "partner_chat_properties")
    val partnerChatProperties: ChatProperties?=null,
    @Json(name = "chat_properties")
    val chatProperties: ChatProperties?=null,
    @Json(name = "id")
    val id: Int?=null,
    @Json(name = "partner_user")
    val partnerUser: CallUser?=null,
    @Json(name = "partner_user_id")
    val partnerUserId: Int?=null,
    @Json(name = "user_id")
    val userId: Int?=null
)



@JsonClass(generateAdapter = true)
data class ChatProperties(
//    @Json(name = "booking_id")
//    val bookingId: Int?=null,
    @Json(name = "id")
    val id: Int?=null,
    @Json(name = "last_message_time")
    var lastMessageTime: String?=null,
    @Json(name = "partner_user_id")
    val partnerUserId: Int?=null,
    @Json(name = "twilio_channel_service_id")
    val twilioChannelServiceId: String?=null,
    @Json(name = "twilio_member_id")
    val twilioMemberId: String?=null,
    @Json(name = "twilio_user_id")
    val twilioUserId: String?=null,
    @Json(name = "unread_messages_count")
    var unreadMessagesCount: Int?=0,
    @Json(name = "user_id")
    val userId: Int?=null,

    @Json(name = "booked_for_id")
    val bookedForId: Int?=null,
    @Json(name = "partner_twilio_user_id ")
    val partnerTwilioUserId: String? = null,
    @Json(name = "partner_twilio_member_id")
    val partnerTwilioMemberId: String? = null,
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null,
    var timeLocale:String?="UTC"
)

@JsonClass(generateAdapter = true)
data class BookingMessage(
    @Json(name = "booking_id")
    val bookingId: Int?=null,
    @Json(name = "id")
    val id: Int?=null,
    @Json(name = "session_end")
    val sessionEnd: String?=null,
    @Json(name = "session_status")
    var sessionStatus: Int?=null,
    @Json(name = "time_left")
    val timeLeft: String?=null,
 )

