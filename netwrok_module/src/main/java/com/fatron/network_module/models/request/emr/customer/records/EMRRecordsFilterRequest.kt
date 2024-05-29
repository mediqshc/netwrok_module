package com.fatron.network_module.models.request.emr.customer.records


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class EMRRecordsFilterRequest {

    @Json(name = "customer_id")
    var customerId: Int? = null

    @Json(name = "page")
    var page: Int? = null

    @Json(name = "type")
    var type: Int? = null

    @Json(name = "record_number")
    var recordNumber: Int? = null

    @Json(name = "start_date")
    var startDate: String? = null

    @Json(name = "end_date")
    var endDate: String? = null

    @Json(name = "referred_by")
    var referredBy: Int? = null

    @Json(name = "medicine_type")
    var medicineType: Int? = null

    @Json(name = "procedure_type")
    var procedureType: Int? = null

    @Json(name = "shared_record")
    var sharedRecord: Int? = null
}