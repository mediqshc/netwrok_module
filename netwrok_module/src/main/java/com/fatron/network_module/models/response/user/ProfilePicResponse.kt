package com.fatron.network_module.models.response.user


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProfilePicResponse(
    @Json(name = "profile_pic")
    val profilePic: String?
)