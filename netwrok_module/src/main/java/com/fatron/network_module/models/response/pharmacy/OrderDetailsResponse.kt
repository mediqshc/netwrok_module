package com.fatron.network_module.models.response.pharmacy

import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.labtest.LabResponse
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.ordersdetails.PaymentBreakdown
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderDetailsResponse(
    @Json(name = "attachments")
    val attachments: List<Attachment>? = null,
    @Json(name = "booking")
    val booking: Booking? = null,
    @Json(name = "family_members")
    val familyMembers: List<FamilyConnection>? = null,
    @Json(name = "products_count")
    val productsCount: Int? = null,
    @Json(name = "payment_breakdown")
    val paymentBreakdown: PaymentBreakdown? = null,
    @Json(name = "products")
    val products: List<Product>? = null,
    @Json(name = "cart_items")
    val labCartItems: List<LabTestResponse>? = null,
)