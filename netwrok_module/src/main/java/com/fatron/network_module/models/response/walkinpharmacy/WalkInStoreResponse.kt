package com.fatron.network_module.models.response.walkinpharmacy

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInStoreResponse(
    @Json(name = "amount")
    val amount: Double? = null,
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "connection_id")
    val connectionId: Int? = null,
    @Json(name = "family_member_id")
    val familyMemberId: Int? = null,
    @Json(name = "pharmacy_id")
    val pharmacyId: Int? = null,
    @Json(name = "rr_log_request_id")
    val rrLogRequestId: String? = null,
    @Json(name = "unique_identification_number")
    val uniqueIdentificationNumber: String? = null,
    @Json(name = "user_id")
    val userId: Int? = null,
    @Json(name = "user_type_id")
    val userTypeId: Int? = null,
    @Json(name = "walk_in_pharmacy")
    val walkInPharmacy: Int? = null,
    @Json(name = "walk_in_pharmacy_id")
    val walkInPharmacyId: Int? = null,
    @Json(name = "walk_in_laboratory_id")
    val walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_hospital_id")
    val walkInHospitalId: Int? = null,
    @Json(name = "healthcare_id")
    val healthcareId: Int? = null,
    @Json(name = "service_id")
    val serviceId: Int? = null,
    @Json(name = "lab_id")
    val labId: Int? = null,
    @Json(name = "walk_in_laboratory")
    val walkInLaboratory: Int? = null,
    @Json(name = "x_city_id")
    val xCityId: Int? = null,
    @Json(name = "x_country_id")
    val xCountryId: Int? = null,
    @Json(name = "x_device_type")
    val xDeviceType: String? = null,
    @Json(name = "x_local")
    val xLocal: String? = null,
    @Json(name = "x_tenant_currency")
    val xTenantCurrency: Int? = null,
    @Json(name = "x_tenant_id")
    val xTenantId: Int? = null,
    @Json(name = "x_user_timezone")
    val xUserTimezone: String? = null
)