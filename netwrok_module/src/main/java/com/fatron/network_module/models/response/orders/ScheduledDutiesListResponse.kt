package com.fatron.network_module.models.response.orders


import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class ScheduledDutiesListResponse(
    @Json(name = "data")
    @SerializedName("data")
    val data: List<ScheduledDutyResponse>? = null,
    @Json(name = "current_page")
    @SerializedName("current_page")
    val currentPage: Int? = null,
    @Json(name = "last_page")
    @SerializedName("last_page")
    val lastPage: Int? = null,
) : Serializable