package com.fatron.network_module.models.request.checkout


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SplitAmount(
    @Json(name = "duty_id")
    val dutyId: Int? = null,
    @Json(name = "speciality_id")
    val specialityId: Int? = null,
    @Json(name = "speciality_name")
    var specialityName: String? =null,
    @Json(name = "fee")
    val fee: String? = null,
    @Json(name = "discount")
    val discount: Int? = null,
    @Json(name = "days_quantity")
    val daysQuantity: String? = null,
    @Json(name = "start_date")
    val startDate: String? = null,
    @Json(name = "end_date")
    val endDate: String? = null,
    @Json(name = "start_time")
    val startTime: String? = null,
    @Json(name = "end_time")
    val endTime: String? = null,
    @Json(name = "days")
    val days: List<String>? = null,

    var isAvailable: Int? = null,
    var isSubstitute: Int? = null

): java.io.Serializable