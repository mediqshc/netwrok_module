package com.fatron.network_module.models.request.offdates

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class OffDatesRequest {
    @Json(name = "end_date_time")
    var endDateTime: String? = null
    @Json(name = "services_ids")
    var servicesIds: List<Int> = emptyList()
    @Json(name = "start_date_time")
    var startDateTime: String? = null
    @Json(name = "force_delete")
    var forceDelete: Int? = 0
}