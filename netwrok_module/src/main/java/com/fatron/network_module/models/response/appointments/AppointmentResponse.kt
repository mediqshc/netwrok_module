package com.fatron.network_module.models.response.appointments


import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.response.chat.ChatProperties
import com.fatron.network_module.models.response.emr.type.GenericMedicalRecord
import com.fatron.network_module.models.response.ordersdetails.CallUser
import com.fatron.network_module.models.response.ordersdetails.PaymentBreakdown
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class AppointmentResponse (
    @Json(name ="id")
    var id: Int? = null,

    @Json(name ="booking_id")
    var bookingId: Int? = null,

    @Json(name = "full_name")
    var fullName: String? = null,

    @Json(name = "duty_id")
    var dutyId: Int? = null,

    @Json(name = "user_id")
    var userId: Int? = null,

    @Json( name ="partner_id")
    var partnerId: Int? = null,

    @Json(name = "booked_for_id")
    var bookedForId: Int? = null,

    @Json(name = "partner_service_id")
    var partnerServiceId: Int? = null,

    @Json(name = "speciality_id")
    var specialityId: String? = null,

    @Json(name = "partner_service")
    var partnerService: String? = null,

    @Json(name = "user_location_id")
    var userLocationId: Int? = null,

    @Json(name = "booking_date")
    var bookingDate: String? = null,

    @Json(name = "booking_date_original")
    var bookingDateOriginal: String? = null,

    @Json(name = "day_id")
    var dayId: Int? = null,

    @Json(name = "start_time")
    var startTime: String? = null,

    @Json(name = "end_time")
    var endTime: String? = null,

    @Json(name = "time_left")
    val timeLeft: String? = null,

    @Json(name = "chat_properties")
    val chatProperties: ChatProperties? = null,

    @Json(name = "chat_session_status")
    val chatSessionStatus: String? = null,

    @Json(name = "instructions")
    var instructions: String? = null,

    @Json(name = "fee")
    var fee: String? = null,

    @Json(name = "fee_collected")
    var feeCollected: Int? = null,

    @Json(name = "unique_identification_number")
    var uniqueIdentificationNumber: String? = null,

    @Json(name = "currency_id")
    var currencyId: Int? = null,

    @Json(name = "customer_id")
    var customerId: Int? = null,

    @Json(name = "booking_status_id")
    var bookingStatusId: Int? = null,

    @Json(name = "booked_by")
    val booked_by: String? = null,

    @Json(name = "start")
    val start: Boolean? = null,

    @Json(name = "booking_status")
    val bookingStatus: String? = null,

    @Json(name = "patient_details")
    val patientDetails: PatientDetail?=null,

    @Json(name = "patient_location")
    val patientLocation:  UserLocation?=null,

    @Json(name = "service_type")
    val serviceType: String?=null,

    @Json(name = "service_type_id")
    val serviceTypeId: String? = null,

    @Json(name = "shift")
    val shift: Shift?=null,

    @Json(name = "read")
    val read: Int?=null,

    @Json(name = "booked_for_user")
    @SerializedName("booked_for_user")
    val bookedForUser: CallUser? = null,

    @Json(name = "customer_user")
    @SerializedName("customer_user")
    val customerUser: CallUser? = null,

    @Json(name = "partner_user")
    @SerializedName("partner_user")
    val partnerUser: CallUser? = null,


    @Json(name = "payment_breakdown")
    @SerializedName("payment_breakdown")
    val paymentBreakdown: PaymentBreakdown? = null,

    @Json(name = "medical_records")
    @SerializedName("medical_records")
    val medicalRecord: List<MedicalRecords>? = null

    ) : Serializable

data class Shift(
    @Json(name = "shift")
    val shift: String,
    @Json(name = "shift_id")
    val shiftId: Int
) : Serializable

data class PatientDetail(
    @Json(name = "age")
    val age: Int?=null,
    @Json(name = "full_name")
    val fullName: String?=null,
    @Json(name = "gender")
    val gender: String?=null,
    @Json(name = "date_of_birth")
    val dateOfBirth: String?=null,
    @Json(name = "gender_id")
    val genderId: Int?=null,
    @Json(name = "profile_picture")
    val profilePicture: String?=null
) : Serializable

