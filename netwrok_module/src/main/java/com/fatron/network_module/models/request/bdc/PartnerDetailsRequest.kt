package com.fatron.network_module.models.request.bdc


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartnerDetailsRequest (
    @Json(name = "partner_user_id")
    var partnerUserId: Int? = null,

    @Json(name = "service_id")
    var serviceId: Int? = null,

    @Json(name = "booking_id")
    var bookingId: Int? = null,

    @Json(name = "city_id")
    var cityId: Int? = null
)