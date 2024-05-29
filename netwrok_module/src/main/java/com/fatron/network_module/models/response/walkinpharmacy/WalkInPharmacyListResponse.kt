package com.fatron.network_module.models.response.walkinpharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInPharmacyListResponse(
    @Json(name = "walk_in_pharmacies")
    val walkInPharmacies: WalkInPharmacies? = null,
    @Json(name = "walk_in_labs")
    val walkInLabs: WalkInPharmacies? = null,
    @Json(name = "walk_in_hospitals")
    val walkInHospitals: WalkInPharmacies? = null,
    @Json(name = "current_page")
    val currentPage: Int? = null,
    @Json(name = "last_page")
    val lastPage: Int? = null
)