package com.fatron.network_module.models.response.partnerprofile


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PartnerAvailabilityResponse(
    @Json(name = "availability_options")
    val availabilityOptions: List<String>? = null,
    @Json(name = "available_now")
    val availableNow: Int? = null,
    @Json(name = "available_till")
    val availableTill: String? = null,
    @Json(name = "available_slots")
    val availableSlots: Int? = null
)