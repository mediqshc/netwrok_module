package com.fatron.network_module.models.response.checkout


import com.fatron.network_module.models.response.ordersdetails.PaymentBreakdown
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CheckoutDetailResponse (
    @Json(name = "booked_for_id")
    val bookedForId: Int? = null,

    @Json(name = "booked_for_user")
    val bookedForUser: BookedForUser? = null,

    @Json(name = "booking_comment")
    val bookingComment: Any? = null,

    @Json(name = "booking_completed_reasons_id")
    val bookingCompletedReasonsId: Any? = null,

    @Json(name = "booking_date")
    val bookingDate: String? = null,

    @Json(name = "booking_status_id")
    val bookingStatusId: Int? = null,

    @Json(name = "created_at")
    val createdAt: String? = null,

    @Json(name = "currency_id")
    val currencyId: Int? = null,

    @Json(name = "customer_user")
    val customerUser: CustomerUser? = null,

    @Json(name = "day_id")
    val dayId: Int? = null,

    @Json(name = "deleted_at")
    val deletedAt: Any? = null,

    @Json(name = "end_time")
    val endTime: String? = null,

    @Json(name = "fee")
    val fee: String? = null,

    @Json(name = "fee_collected")
    val feeCollected: Any? = null,

    @Json(name = "followup_date")
    val followupDate: Any? = null,

    @Json(name = "id")
    val id: Int? = null,

    @Json(name = "instructions")
    val instructions: String? = null,

    @Json(name = "partner_details")
    val partnerDetails: PartnerDetails? = null,

    @Json(name = "partner_id")
    val partnerId: Int? = null,

    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,

    @Json(name = "partner_user")
    val partnerUser: PartnerUser? = null,

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

    @Json(name = "payment_breakdown")
    val paymentBreakdown: PaymentBreakdown? = null,

    @Json(name = "applied_promotions")
    val promotions: Promotions? = null,

    @Json(name = "applied_packages")
    val packages: List<Promotions>? = null,

    @Json(name = "available_promotions")
    val availablePromotions: List<Promotions>? = null,

    @Json(name = "available_packages")
    val availablePackages: List<Promotions>? = null,

    @Json(name = "speciality_id")
    val specialityId: Int? = null
)