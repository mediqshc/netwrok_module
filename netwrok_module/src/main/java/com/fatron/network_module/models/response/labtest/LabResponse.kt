package com.fatron.network_module.models.response.labtest


import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LabResponse (
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "type_id")
    var typeId: Int? = null,
    @Json(name = "company_id")
    var companyId: Int? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "currency_id")
    var currencyId: Int? = 0,
    @Json(name = "rate")
    var rate: String? = null,
    @Json(name = "street_address")
    var streetAddress: String? = null,
    @Json(name = "labs")
    var labs: LabResponse? = null,
    @Json(name = "lab")
    var lab: LabResponse? = null,
    @Json(name = "icon_url")
    var iconUrl: String? = null,
    @Json(name = "lab_test_laboratory")
    var labTestLaboratory: List<LabResponse>? = null,
    @Json(name = "latitude")
    var lat: Double? = null,
    @Json(name = "longitude")
    var long: Double? = null
): MultipleViewItem(itemId = id.toString(), title = name, desc = streetAddress, imageUrl = iconUrl)