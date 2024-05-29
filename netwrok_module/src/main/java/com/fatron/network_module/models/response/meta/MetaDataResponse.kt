package com.fatron.network_module.models.response.meta


import com.fatron.network_module.models.generic.MultipleViewItem
import com.fatron.network_module.models.response.language.LanguageItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MetaDataResponse(
    @Json(name = "cities")
    val cities: List<City>? = null,
    @Json(name = "all_countries")
    val allCountries: List<Country>? = null,
    @Json(name = "countries")
    val countries: List<Country>? = null,
    @Json(name = "days")
    val days: List<GenericItem>? = null,
    @Json(name = "genders")
    var genders: List<GenericItem>? = null,
    @Json(name = "location_categories")
    var locationCategories: List<GenericItem>? = null,
    @Json(name = "partner_types")
    val partnerTypes: List<GenericItem>? = null,
    @Json(name = "partner_services_types")
    val partnerServiceType: List<PartnerService>? = null,
    @Json(name = "partner_services_types_for_emr_customer")
    val partnerServiceTypeForCMR: List<PartnerService>? = null,
    @Json(name = "phone_categories")
    var phoneCategories: List<GenericItem>? = null,
    @Json(name = "max_file_size")
    var maxFileSize: Long? = null,
    @Json(name = "family_member_relations")
    var familyMemberRelations: List<GenericItem>? = null,
    @Json(name = "doctor_services")
    var doctorServices: List<GenericItem>? = null,
    @Json(name = "keys")
    val keys: Keys? = null,
    @Json(name = "specialties")
    val specialties: Specialties,
    @Json(name = "slot_intervals")
    val slotIntervals: List<SlotIntervals>? = null,
    @Json(name = "order_statuses")
    var orderStatuses: List<GenericItem>? = null,
    @Json(name = "duty_statuses")
    var dutyStatuses: List<GenericItem>? = null,
    @Json(name = "booking_rejected_statuses")
    var bookingRejectedStatuses: List<GenericItem>? = null,
    @Json(name = "booking_completed_statuses")
    var bookingCompletedStatuses: List<GenericItem>? = null,
    @Json(name = "appointment_start_time")
    var appointmentStartTime: List<GenericItem>? = null,
    @Json(name = "home_service_visit_types")
    var homeServiceVisitTypes: List<GenericItem>? = null,
    @Json(name = "currencies")
    var currencies: List<GenericItem>? = null,
    @Json(name = "call_ringing_time")
    var callRingingTime: Int? = null,
    @Json(name = "emr_attachment_types")
    val emrAttachmentTypes: List<GenericItem>? = null,
    @Json(name = "emr_vitals_units")
    val emrVitalsUnits: List<GenericItem>? = null,
    @Json(name = "emr_types")
    val emrTypes: List<GenericItem>? = null,
    @Json(name = "customer_emr_types")
    val customerEmrTypes: List<GenericItem>? = null,
    @Json(name = "emr_vitals")
    val emrVitals: List<EMRVitalsMeta>? = null,
    @Json(name = "dosage_quantity")
    val dosageQuantity: List<GenericItem>? = null,
    @Json(name = "product_types")
    val productTypes: List<GenericItem>? = null,
    @Json(name = "consultation_messages_statuses")
    val consultationMessagesStatuses: List<GenericItem>? = null,
    @Json(name = "paymob_integration_id")
    val paymobIntegrationId: PaymobIntegrationId? = null,
    @Json(name = "payment_methods")
    val paymentMethods: List<PaymentMethod>? = null,
    @Json(name = "notification_types")
    val notificationTypes: List<NotificationType>? = null,
    @Json(name = "notification_categories")
    val notificationCategories: List<NotificationCategory>? = null,
    @Json(name = "default_country_id")
    val defaultCountryId: Int? = null,
    @Json(name = "default_city_id")
    val defaultCityId: Int? = null,
    @Json(name = "otp_resend_time")
    val otpRespondTime: Int? = null,
    @Json(name = "home_menu_items")
    val homeMenuItems: List<HomeMenuItem>? = null,
    @Json(name = "tenant_languages")
    val tenantLanguageItem: List<LanguageItem>? = null,
    @Json(name = "faq_url")
    val faqUrl: String? = null,
    @Json(name = "claim_document_type")
    val claimDocumentType: List<RequiredDocumentType>? = null,
    @Json(name = "walk_in_pharmacy_document_type")
    val walkInPharmacyDocumentType: List<RequiredDocumentType>? = null,
    @Json(name = "walk_in_laboratory_document_type")
    val walkInLaboratoryDocumentType: List<RequiredDocumentType>? = null,
    @Json(name = "walk_in_hospital_document_type")
    val walkInHospitalDocumentType: List<RequiredDocumentType>? = null,
    @Json(name = "walk_in_and_claim_statuses")
    val claimWalkInStatuses: List<GenericItem>? = null,
    @Json(name = "claim_categories")
    val claimCategories: List<GenericItem>? = null,
    @Json(name = "walk_in_hospital_categories")
    val walkInHospitalCategories: List<GenericItem>? = null,

)

@JsonClass(generateAdapter = true)
data class Specialties(
    @Json(name = "doctor_specialties")
    val doctorSpecialties: List<GenericItem>?,
    @Json(name = "medical_staff_specialties")
    val medicalStaffSpecialties: List<GenericItem>?

)

@JsonClass(generateAdapter = true)
data class PartnerService(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "partner_type_id")
    val partnerTypeId: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "short_name")
    val shortName: String? = null,
    @Json(name = "description")
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class HomeMenuItem(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "is_active")
    val isActive: Boolean? = null,


    var icon: Int? = null,
    var isEnabled: Boolean = true, //temporary disable
)

@JsonClass(generateAdapter = true)
open class  NotificationCategory(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    var name: String? = null,
    @Json(name = "title")
    val title: String? = null,
    @Json(name = "icon")
    val icon_url: String? = null,
    var isChecked: Boolean? = false,
)

@JsonClass(generateAdapter = true)
open class  GenericItem(
    @Json(name = "id")
    val genericItemId: Int? = null,
    @Json(name = "name")
    var genericItemName: String? = null,
    @Json(name = "label")
    val label: String? = null,
    @Json(name = "icon_url")
    var icon_url: String? = null,
    @Json(name = "description")
    val description: String? = null,
    @Json(name = "integration_id")
    val integrationId: String? = null,
    @Json(name = "offer_service")
    val offerService: Int? = null,
    @Json(name = "partner_service_id")
    val partnerServiceId: Int? = null, // v1/claim/services
    @Json(name = "amount")
    val amount: Double? = null,
    @Json(name = "tenant_id")
    val tenantId: Int? = null,
    var isChecked: Boolean? = false,
): MultipleViewItem(itemId = genericItemId.toString(), title = genericItemName, imageUrl = icon_url, desc = description)

@JsonClass(generateAdapter = true)
open class EMRVitalsMeta(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "short_name")
    val short_name: String? = null,
    @Json(name = "group")
    val group: String? = null,
    @Json(name = "unit")
    val unit: Int? = null,
): GenericItem()

@JsonClass(generateAdapter = true)
open class PaymobIntegrationId(
    @Json(name = "online_card")
    val onlineCard: Int? = null,
    @Json(name = "online_card_test")
    val onlineCardTest: Int? = null,
    @Json(name = "easypaisa")
    val easypaisa: Int? = null,
    @Json(name = "easypaisa_test")
    val easypaisaTest: Int? = null,
    @Json(name = "jazzcash")
    val jazzcash: Int? = null,
    @Json(name = "jazzcash_test")
    val jazzcashTest: Int? = null,
    @Json(name = "nift")
    val nift: Int? = null,
): GenericItem()

@JsonClass(generateAdapter = true)
open class PaymentMethod(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "tenant_id")
    val tenantId: Int? = null,
    @Json(name = "payment_method_id")
    val paymentMethodId: Int? = null,
    @Json(name = "payment_method")
    val paymentMethod: GenericItem? = null,
)

@JsonClass(generateAdapter = true)
open class NotificationType(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "is_silent")
    val isSilent: String? = null,
)

@JsonClass(generateAdapter = true)
open class RequiredDocumentType(
    @Json(name = "id")
    val id: Int? = null,
    @Json(name = "is_required")
    val required: Int? = null,
    @Json(name = "request_documents")
    val requestDocuments: GenericItem? = null,
): GenericItem(genericItemId = requestDocuments?.genericItemId , genericItemName = requestDocuments?.genericItemName)
