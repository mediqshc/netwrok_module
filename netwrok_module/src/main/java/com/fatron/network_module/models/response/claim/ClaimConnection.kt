package com.fatron.network_module.models.response.claim


import com.fatron.network_module.models.response.meta.GenericItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ClaimConnection(
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "company_id")
    var companyId: Int? = null,
    @Json(name = "currency_id")
    var currencyId: Int? = null,
    @Json(name = "company_type_id")
    var companyTypeId: Int? = null,
    @Json(name = "package_id")
    var packageId: String? = null,
    @Json(name = "customer_id")
    var customerId: Int? = null,
    @Json(name = "package")
    var claimPackage: ClaimPackage? = null,
    @Json(name = "company")
    var company: GenericItem? = null,
    @Json(name = "insurance")
    var insurance: GenericItem? = null,
    @Json(name = "company_employee_id")
    val companyEmployeeId: Int? = null,
    @Json(name = "on_hold")
    val onHold: Int? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null,
): GenericItem(genericItemId = id, genericItemName = company?.genericItemName, description = claimPackage?.code)