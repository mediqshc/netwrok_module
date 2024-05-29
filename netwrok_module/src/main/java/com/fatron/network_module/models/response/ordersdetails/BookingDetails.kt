package com.fatron.network_module.models.response.ordersdetails

import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.chat.ChatProperties
import com.fatron.network_module.models.response.checkout.Promotions
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.RequiredDocumentType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class BookingDetails(
    @Json(name = "attachments")
    val attachments: List<Attachment>? = null,

    @Json(name = "booked_for_id")
    val bookedForId: Int? = null,

    @Json(name = "booked_for_user")
    val bookedForUser: CallUser? = null,

    @Json(name = "booking_comment")
    val bookingComment: Any? = null,

    @Json(name = "booking_completed_reasons_id")
    val bookingCompletedReasonsId: Any? = null,

    @Json(name = "booking_date")
    val bookingDate: String? = null,

    @Json(name = "booking_status_id")
    var bookingStatusId: Int? = null,

    @Json(name = "booking_type")
    val bookingType: Int? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "created_by")
    val createdBy: Int? = null,

    @Json(name = "currency_id")
    val currencyId: Int? = null,

    @Json(name = "customer_user")
    val customerUser: CallUser? = null,

    @Json(name = "chat_properties")
    val chatProperties: ChatProperties? = null,

    @Json(name = "chat_session_status")
    val chatSessionStatus: String? = null,

    @Json(name = "day_id")
    val dayId: Int? = null,

    @Json(name = "deleted_at")
    val deletedAt: Any? = null,

    @Json(name = "end_time")
    val endTime: String? = null,

    @Json(name = "family_members")
    val familyMembers: List<FamilyConnection>? = null,

    @Json(name = "fee")
    val fee: String? = null,

    @Json(name = "fee_collected")
    val feeCollected: Int? = null,

    @Json(name = "followup_date")
    val followupDate: Any? = null,

    @Json(name = "prescription_lab")
    val homeCollection: HomeCollection? = null,

    @Json(name = "id")
    val id: Int? = null,

    @Json(name = "instructions")
    val instructions: String? = null,

    @Json(name = "partner_id")
    val partnerId: Int? = null,

    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,

    @Json(name = "speciality_id")
    var specialityId: Int? = null,

    @Json(name = "partner_type_id")
    val partnerTypeId: Int? = null,

    @Json(name = "partner_user_id")
    val partnerUserId: Int? = null,

    @Json(name = "read")
    val read: Int? = null,

    @Json(name = "start_time")
    val startTime: String? = null,

    @Json(name = "unique_identification_number")
    val uniqueIdentificationNumber: String? = null,

    @Json(name = "updated_at")
    val updatedAt: String? = null,

    @Json(name = "user_id")
    val userId: Int? = null,

    @Json(name = "user_location_id")
    val userLocationId: Int? = null,

    @Json(name = "review")
    val reviews: Review? = null,

    @Json(name = "journey_time_id")
    val journeyTimeId: Int? = null,

    @Json(name = "shift_id")
    val shiftId: Int? = null,

    @Json(name = "shift")
    val shift: String? = null,

    @Json(name = "booking_address")
    var bookingAddress: UserLocation? = null,

    @Json(name = "payment_breakdown")
    val paymentBreakdown: PaymentBreakdown? = null,

    @Json(name = "applied_promotions")
    val promotions: Promotions? = null,

    @Json(name = "applied_packages")
    val packages: List<Promotions>? = null,

    @Json(name = "visit_type")
    val visitType: Int? = null,

    @Json(name = "partner_service")
    val partnerService: String? = null,

    @Json(name = "time_left")
    val timeLeft: String? = null,

    @Json(name = "next_session")
    val nextSession: String? = null,

    @Json(name = "last_session")
    val lastSession: String? = null,

    @Json(name = "duty_status_id")
    val dutyStatusId: Int? = null,

    @Json(name = "city_id")
    val cityId: Int? = null,

    @Json(name = "partner_details")
    val partnerDetails: CallUser? = null,

    @Json(name = "partner_user")
    val partnerUser: CallUser? = null,

    // for claim details
    @Json(name = "settlement_documents")
    var settlementDocuments: List<RequiredDocumentType>? = null,

    //for booking details
    @Json(name="booking_reason")
    var bookingReason :BookingReason? = null,

    @Json(name = "settlement_documents_required")
    var settlementDocumentsRequired: List<SettlementDocumentRequired>? = null,

) : Serializable

@JsonClass(generateAdapter = true)
data class SettlementDocumentRequired(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "count")
    val count: Int? = null
)
@JsonClass(generateAdapter = true)
data  class BookingReason(
    @Json(name="id")
    val id:Int?=null,
    @Json(name="booking_id")
    val booking_id:Int?= null,
    @Json(name="reason_id")
    val reason_id:Int?= null,
    @Json(name="comment")
    val comment:String?=null,
    @Json(name="created_at")
    val created_at:String?= null,
    @Json(name="updated_at")
    val updated_at:String?= null,
    @Json(name="reason")
    val reason: reason?=null



)
@JsonClass(generateAdapter = true)
data class reason(
    @Json(name="id")
    val id:Int?=null,
    @Json(name="tenant_id")
    val tenant_id:Int?= null,
    @Json(name="reason_type_id")
    val reason_type_id:Int?= null,
    @Json(name="isactive")
    val isactive:Boolean?=null,
    @Json(name="reason")
    val reason:String?= null,
    @Json(name="description")
    val description:String?=null,
    @Json(name="created_at")
    val created_at:String?= null,
    @Json(name="updated_at")
    val updated_at:String?= null,
)
