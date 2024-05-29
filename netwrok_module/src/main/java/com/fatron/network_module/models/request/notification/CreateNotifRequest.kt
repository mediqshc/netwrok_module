package com.fatron.network_module.models.request.notification


import com.fatron.network_module.models.response.ordersdetails.BookingDetails
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class NotificationsItem(
    @Json(name = "booking_id")
    val bookingId: String? =null,
    @Json(name = "entity_id")
    val entityId: String? =null,
    @Json(name = "notify_user_id")
    val notifyUserId: Int? = null,
    @Json(name = "title")
    val title: String?=null,
    @Json(name = "body")
    val body: String?=null,
    @Json(name = "properties")
    val properties: PropertiesObj?=null
)


@JsonClass(generateAdapter = true)
data class CreateNotifRequest(
    @Json(name = "notifications")
    val notifications: List<NotificationsItem>?=null
)
@JsonClass(generateAdapter = true)
data class PropertiesObj(
    @Json(name = "sid")
    @SerializedName("sid")
    val sid: String?=null,
    @Json(name = "booking_id")
    @SerializedName("booking_id")
    val bookingId: Int?=null,
    @Json(name = "partner_service_id")
    @SerializedName("partner_service_id")
    val partnerServiceId: Int?=null,
    @Json(name = "booking_details")
    @SerializedName("booking_details")
    val bookingDetail: BookingDetail?=null,
    @Json(name = "duty_id")
    @SerializedName("duty_id")
    val dutyId: Int?=null
):Serializable


@JsonClass(generateAdapter = true)
data class BookingDetail(
    @Json(name = "booking_id")
    @SerializedName("booking_id")
    val bookingId: Int?=null,
    @Json(name = "partner_service_id")
    @SerializedName("partner_service_id")
    val partnerServiceId: Int?=null,
    @Json(name = "walk_in_pharmacy_id")
    @SerializedName("walk_in_pharmacy_id")
    var walkInPharmacyId: Int? = null,
    @Json(name = "walk_in_laboratory_id")
    @SerializedName("walk_in_laboratory_id")
    var walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_hospital_id")
    @SerializedName("walk_in_hospital_id")
    var walkInHospitalId: Int? = null,
    @Json(name = "claim_id")
    @SerializedName("claim_id")
    var claimId: Int? = null,
    @Json(name = "id")
    @SerializedName("id")
    val id: Int? = null,
    @Json(name = "user_id")
    @SerializedName("user_id")
    val userid: Int? = null,
    @Json(name = "claim_category_id")
    @SerializedName("claim_category_id")
    val claimCategoryId: Int? = null,
):Serializable

@JsonClass(generateAdapter = true)
data class BookingDetailsNotification(
    @Json(name = "booking_details")
    @SerializedName("booking_details")
    val bookingDetail: BookingDetail? = null
)


