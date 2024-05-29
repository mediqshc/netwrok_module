package com.fatron.network_module.models.response.ordersdetails

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrdersListResponse(
    @Json(name = "last_page")
    @SerializedName("last_page")
    val lastPage: Int?,
    @Json(name = "orders")
    @SerializedName("orders")
    val orders: List<OrderResponse>?
)