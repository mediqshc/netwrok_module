package com.fatron.network_module.models.request.orders


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class OrdersRequest {
    @Json(name = "orders_type")
    var ordersType: String? = null
    @Json(name = "page")
    var page: Int? = 1
    @Json(name = "service_type_id")
    var serviceTypeId: Int? = null
}
