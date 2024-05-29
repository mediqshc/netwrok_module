package com.fatron.network_module.models.request.appointments


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppointmentDetailReq(
    @Json(name = "booking_id")
    val bookingId: String,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,
    @Json(name = "duty_id")
    val dutyId: Int? = null,
    @Json(name = "renew_consultation")
    var renewConsultation: Int? = null,
    @Json(name = "promo_code")
    val promoCode: String? = null,
    @Json(name = "promotion_id")
    val promotionId: Int? = null,
    @Json(name = "amount")
    val amount: String? = null
)