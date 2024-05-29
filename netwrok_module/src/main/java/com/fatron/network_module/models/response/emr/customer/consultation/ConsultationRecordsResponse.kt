package com.fatron.network_module.models.response.emr.customer.consultation


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConsultationRecordsResponse(
    @Json(name = "emr_id")
    var emrId: Int? = null,
    @Json(name = "emr_number")
    val emrNumber: String? = null,
    @Json(name = "speciality")
    var speciality: List<GenericItem>? = null,
    @Json(name = "partner_name")
    val partnerName: String? = null,
    @Json(name = "booking_date")
    var bookingDate: String? = null,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null,
    @Json(name = "is_shared")
    val isShared: Int? = null,
    @Json(name = "date")
    val date: String? = null,
    @Json(name = "shared")
    val shared: List<*>? = null,
){
    var isSelected = false
}