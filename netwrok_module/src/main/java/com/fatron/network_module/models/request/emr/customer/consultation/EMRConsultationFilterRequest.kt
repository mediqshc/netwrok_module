package com.fatron.network_module.models.request.emr.customer.consultation


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class EMRConsultationFilterRequest {
    @Json(name = "customer_id")
    var customerId: Int? = null

    @Json(name = "record_number")
    var recordNumber: Int? = null

    @Json(name = "start_date")
    var startDate: String? = null

    @Json(name = "end_date")
    var endDate: String? = null

    @Json(name = "service_type")
    var serviceType: Int? = null

    @Json(name = "diagnosis_id")
    var diagnosisId: Int? = null

    @Json(name = "symptoms_id")
    var symptomId: Int? = null

    @Json(name = "speciality_id")
    var specialityId: Int? = null

    @Json(name = "shared_record")
    var sharedRecord: Int? = null

    @Json(name = "page")
    var page: Int? = null

    var specialityName: String = ""
    var serviceName: String = ""
}