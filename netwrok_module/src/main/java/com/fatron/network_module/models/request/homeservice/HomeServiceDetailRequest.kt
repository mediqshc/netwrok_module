package com.fatron.network_module.models.request.homeservice


import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HomeServiceDetailRequest(
    @Json(name = "speciality_id")
    val specialityId: Int?,
    @Json(name = "booking_id")
    var bookingId: Int? = null
)

@JsonClass(generateAdapter = true)
data class HomeServiceListRequest(
    @Json(name = "country_id")
    val countryId: Int? = null,
    @Json(name = "city_id")
    val cityId: Int? = null
)