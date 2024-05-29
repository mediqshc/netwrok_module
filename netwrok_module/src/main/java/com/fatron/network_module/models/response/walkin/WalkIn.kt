package com.fatron.network_module.models.response.walkin


import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.claim.ClaimConnection
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.RequiredDocumentType
import com.fatron.network_module.models.response.ordersdetails.BookingDetails
import com.fatron.network_module.models.response.ordersdetails.Review
import com.fatron.network_module.models.response.ordersdetails.SettlementDocumentRequired
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WalkIn(
    @Json(name = "walk_in_pharmacy_id")
    var walkInPharmacyId: Int? = null,
    @Json(name = "walk_in_laboratory_id")
    var walkInLaboratoryId: Int? = null,
    @Json(name = "walk_in_hospital_id")
    var walkInHospitalId: Int? = null,
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "pharmacy_id")
    var pharmacyId: Int? = null,
    @Json(name = "laboratory_id")
    var laboratoryId: Int? = null,
    @Json(name = "healthcare_id")
    var healthcareId: Int? = null,
    @Json(name = "family_members")
    val familyMembers: List<FamilyConnection>?,

    @Json(name = "city_id")
    var cityId: Int? = null,
    @Json(name = "city_name")
    var cityName: String? = null,
    @Json(name = "country_id")
    var countryId: Int? = null,
    @Json(name = "country_name")
    var countryName: String? = null,
    @Json(name = "patient_id")
    var patientId: Int? = null,
    @Json(name = "currency_id")
    var currencyId: Int? = null,

    //for details screen
    @Json(name = "booking_details")
    var bookingDetails: BookingDetails? = null,
    @Json(name = "branch")
    var branch: WalkInBranch? = null,
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "connection_id")
    var connection_id: Int? = null,
    @Json(name = "connection")
    var connection: ClaimConnection? = null,
    @Json(name = "settlement_documents")
    var settlementDocuments: List<RequiredDocumentType>? = null,
    @Json(name = "settlement_documents_required")
    var settlementDocumentsRequired: List<SettlementDocumentRequired>? = null,
    @Json(name = "amount")
    var amount: String? = null,
    @Json(name = "service_provider")
    var serviceProvider: String? = null,
    @Json(name = "created_at")
    var createdAt: String? = null,
    @Json(name = "attachments")
    var claimAttachments: List<Attachment>? = null,
    @Json(name = "review")
    var review: Review? = null,
    @Json(name = "document_types")
    val documentTypes: List<RequiredDocumentType>? = null,
)