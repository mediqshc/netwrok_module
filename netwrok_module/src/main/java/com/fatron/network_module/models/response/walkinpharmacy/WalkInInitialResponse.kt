package com.fatron.network_module.models.response.walkinpharmacy

import com.fatron.network_module.models.response.meta.RequiredDocumentType
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkInInitialResponse(
    @Json(name = "walk_in_pharmacy")
    val walkInPharmacy: WalkInPharmacy? = null,
    @Json(name = "walk_in_laboratory")
    val walkInLaboratory: WalkInPharmacy? = null,
    @Json(name = "walk_in_hospital")
    val walkInHospital: WalkInPharmacy? = null
)

data class WalkInPharmacy(
    @Json(name = "walk_in_pharmacy_id")
    val walkInPharmacyId: Int? = null,
    @Json(name = "walk_in_laboratory_id")
    val walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_hospital_id")
    val walkInHospitalId: Int? = null,
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "family_members")
    val familyMembers: List<FamilyMember>? = null,
    @Json(name = "document_types")
    var documentTypes: List<RequiredDocumentType>? = null
)

data class FamilyMember(
    @Json(name = "addresses")
    val addresses: List<AddressLocation>? = null,
    @Json(name = "family_member_id")
    val familyMemberId: Int? = null,
    @Json(name = "full_name")
    var fullName: String? = null,
    @Json(name = "gender_id")
    val genderId: Int? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "relation")
    val relation: String? = null,
    @Json(name = "user_id")
    val userId: Int? = null
)

data class AddressLocation(
    @Json(name = "address")
    val address: String? = null,
    @Json(name = "category")
    val category: Int? = null,
    @Json(name = "floor_unit")
    val floorUnit: String? = null,
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "lat")
    val lat: String? = null,
    @Json(name = "long")
    val long: String? = null,
    @Json(name = "other")
    val other: String? = null,
    @Json(name = "region")
    val region: String? = null,
    @Json(name = "street")
    val street: String? = null,
    @Json(name = "sublocality")
    val subLocality: String? = null
)

@JsonClass(generateAdapter = true)
data class ServiceTypes(
    @Json(name = "online")
    val online: Int? = null,
    @Json(name = "walk_in_cashless")
    val walkInCashless: Int? = null,
    @Json(name = "walk_in_discount_center")
    val walkInDiscountCenter: Int? = null
)