package com.fatron.network_module.models.response.bdc


import com.fatron.network_module.models.response.partnerprofile.DateSlotResponse
import com.fatron.network_module.models.response.partnerprofile.PatientResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class PartnerSlotsResponse : UserResponse() {
    @Json(name = "fees")
    var fee: String? = ""

    @Json(name = "currency")
    var currency: String? = ""

    @Json(name = "currency_id")
    var currencyId: Int? = null

    @Json(name = "patients")
    var patients:  List<PatientResponse>? = null

    @Json(name = "slots")
    var dateSlots:  List<DateSlotResponse>? = null

    @Json(name = "is_available_in_city")
    var isAvailableInCity: Int? = 0
}