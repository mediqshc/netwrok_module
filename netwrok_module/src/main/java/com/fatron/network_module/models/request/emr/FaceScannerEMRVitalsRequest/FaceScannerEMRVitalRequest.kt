package com.fatron.network_module.models.request.emr.FaceScannerEMRVitalsRequest
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass




@JsonClass(generateAdapter = true)
data class BioMarkerVitalSign(
    @Json(name = "id")
    val id: Int,

    @Json(name = "vital")
    val vital: String,

    @Json(name = "value")
    val value: String
)

@JsonClass(generateAdapter = true)
data class VitalSignsRequest(
    @Json(name = "vitalSigns") // 🔥 change to "vital_signs" if your API expects snake_case
    val vitalSigns: List<BioMarkerVitalSign>
)