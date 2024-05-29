package com.fatron.network_module.models.response.ordersdetails

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class UserProfilePic(
    @Json(name = "id")
    @SerializedName("id")
    val id: Int? = null,
    @Json(name = "file")
    @SerializedName("file")
    val file: String? = null
): Serializable