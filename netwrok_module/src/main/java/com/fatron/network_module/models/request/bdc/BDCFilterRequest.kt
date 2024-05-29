package com.fatron.network_module.models.request.bdc


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BDCFilterRequest {
    @Json(name = "page")
    var page: String? = "1"

    @Json(name = "partner_type")
    var partnerType: Int? = null

    @Json(name = "service_id")
    var serviceId: Int? = null
    
    @Json(name = "partner_name")
    var partnerName: String? = null

    @Json(name = "country_id")
    var countryId: Int? = null

    @Json(name = "city_id")
    var cityId: Int? = null

    @Json(name = "speciality_id")
    var specialityId: Int? = null

    @Json(name = "gender_id")
    var genderId: Int? = null

    @Json(name = "healthcare_id")
    var healthcareId: Int? = null

    @Json(name = "available_for_video_call")
    var availableForVideoCall: Int? = null

    var countryName: String = ""
    var cityName: String = ""
    var specialityName: String = ""
    var genderName: String = ""
    var serviceName: String = ""
}