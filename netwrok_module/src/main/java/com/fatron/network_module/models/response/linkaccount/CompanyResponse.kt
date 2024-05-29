package com.fatron.network_module.models.response.linkaccount


import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.request.checkout.SplitAmount
import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
 class CompanyResponse(
    @Json(name = "id")
   val id: Int? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "company_id")
    val companyId: Int? = null,
    @Json(name = "package_id")
    val packageId: Int? = null,
    @Json(name = "insurance_id")
    val insuranceId: Int? = null,
    @Json(name = "healthcare_id")
   val healthcareId: Int? = null,
    @Json(name = "valid_till")
    val validTill: String? = null,
    @Json(name = "info")
    val info: InsuranceInfoModel? = null,
    @Json(name = "name")
   val name: String? = null,
    @Json(name = "icon_url")
    val iconUrl: String? = null,
    @Json(name = "amount")
    val amount: Double? = null,
    @Json(name = "currency_id")
    val currencyId: Int? = null
): MultipleViewItem(itemId = id.toString(), title = name, imageUrl = iconUrl, pakgeId = packageId, charges = amount?.toInt())