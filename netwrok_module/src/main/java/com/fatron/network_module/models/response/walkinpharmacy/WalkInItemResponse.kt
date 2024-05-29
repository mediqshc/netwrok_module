package com.fatron.network_module.models.response.walkinpharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInItemResponse(
    @Json(name = "city_id")
    val cityId: Int? = null,
    @Json(name = "contract_document_id")
    val contractDocumentId: Int? = null,
    @Json(name = "country_id")
    val countryId: Int? = null,
    @Json(name = "created_at")
    val createdAt: String? = null,
    @Json(name = "deposit_threshold")
    val depositThreshold: Int? = null,
    @Json(name = "display_name")
    val displayName: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "info")
    val info: Any? = null,
    @Json(name = "is_active")
    val isActive: Int? = null,
    @Json(name = "kilometer")
    val kilometer: Double? = null,
    @Json(name = "latitude")
    val latitude: Double? = null,
    @Json(name = "logo")
    val logo: String? = null,
    @Json(name = "longitude")
    val longitude: Double? = null,
    @Json(name = "online_revenue")
    val onlineRevenue: Int? = null,
    @Json(name = "online_service")
    val onlineService: Int? = null,
    @Json(name = "registered_name")
    val registeredName: String? = null,
    @Json(name = "security_deposit")
    val securityDeposit: Int? = null,
    @Json(name = "street_address")
    val streetAddress: String? = null,
    @Json(name = "tenant_id")
    val tenantId: Int? = null,
    @Json(name = "updated_at")
    val updatedAt: String? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "valid_till")
    val validTill: String? = null,
    @Json(name = "walkin_revenue")
    val walkinRevenue: Int? = null,
    @Json(name = "walkin_service")
    val walkinService: Int? = null,
    @Json(name = "whatsapp_number")
    val whatsappNumber: String? = null,
    @Json(name = "online")
    val online: Int? = null,
    @Json(name = "walkin")
    val walkIn: Int? = null,
    @Json(name = "country_code")
    val countryCode: String? = null,
    @Json(name = "contact_number")
    val contactNumber: String? = null,
    @Json(name = "company_id")
    val companyId: Int? = null,
    @Json(name = "email")
    val email: String? = null,
    @Json(name = "branch_code")
    val branchCode: String? = null
)