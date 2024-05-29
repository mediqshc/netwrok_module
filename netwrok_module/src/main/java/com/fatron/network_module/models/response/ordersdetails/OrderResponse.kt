package com.fatron.network_module.models.response.ordersdetails

import com.fatron.network_module.models.response.appointments.MedicalRecords
import com.fatron.network_module.models.response.chat.ChatProperties
import com.fatron.network_module.models.response.claim.Claim
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.partnerprofile.EducationResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.walkin.WalkIn
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class OrderResponse(
    @Json(name = "booking_details")
    val bookingDetails: BookingDetails?,
    @Json(name = "partner_details")
    val partnerDetails: PartnerProfileResponse?,

    @Json(name = "medical_records")
    val medicalRecord: List<MedicalRecords>? = null,

    // for call and PN
    @Json(name = "booked_for_id")
    @SerializedName("booked_for_id")
    val bookedForId: Int? = null,

    @Json(name = "booked_for_user")
    @SerializedName("booked_for_user")
    val bookedForUser: CallUser? = null,

    @Json(name = "booking_date")
    @SerializedName("booking_date")
    val bookingDate: String? = null,

    @Json(name = "booking_status_id")
    @SerializedName("booking_status_id")
    val bookingStatusId: Int? = null,

    @SerializedName("chat_properties")
    val chatProperties: ChatProperties? = null,

    @Json(name = "chat_session_status")
    val chatSession: String? = null,

    @Json(name = "customer_user")
    @SerializedName("customer_user")
    val customerUser: CallUser? = null,

    @Json(name = "id")
    @SerializedName("id")
    val id: Int? = null,

    @Json(name = "partner_id")
    @SerializedName("partner_id")
    val partnerId: Int? = null,

    @Json(name = "partner_service_id")
    @SerializedName("partner_service_id")
    val partnerServiceId: Int? = null,

    @Json(name = "partner_user")
    @SerializedName("partner_user")
    val partnerUser: CallUser? = null,

    @Json(name = "unique_identification_number")
    @SerializedName("unique_identification_number")
    val uniqueIdentificationNumber: String? = null,

    @Json(name = "user_id")
    @SerializedName("user_id")
    val userId: Int? = null,

    @Json(name = "average_reviews_rating")
    @SerializedName("average_reviews_rating")
    val averageReviewsRating: Double? = null,

    @Json(name = "currency_id")
    @SerializedName("currency_id")
    val currencyId: Int? = null,

    @Json(name = "customer_user_id")
    @SerializedName("customer_user_id")
    val customerUserId: Int? = null,

    @Json(name = "educations")
    @SerializedName("educations")
    val educations: List<EducationResponse>? = null,

    @Json(name = "experience")
    @SerializedName("experience")
    val experience: String? = null,

    @Json(name = "fee")
    @SerializedName("fee")
    val fee: String? = null,

    @Json(name = "full_name")
    @SerializedName("full_name")
    val fullName: String? = null,

    @Json(name = "is_online")
    @SerializedName("is_online")
    val isOnline: Int? = null,

    @Json(name = "overview")
    @SerializedName("overview")
    val overview: String? = null,

    @Json(name = "partner_type_id")
    @SerializedName("partner_type_id")
    val partnerTypeId: String? = null,

    @Json(name = "partner_user_id")
    @SerializedName("partner_user_id")
    val partnerUserId: Int? = null,

    @Json(name = "profile_picture")
    @SerializedName("profile_picture")
    val profilePicture: String?,

    @Json(name = "review")
    @SerializedName("review")
    val review: Review? = null,

    @Json(name = "specialities")
    @SerializedName("specialities")
    val specialities: List<GenericItem>? = null,

    @Json(name = "total_no_of_reviews")
    @SerializedName("total_no_of_reviews")
    val totalNoOfReviews: Int? = null,

    @Json(name = "speciality_id")
    var specialityId: Int? = null,

    @Json(name = "claim")
    var claim: Claim? = null,

    @Json(name = "walk_in_pharmacy")
    var walkInPharmacy: WalkIn? = null,

    @Json(name = "walk_in_laboratory")
    var walkInLaboratory: WalkIn? = null,

    @Json(name = "walk_in_hospital")
    var walkInHospital: WalkIn? = null

) : Serializable