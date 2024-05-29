package com.fatron.network_module.models.response.family


import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.user.UserPhoneNumber
import com.fatron.network_module.models.response.ordersdetails.UserProfilePic
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FamilyConnection (
    @Json(name = "id")
    var id: Int? = 0,

    @Json(name = "user_id")
    var userId: Int? = 0,

    @Json(name = "family_member_id")
    var familyMemberId: Int? = 0,

    @Json(name = "relation")
    var relation: String? = "",

    @Json(name = "relationId")
    var relationId: String? = "",

    @Json(name = "gender_id")
    var genderId: String? = "",

    @Json(name = "full_name")
    var fullName: String? = "",

    @Json(name = "profile_picture")
    var profilePicture: String? = "",

    @Json(name = "phone_number")
    var phoneNumber: String? = null,

    @Json(name = "country_code")
    var countryCode: String? = null,

    @Json(name = "user_profile_pic")
    var userProfilePicture: UserProfilePic? = null,

    @Json(name = "age")
    var age: String? = "",

    @Json(name = "addresses")
    var addresses: List<UserLocation>? = null,

    @Json(name = "to_show_in_emr_share")
    val toShowInEMRShare: Int? = null
){
    var isSelected = false
}