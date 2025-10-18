package com.fatron.network_module.models.response.Scribe
import com.google.gson.annotations.SerializedName
import android.os.Parcelable



import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


data class ScribeResponse(
    @Json(name = "bookings") val bookings: List<Booking>?
)

data class Booking(
    @Json(name = "id") val id: Int?,
    @Json(name = "tenant_id") val tenantId: Int?,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "booked_for_id") val bookedForId: Int?,
    @Json(name = "partner_id") val partnerId: Int?,
    @Json(name = "partner_user_id") val partnerUserId: Int?,
    @Json(name = "partner_service_id") val partnerServiceId: Int?,
    @Json(name = "speciality_id") val specialityId: Int?,
    @Json(name = "city_id") val cityId: Int?,
    @Json(name = "user_location_id") val userLocationId: Int?,
    @Json(name = "booking_type") val bookingType: Int?,
    @Json(name = "unique_identification_number") val uniqueIdentificationNumber: String?,
    @Json(name = "booking_date") val bookingDate: String?,
    @Json(name = "day_id") val dayId: Int?,
    @Json(name = "start_time") val startTime: String?,
    @Json(name = "end_time") val endTime: String?,
    @Json(name = "shift_id") val shiftId: Int?,
    @Json(name = "instructions") val instructions: String?,
    @Json(name = "fee") val fee: String?,
    @Json(name = "discount") val discount: String?,
    @Json(name = "currency_id") val currencyId: Int?,
    @Json(name = "booking_status_id") val bookingStatusId: Int?,
    @Json(name = "external_booking_id") val externalBookingId: String?,
    @Json(name = "external_booking_status_id") val externalBookingStatusId: String?,
    @Json(name = "followup_date") val followupDate: String?,
    @Json(name = "fee_collected") val feeCollected: Int?,
    @Json(name = "booking_completed_reasons_id") val bookingCompletedReasonsId: Int?,
    @Json(name = "booking_comment") val bookingComment: String?,
    @Json(name = "read") val read: Int?,
    @Json(name = "visit_type_id") val visitTypeId: Int?,
    @Json(name = "created_by") val createdBy: Int?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "deleted_at") val deletedAt: String?,
    @Json(name = "delivery_charges") val deliveryCharges: String?,
    @Json(name = "actual_amount") val actualAmount: String?,
    @Json(name = "is_recursive") val isRecursive: Int?,
    @Json(name = "checkout_time") val checkoutTime: String?,
    @Json(name = "processed_by") val processedBy: String?,
    @Json(name = "rider_name") val riderName: String?,
    @Json(name = "rider_phone") val riderPhone: String?,
    @Json(name = "room_sid") val roomSid: String?,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "customer_user") val customerUser: User?,
    @Json(name = "booked_for_user") val bookedForUser: User?,
    @Json(name = "partner_user") val partnerUser: User?,
    @Json(name = "surgery") val surgery: String?,
    @Json(name = "next_session") val nextSession: String?,
    @Json(name = "last_session") val lastSession: String?,
    @Json(name = "partner_type_id") val partnerTypeId: Int?,
    @Json(name = "shift") val shift: String?,
    @Json(name = "feex") val feex: String?,
    @Json(name = "booking_address") val bookingAddress: BookingAddress?,
    @Json(name = "family_members") val familyMembers: List<FamilyMember>?,
    @Json(name = "attachments") val attachments: List<Any>?,
    @Json(name = "review") val review: String?,
    @Json(name = "journey_time_id") val journeyTimeId: String?,
    @Json(name = "payment_breakdown") val paymentBreakdown: PaymentBreakdown?,
    @Json(name = "applied_promotions") val appliedPromotions: AppliedPromotions?,
    @Json(name = "applied_packages") val appliedPackages: List<AppliedPackage>?
)

data class User(
    @Json(name = "id") val id: Int?,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "profile_picture_id") val profilePictureId: Int?,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "country_id") val countryId: Int?,
    @Json(name = "city_id") val cityId: Int?,
    @Json(name = "gender_id") val genderId: Int?,
    @Json(name = "date_of_birth") val dateOfBirth: String?,
    @Json(name = "user_profile_pic") val userProfilePic: UserProfilePic?
)

data class UserProfilePic(
    @Json(name = "id") val id: Int?,
    @Json(name = "file") val file: String?
)

data class BookingAddress(
    @Json(name = "id") val id: Int?,
    @Json(name = "lat") val lat: String?,
    @Json(name = "long") val long: String?,
    @Json(name = "street") val street: String?,
    @Json(name = "floor_unit") val floorUnit: String?,
    @Json(name = "category") val category: Int?,
    @Json(name = "other") val other: String?,
    @Json(name = "address") val address: String?,
    @Json(name = "region") val region: String?,
    @Json(name = "sublocality") val sublocality: String?
)

data class FamilyMember(
    @Json(name = "id") val id: Int?,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "family_member_id") val familyMemberId: Int?,
    @Json(name = "relation") val relation: String?,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "country_code") val countryCode: String?,
    @Json(name = "gender_id") val genderId: Int?,
    @Json(name = "family_member_relation_id") val familyMemberRelationId: Int?,
    @Json(name = "profile_picture") val profilePicture: String?,
    @Json(name = "age") val age: Int?,
    @Json(name = "addresses") val addresses: List<BookingAddress>?
)

data class PaymentBreakdown(
    @Json(name = "sub_total") val subTotal: String?,
    @Json(name = "payable_amount") val payableAmount: String?,
    @Json(name = "payment_to_be_collected") val paymentToBeCollected: String?,
    @Json(name = "total") val total: String?,
    @Json(name = "payment_method") val paymentMethod: String?
)

data class AppliedPromotions(
    @Json(name = "promotion_id") val promotionId: Int?,
    @Json(name = "promotion_name") val promotionName: String?,
    @Json(name = "discount_type_id") val discountTypeId: Int?,
    @Json(name = "discount_value") val discountValue: String?,
    @Json(name = "is_promotion") val isPromotion: Boolean?
)

data class AppliedPackage(
    @Json(name = "promotion_id") val promotionId: Int?,
    @Json(name = "promotion_name") val promotionName: String?,
    @Json(name = "promotion_promocode") val promotionPromocode: String?,
    @Json(name = "discount_value") val discountValue: String?,
    @Json(name = "is_promotion") val isPromotion: Boolean?
)



data class SoapNotesRequest(
    @Json(name = "booking_id")
    val bookingId:Int,
    @Json(name = "img_url")
    val imgUrl:String
)



data class Notes(
    @Json(name = "notes") val notes: SoapNotes
)

@JsonClass(generateAdapter = true)
data class SoapNotes(
    @Json(name = "soap_notes") val soapNotes: List<SoapNote>?,
    @Json(name = "clinic_notes") val clinicNotes: List<ClinicNote>?
)

@JsonClass(generateAdapter = true)
data class SoapNote(
    @Json(name = "subjective") val subjective: String?,
    @Json(name = "objective") val objective: String?,
    @Json(name = "assessment") val assessment: String?,
    @Json(name = "plan") val plan: String?
)

@JsonClass(generateAdapter = true)
data class ClinicNote(
    @Json(name = "note") val note: String?,
    @Json(name = "note1") val note1: String?,
    @Json(name = "note2") val note2: String?

)

