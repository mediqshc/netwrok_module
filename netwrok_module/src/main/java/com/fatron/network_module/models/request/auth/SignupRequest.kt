package com.fatron.network_module.models.request.auth

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignupRequest(
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "country_code")
    val countryCode: String,
    @Json(name = "phone_number")
    val phoneNumber: String,
    @Json(name = "gender")
    val gender: String,
    @Json(name = "date_of_birth")
    val dateOfBirth: String,
    @Json(name = "country_id")
    val countryId: Int,
    @Json(name = "city_id")
    val cityId: String,
    @Json(name = "password")
    val password: String,
    @Json(name = "password_confirmation")
    val passwordConfirm: String,
    @Json(name = "otp")
    val otp: String,
    @Json(name = "gender_id")
    val genderId: Int?,
    @Json(name = "is_health_care_professional")
    val isHealthCareProfessional: Int?=0,
    @Json(name = "device_token")
    var deviceToken: String = "",
    @Json(name = "fcm_token")
    var fcmToken: String = "",
    @Json(name = "device_meta")
    var deviceMeta: String = ""
)
