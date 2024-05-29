package com.fatron.network_module.models.response.meta


import com.squareup.moshi.Json

data class MedicalStaffSpecialty(
    @Json(name = "icon_url")
    val iconUrl: String?,
    @Json(name = "name")
    val name: String?
)