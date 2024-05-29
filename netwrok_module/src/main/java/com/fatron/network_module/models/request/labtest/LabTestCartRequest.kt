package com.fatron.network_module.models.request.labtest


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LabTestCartRequest(
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "lab_test_id")
    var labTestId: Int? = null,
    @Json(name = "lab_id")
    var labId: Int? = null,
    @Json(name = "branch_id")
    var branchId: Int? = null,

//    @Json(name = "rate")
//    var rate: Int? = null,
//    @Json(name = "discount")
//    var discount: Int? = null,
//    @Json(name = "subtotal")
//    var subtotal: Int? = null,

    @Json(name = "item_id") //for delete
    var item_id: List<Int>? = null,
)