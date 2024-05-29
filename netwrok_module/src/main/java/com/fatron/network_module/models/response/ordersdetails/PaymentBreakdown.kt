package com.fatron.network_module.models.response.ordersdetails

import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.response.labtest.LabTestResponse
import com.fatron.network_module.models.response.meta.GenericItem
import com.fatron.network_module.models.response.pharmacy.Product
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class PaymentBreakdown(
    @Json(name = "duties")
    val duties: List<SplitAmount>?,
    @Json(name = "sub_total")
    val subTotal: String?,
    @Json(name = "corporate_discount")
    val corporateDiscount: String?,
    @Json(name = "promo_discount")
    val promoDiscount: String?,
    @Json(name = "company_credit")
    val companyCredit: String?,
    @Json(name = "lab_discount")
    val labDiscount: String?,
    @Json(name = "paid_in_advance")
    val paidInAdvance: String?,
    @Json(name = "payment_to_be_collected")
    val paymentCollected: String?,
    @Json(name = "payment_method")
    val paymentMethod: String?,
    @Json(name = "total")
    val total: String?,
    @Json(name = "total_fee")
    val totalFee: String?,
    @Json(name = "payable_amount")
    val payableAmount: String?,
    @Json(name = "products")
    val products: List<Product>? = null,
    @Json(name = "labs")
    val labs: List<LabTestResponse>? = null,
    @Json(name = "currency_id")
    val currencyId: Int? = null,
    @Json(name = "sample_charges")
    val sampleCharges: String? = null,
    @Json(name = "walk_in_name")  //walkin
    val walkInName: String? = null,
    @Json(name = "package_name")
    val packageName: String? = null,
    @Json(name = "package_amount")
    val packageAmount: String? = null,
    @Json(name = "items")
    val items: List<GenericItem>? = null
): Serializable