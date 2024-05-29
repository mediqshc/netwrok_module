package com.fatron.network_module.models.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseGeneralArray<T>(
    @Json(name = "message")
    val message: String? = null,
    @Json(name = "errors")
    val errors: List<String>? = arrayListOf(),
    @Json(name = "code")
    val code: Int = 0,
    @Json(name = "update_available")
    val update: Int = 0,
    @Json(name = "data")
    var data: List<T>?,
    @Json(name = "exception")
    val exception: String? = null
)