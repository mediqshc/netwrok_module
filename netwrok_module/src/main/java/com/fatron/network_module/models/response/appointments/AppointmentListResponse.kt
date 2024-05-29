package com.fatron.network_module.models.response.appointments


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class AppointmentListResponse {
    @Json(name = "upcoming")
    var upcoming: AppointmentListTabResponse? = null

    @Json(name = "history")
    var history: AppointmentListTabResponse? = null

    @Json(name = "unread")
    var unread: AppointmentListTabResponse? = null
}

@JsonClass(generateAdapter = true)
class AppointmentListTabResponse {
    @Json(name = "current_page")
    var currentPage: Int? = null

    @Json(name = "last_page")
    var lastPage: Int? = null

    @Json(name = "total")
    var total: Int? = null

    @Json(name = "data")
    var data: List<AppointmentResponse>? = arrayListOf()
}