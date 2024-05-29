package com.fatron.network_module.models.request.walkin


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInRequest(
    @Json(name = "walk_in_pharmacy_id")
    var walkInPharmacyId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null,
    @Json(name = "city_id")
    var cityId: Int? = null,
    @Json(name = "city_name")
    var cityName: String? = null,
    @Json(name = "country_id")
    var countryId: Int? = null,
    @Json(name = "country_name")
    var countryName: String? = null,
    @Json(name = "family_member_id")
    var familyMemberId: Int? = null,
    @Json(name = "amount")
    var amount: String? = null,
    @Json(name = "service_provider")
    var serviceProvider: String? = null,
    @Json(name = "comments")
    var comments: String? = null,
    @Json(name = "service_id")
    var serviceId: Int? = null,


    @Json(name = "walk_in_laboratory_id")
    var walkInLaboratoryId: Int? = null,

    @Json(name = "walk_in_hospital_id")
    var walkInHospitalId: Int? = null,

    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "lab_id")
    val labId: Int? = null,
    @Json(name = "pharmacy_id")
    val pharmacyId: Int? = null,
    @Json(name = "healthcare_id")
    val healthCareId: Int? = null
)