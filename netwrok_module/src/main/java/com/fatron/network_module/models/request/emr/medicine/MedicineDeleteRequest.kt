package com.fatron.network_module.models.request.emr.medicine

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicineDeleteRequest(
    @Json(name = "emr_product_id")
    val emrProductId: Int? = null
)