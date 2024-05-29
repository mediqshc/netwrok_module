package com.fatron.network_module.models.request.user


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserRequest(
    @Json(name = "cnic")
    val cnic: String?=null,
    @Json(name = "phone_number")
    val phoneNumber: String?=null,
    @Json(name = "country_code")
    val countryCode: String?=null,
    @Json(name = "date_of_birth")
    val dateOfBirth: String?=null,
    @Json(name = "email")
    val email: String?=null,
    @Json(name = "country_id")
    val countryId: Int?=null,
    @Json(name = "city_id")
    val cityId: Int?=null,
    @Json(name = "full_name")
    val fullName: String?=null,
    @Json(name = "gender")
    val gender: String?=null,
    @Json(name = "gender_id")
    val genderId: Int?=null,
    @Json(name = "profile_picture")
    val profilePicture: String?=null,
    @Json(name = "user_locations")
    val userLocations: List<UserLocation>?=null,
    @Json(name = "user_phone_numbers")
    val userPhoneNumbers: List<UserPhoneNumber>?=null
)