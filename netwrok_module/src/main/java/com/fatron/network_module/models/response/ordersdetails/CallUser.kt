package com.fatron.network_module.models.response.ordersdetails


import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.ordersdetails.UserProfilePic
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class CallUser(
    @Json(name = "city_id")
    @SerializedName("city_id")
    val cityId: Int? = null,
    @Json(name = "country_id")
    @SerializedName("country_id")
    val countryId: Int? = null,
    @Json(name = "date_of_birth")
    @SerializedName("date_of_birth")
    val dateOfBirth: String? = null,
    @Json(name = "full_name")
    @SerializedName("full_name")
    val fullName: String? = null,
    @Json(name = "gender_id")
    @SerializedName("gender_id")
    val genderId: Int? = null,
    @Json(name = "partner_specialities")
    @SerializedName("partner_specialities")
    val partnerSpecialities: List<GenericItem>? = null,
    @Json(name = "id")
    @SerializedName("id")
    val id: Int? = null,
    @Json(name = "phone_number")
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    @Json(name = "user_profile_pic")
    @SerializedName("user_profile_pic")
    val userProfilePicture: UserProfilePic? = null,
    @Json(name = "user_locations")
    @SerializedName("user_locations")
    var userLocations:List<UserLocation>? = null
) : Serializable