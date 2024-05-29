package com.fatron.network_module.models.response


import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseGeneral<T>(
    @Json(name = "message")
    var message: String? = null,
    @SerializedName("status")
    var status: Int = 0,
    @Json(name = "data")
    var data: T?,
    @Json(name = "error")
    var errors: List<String>?,
    @SerializedName("update_available")
    var update_available: Int?,
    @SerializedName("android_update_available")
    var androidUpdateAvailable: Int?,
    @SerializedName("android_version")
    var androidVersion: Int?,
    @Json(name = "exception")
    var exception: String? = null
)