package com.fatron.network_module.models.response.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Order(
    @Json(name = "booked_for_id")
    val bookedForId: Int?,
    @Json(name = "booked_for_user")
    val bookedForUser: BookedForUser?,
    @Json(name = "booking_comment")
    val bookingComment: Any?,
    @Json(name = "booking_completed_reasons_id")
    val bookingCompletedReasonsId: Int?,
    @Json(name = "booking_date")
    val bookingDate: String?,
    @Json(name = "booking_status_id")
    val bookingStatusId: Int?,
    @Json(name = "created_at")
    val createdAt: String?,
    @Json(name = "currency_id")
    val currencyId: Int?,
    @Json(name = "customer_user")
    val customerUser: CustomerUser?,
    @Json(name = "day_id")
    val dayId: Int?,
    @Json(name = "deleted_at")
    val deletedAt: Any?,
    @Json(name = "end_time")
    val endTime: String?,
    @Json(name = "fee")
    val fee: Int?,
    @Json(name = "fee_collected")
    val feeCollected: Int?,
    @Json(name = "followup_date")
    val followupDate: Any?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "instructions")
    val instructions: String?,
    @Json(name = "partner_id")
    val partnerId: Int?,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int?,
    @Json(name = "partner_user")
    val partnerUser: PartnerUser?,
    @Json(name = "read")
    val read: Int?,
    @Json(name = "start_time")
    val startTime: String?,
    @Json(name = "unique_identification_number")
    val uniqueIdentificationNumber: String?,
    @Json(name = "updated_at")
    val updatedAt: String?,
    @Json(name = "user_id")
    val userId: Int?,
    @Json(name = "user_location_id")
    val userLocationId: Int?
)