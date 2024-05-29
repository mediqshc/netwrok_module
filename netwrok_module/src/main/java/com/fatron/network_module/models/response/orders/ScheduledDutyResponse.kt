package com.fatron.network_module.models.response.orders


import com.fatron.network_module.models.response.ordersdetails.CallUser
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ScheduledDutyResponse(
    @Json(name = "id")
    @SerializedName("id")
    val id: Int? = null,
    @Json(name = "partner_user_id")
    @SerializedName("partner_user_id")
    val partnerUserId: Int? = null,
    @Json(name = "duty_detail_id")
    @SerializedName("duty_detail_id")
    val dutyDetailId: Int? = null,
    @Json(name = "booking_id")
    @SerializedName("booking_id")
    val bookingId: Int? = null,
    @Json(name = "date")
    @SerializedName("date")
    val date: String? = null,
    @Json(name = "status_id")
    @SerializedName("status_id")
    val statusId: Int? = null,
    @Json(name = "status_label")
    @SerializedName("status_label")
    val statusLabel: String? = null,
    @Json(name = "start_time")
    @SerializedName("start_time")
    val startTime: String? = null,
    @Json(name = "end_time")
    @SerializedName("end_time")
    val endTime: String? = null,
    @Json(name = "partner_user")
    @SerializedName("partner_user")
    val partnerUser: CallUser? = null,
) : Serializable