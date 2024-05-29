package com.fatron.network_module.models.response.labtest


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LabTestResponse(
    @Json(name = "id")
    var id: Int? = null,

    @Json(name = "name")
    var name: String? = "",

    @Json(name = "description")
    var description: String? = "",

    @Json(name = "total_fee")
    var totalFee: String? = "",

    @Json(name = "rate")
    var rate: String? = "",

    @Json(name = "currency_id")
    var currencyId: Int? = 0,

    @Json(name = "discount")
    var discount: String? = "",

    @Json(name = "branch_id")
    var branchId: Int? = null,

    @Json(name = "booking_id")
    var bookingId: Int? = null,

    @Json(name = "lab_id")
    var labId: Int? = null,

    @Json(name = "lab_test_id")
    var labTestId: Int? = null,

    @Json(name = "lab_test")
    var labTest: GenericItem? = null,

    @Json(name = "lab_test_laboratory")
    var labTestLaboratory: List<LabResponse>? = null,

    @Json(name = "booking_laboratory")
    var bookingLaboratory: LabResponse? = null,

    //selected
    @Json(name = "lab")
    val lab: LabResponse? = null,

    //selected
    @Json(name = "branch")
    val branch: LabResponse? = null,
)