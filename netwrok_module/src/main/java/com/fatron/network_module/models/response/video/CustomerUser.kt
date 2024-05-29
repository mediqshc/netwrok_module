package com.fatron.network_module.models.response.video


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomerUser(
    @Json(name = "full_name")
    val fullName: String?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "profile_picture_id")
    val profilePictureId: Any?,
    @Json(name = "user_device")
    val userDevice: UserDevice?,
    @Json(name = "user_profile_pic")
    val userProfilePic: Any?
)