package com.fatron.network_module.models.response.checkout


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookedForUser(
    @Json(name = "city_id")
    val cityId: Int?,
    @Json(name = "country_id")
    val countryId: Int?,
    @Json(name = "date_of_birth")
    val dateOfBirth: String?,
    @Json(name = "full_name")
    val fullName: String?,
    @Json(name = "gender_id")
    val genderId: Int?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "phone_number")
    val phoneNumber: String?
)