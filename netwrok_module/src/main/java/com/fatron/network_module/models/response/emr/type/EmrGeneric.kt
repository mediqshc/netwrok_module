package com.fatron.network_module.models.response.emr.type


import com.fatron.network_module.models.generic.MultipleViewItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EmrGeneric(
    @Json(name = "description")
    val description: String?,
    @Json(name = "emr_id")
    val emrId: Int?,
    @Json(name = "id")
    val id: Int?,
    @Json(name = "name")
    val name: String?,
    @Json(name = "symptoms_id")
    val symptomsId: Int?,
    @Json(name = "diagnosis_id")
    val diagnosisId: Int?
): MultipleViewItem(itemId = id.toString(), title = name, desc = description)