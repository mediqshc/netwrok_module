package com.fatron.network_module.models.response.walkin


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInBranch(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "street_address")
    var street_address: String? = null,
    @Json(name = "company_id")
    var companyId: Int? = null,
    @Json(name = "type_id")
    var typeId: Int? = null,
    @Json(name = "pharmacy")
    var pharmacy: GenericItem? = null,
    @Json(name = "labs")
    var labs: GenericItem? = null,
    @Json(name = "hospital")
    var hospital: GenericItem? = null,
    @Json(name = "healthcare")
    var healthcare: GenericItem? = null,
)