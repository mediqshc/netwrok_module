package com.fatron.network_module.models.response.claim


import com.fatron.network_module.models.request.claim.PriorAuthorizationRequest
import com.fatron.network_module.models.response.appointments.Attachment
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.meta.RequiredDocumentType
import com.fatron.network_module.models.response.ordersdetails.BookingDetails
import com.fatron.network_module.models.response.ordersdetails.PaymentBreakdown
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Claim(
    @Json(name = "claim_id")
    var claimId: Int? = null,
    @Json(name = "booking_id")
    var bookingId: Int? = null,
    @Json(name = "claim_category_id")
    var claimCategoryId: Int? = null,
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
    @Json(name = "id")
    var id: Int? = null,
    @Json(name = "connection_id")
    var connection_id: Int? = null,
    @Json(name = "connection")
    var connection: ClaimConnection? = null,
    @Json(name = "settlements")
    val settlements: List<Settlements>? = null,
    @Json(name = "settlement_documents")
    var settlementDocuments: List<RequiredDocumentType>? = null,
    @Json(name = "amount")
    var amount: String? = null,
    @Json(name = "service_provider")
    var serviceProvider: String? = null,
    @Json(name = "comments")
    var comments: String? = null,
    @Json(name = "created_at")
    var createdAt: String? = null,
    @Json(name = "attachments")
    var claimAttachments: List<Attachment>? = null,
    @Json(name = "document_types")
    var documentTypes: List<RequiredDocumentType>? = null
)

@JsonClass(generateAdapter = true)
data class Settlements(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "booking_id")
    val bookingId: Int? = null,
    @Json(name = "actual_amount")
    val actualAmount: String? = null
)

@JsonClass(generateAdapter = true)
data class PriorAuthorizationResponse(
    @Json(name = "result")
    val result: String? = null
)

@JsonClass(generateAdapter = true)
data class PriorAuthorizationResponseList(
    @Json(name = "result")
    val result: ArrayList<PriorAuthorizationRequest>? = null
)
@JsonClass(generateAdapter = true)
data class PriorAuthorization(
    val id: Int,
    val patient_name: String,
)