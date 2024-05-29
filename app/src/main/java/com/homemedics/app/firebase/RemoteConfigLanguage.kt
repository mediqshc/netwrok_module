package com.homemedics.app.firebase

import com.squareup.moshi.Json

data class RemoteConfigLanguage(
    @Json(name = "tab_string")
    val tabString: TabString? = null,

    @Json(name = "generic")
    val globalString: GenericString? = null,

    @Json(name = "error_messages")
    val errorMessages: ErrorMessages? = null,

    @Json(name = "task_screens")
    val taskScreens: TaskScreens? = null,

    @Json(name = "messages")
    val messages: Messages? = null,

    @Json(name = "notification_screens")
    val notificationScreens: NotificationScreen? = null,

    @Json(name = "user_auth_screen")
    val userAuthScreen: userAuthScreen? = null,

    @Json(name = "chat_screen")
    val chatScreen: ChatScreen? = null,

    @Json(name = "linked_account_screen")
    val linkedAccountScreen: LinkedAccountScreen? = null,

    @Json(name = "home_screen")
    val homeScreen: homeScreen? = null,

    @Json(name = "personal_profile_screen")
    val personalprofileBasicScreen: PersonalProfileBasicScreen? = null,

    @Json(name = "dialogs_strings")
    val dialogsStrings: DialogsStrings? = null,

    @Json(name = "field_validation_strings")
    val fieldValidationStrings: FieldValidationStrings? = null,

    @Json(name = "location_string")
    val locationString: LocationString? = null,

    @Json(name = "call_screen")
    val callScreen: CallScreen? = null,

    @Json(name = "planning_screen")
    val planningScreen: PlanningScreen? = null,

    @Json(name = "booking_screen")
    val bookingScreen: BookingScreen? = null,

    @Json(name = "partner_screen")
    val partnerProfileScreen: PartnerProfileScreen? = null,

    @Json(name = "lab_pharmacy_screens")
    val labPharmacyScreen: LabPharmacyScreen? = null,

    @Json(name = "checkout_screens")
    val checkoutScreen: CheckoutScreen? = null,

    @Json(name = "emr_screens")
    val emrScreens: MedicalRecordScreens? = null,

    @Json(name = "my_orders_screens")
    val myOrdersScreens: MyOrdersScreens? = null,

    @Json(name = "walk_in_screens")
    val walkInScreens: WalkInScreens? = null,

    @Json(name = "claim_screen")
    val claimScreen: ClaimScreen? = null
)

data class GenericString(

    @Json(name = "app_update_url_android")
    val appUpdateUrlAndroid: String? = null,

    @Json(name = "app_version")
    val appVersion: String? = null,

    @Json(name = "call_status")
    val callStatus: String? = null,

    @Json(name = "accept")
    val accept: String? = null,

    @Json(name = "no_age_limit")
    val noAgeLimit: String? = null,

    @Json(name = "sublimit")
    val sublimit: String? = null,

    @Json(name = "age")
    val age: String? = null,

    @Json(name = "current")
    val current: String? = null,

    @Json(name = "below")
    val below: String? = null,

    @Json(name = "above")
    val above: String? = null,

    @Json(name = "discount")
    val discount: String? = null,

    @Json(name = "discount_center")
    val discountCenter: String? = null,

    @Json(name = "discount_desc")
    val discountDesc: String? = null,

    @Json(name = "discount_details")
    val discountDetails: String? = null,

    @Json(name = "terms_condition_apply")
    val termsConditionApply: String? = null,

    @Json(name = "pharmacy_online_desc")
    val pharmacyOnlineDesc: String? = null,

    @Json(name = "pharmacy_walk_in_desc")
    val pharmacyWalkInDesc: String? = null,

    @Json(name = "lab_online_desc")
    val labOnlineDesc: String? = null,

    @Json(name = "lab_walk_in_desc")
    val labWalkInDesc: String? = null,

    @Json(name = "walk_in_cashless")
    val walkInCashless: String? = null,

    @Json(name = "walk_in_discount")
    val walkInDiscount: String? = null,

    @Json(name = "off")
    val off: String? = null,

    @Json(name = "on")
    val on: String? = null,

    @Json(name = "and")
    val and: String? = null,

    @Json(name = "decline")
    val decline: String? = null,

    @Json(name = "delete_account")
    val deleteAccount: String? = null,

    @Json(name = "delete_my_account")
    val deleteMyAccount: String? = null,

    @Json(name = "promo_code")
    val promoCode: String? = null,

    @Json(name = "cnic_number")
    val cnicNumber: String? = null,

    @Json(name = "unread")
    val unread: String? = null,

    @Json(name = "upcoming")
    val upcoming: String? = null,

    @Json(name = "fee")
    val fee: String? = null,

    @Json(name = "promo")
    val promo: String? = null,

    @Json(name = "order")
    val order: String? = null,

    @Json(name = "km")
    val kilometer: String? = null,

    @Json(name = "history")
    val history: String? = null,

    @Json(name = "select_pharmacy")
    val selectPharmacy: String? = null,

    @Json(name = "select_laboratory")
    val selectLaboratory: String? = null,

    @Json(name = "condition_apply")
    val conditionsApply: String? = null,

    @Json(name = "select_hospital")
    val selectHospital: String? = null,

    @Json(name = "walk_in_services_desc")
    val walk_in_services_desc: String? = null,

    @Json(name = "lab_walk_in_discount_desc")
    val lab_walk_in_discount_desc: String? = null,

    @Json(name = "pharma_walk_in_discount_desc")
    val pharma_walk_in_discount_desc: String? = null,

    @Json(name = "hospital_walk_in_discount_desc")
    val hospital_walk_in_discount_desc: String? = null,

    @Json(name = "walk_in_online_desc")
    val walk_in_online_desc: String? = null,

    @Json(name = "title")
    val title: String? = null,

    @Json(name = "desc")
    val description: String? = null,

    @Json(name = "no_instructions")
    val noInstructions: String? = null,

    @Json(name = "year")
    val year: String? = null,

    @Json(name = "no_attachments")
    val noAttachments: String? = null,

    @Json(name = "no_medical_records_added")
    val noMedicalRecordsAdded: String? = null,

    @Json(name = "upload_image")
    val uploadImage: String? = null,

    @Json(name = "select_status")
    val selectStatus: String? = null,

    @Json(name = "status")
    val status: String? = null,

    @Json(name = "save_record")
    val saveRecord: String? = null,

    @Json(name = "followup_date")
    val followupDate: String? = null,

    @Json(name = "months")
    val monthhs: String? = null,

    @Json(name = "search_by_name")
    val searchByName: String? = null,

    @Json(name = "year_exp")
    val yearExp: String? = null,

    @Json(name = "years_exp")
    val yearsExp: String? = null,

    @Json(name = "delete_button")
    val delete: String? = null,

    @Json(name = "less_than_one_year_exp")
    val lessThanOneYearExp: String? = null,

    @Json(name = "less_than_year")
    val lessThanYear: String? = null,

    @Json(name = "doctor_experience")
    val doctorExperience: String? = null,

    @Json(name = "doctor_experiences")
    val doctorExperiences: String? = null,

    @Json(name = "booked_for")
    val bookedFor: String? = null,

    @Json(name = "payment_mode")
    val paymentMode: String? = null,

    @Json(name = "no_walkin_pharmacy")
    val noWalkInPharmacy: String? = null,

    @Json(name = "no_walkin_lab")
    val noWalkInLab: String? = null,

    @Json(name = "no_walkin_hospital")
    val noWalkInHospital: String? = null,

    @Json(name = "none")
    val none: String? = null,

    @Json(name = "no_result_found")
    val noResultFound: String? = null,

    @Json(name = "exclusion_categories")
    val exclusionCategories: String? = null,

    @Json(name = "booked_by")
    val bookedBy: String? = null,

    @Json(name = "speciality")
    val speciality: String? = null,

    @Json(name = "specialities")
    val specialities: String? = null,

    @Json(name = "doctors")
    val doctors: String? = null,

    @Json(name = "total")
    val total: String? = null,

    @Json(name = "selF")
    val self: String? = null,

    @Json(name = "charges")
    val charges: String? = null,

    @Json(name = "set_year")
    val setYear: String? = null,

    @Json(name = "record_num")
    val recordNum: String? = null,

    @Json(name = "record")
    val record: String? = null,

    @Json(name = "stars")
    val stars: String? = null,

    @Json(name = "add_more_tests")
    val addMoreTests: String? = null,

    @Json(name = "select_patient")
    val selectPatient: String? = null,

    @Json(name = "select_medicine")
    val selectMedicine: String? = null,

    @Json(name = "about_doctor")
    val aboutDoctor: String? = null,

    @Json(name = "read_more")
    val readMore: String? = null,

    @Json(name = "other_attachments")
    val otherAttachments: String? = null,

    @Json(name = "clear_filter")
    val clearFilter: String? = null,

    @Json(name = "read_less")
    val readLess: String? = null,

    @Json(name = "no_record_found")
    val noRecordFound: String? = null,

    @Json(name = "reviews")
    val reviews: String? = null,

    @Json(name = "shared")
    val shared: String? = null,

    @Json(name = "visit_address")
    val visitAddress: String? = null,

    @Json(name = "free")
    val free: String? = null,

    @Json(name = "amount_unavailable")
    val amountUnavailable: String? = null,

    @Json(name = "ok")
    val ok: String? = null,

    @Json(name = "patient_details")
    val patientDetails: String? = null,

    @Json(name = "request_submit")
    val requestSubmit: String? = null,

    @Json(name = "request")
    val request: String? = null,

    @Json(name = "warning")
    val warning: String? = null,

    @Json(name = "patient")
    val patient: String? = null,

    @Json(name = "special_instructions")
    val specialInstructions: String? = null,

    @Json(name = "no_rating")
    val noRating: String? = null,

    @Json(name = "no_connections")
    val noConnections: String? = null,

    @Json(name = "attachments")
    val attachments: String? = null,

    @Json(name = "video_calling")
    val videoCalling: String? = null,

    @Json(name = "btn_continue")
    val btnContinue: String? = null,

    @Json(name = "home_visit")
    val homeVisit: String? = null,

    @Json(name = "go_back")
    val goBack: String? = null,

    @Json(name = "m_add_attachments")
    val mAddAttachments: String? = null,

    @Json(name = "voice_note")
    val voiceNote: String? = null,

    @Json(name = "add_image")
    val addImage: String? = null,

    @Json(name = "image_preview")
    val imagePreview: String? = null,

    @Json(name = "upload_doc")
    val uploadDoc: String? = null,

    @Json(name = "medical_records")
    val medicalRecords: String? = null,

    @Json(name = "book_now")
    val bookNow: String? = null,

    @Json(name = "online_booking")
    val onlineBooking: String? = null,

    @Json(name = "walk_in")
    val walkin: String? = null,

    @Json(name = "walk_in_hospital")
    val walkinHospital: String? = null,

    @Json(name = "home_health_care")
    val homeHealthCare: String? = null,

    @Json(name = "done")
    val done: String? = null,

    @Json(name = "cancel")
    val cancel: String? = null,

    @Json(name = "cancel_request")
    val cancelRequest: String? = null,

    @Json(name = "select")
    val select: String? = null,

    @Json(name = "remove")
    val remove: String? = null,

    @Json(name = "upload")
    val upload: String? = null,

    @Json(name = "back")
    val back: String? = null,

    @Json(name = "other")
    val other: String? = null,

    @Json(name = "country")
    val country: String? = null,

    @Json(name = "city")
    val city: String? = null,

    @Json(name = "add_new")
    val addNew: String? = null,

    @Json(name = "to")
    val to: String? = null,

    @Json(name = "save")
    val save: String? = null,

    @Json(name = "add")
    val add: String? = null,

    @Json(name = "cnic")
    val cnic: String? = null,

    @Json(name = "no")
    val no: String? = null,

    @Json(name = "sec")
    val sec: String? = null,

    @Json(name = "yes")
    val yes: String? = null,

    @Json(name = "filter")
    val filter: String? = null,

    @Json(name = "submit")
    val submit: String? = null,

    @Json(name = "messages")
    val Messages: String? = null,

    @Json(name = "verify_code")
    val verify_code: String? = null,

    @Json(name = "verify")
    val verify: String? = null,

    @Json(name = "password")
    val password: String? = null,

    @Json(name = "forgot_password")
    val forgotPassword: String? = null,

    @Json(name = "category")
    val category: String? = null,

    @Json(name = "save_n_continue")
    val saveNContinue: String? = null,

    @Json(name = "date_of_birth")
    val dateOfBirth: String? = null,

    @Json(name = "verification_code")
    val verificationCode: String? = null,

    @Json(name = "country_code")
    val countryCode: String? = null,

    @Json(name = "mobile_number")
    val mobileNumber: String? = null,

    @Json(name = "phone")
    val phone: String? = null,

    @Json(name = "gender")
    val gender: String? = null,

    @Json(name = "resend_code")
    val resendCode: String? = null,

    @Json(name = "send_code")
    val sendCode: String? = null,

    @Json(name = "name")
    val name: String? = null,

    @Json(name = "time")
    val time: String? = null,

    @Json(name = "_years")
    val _years: String? = null,

    @Json(name = "company")
    val company: String? = null,

    @Json(name = "dob")
    val dob: String? = null,

    @Json(name = "error")
    val error: String? = null,

    @Json(name = "from")
    val from: String? = null,

    @Json(name = "end_date")
    val endDate: String? = null,

    @Json(name = "start_date")
    val startDate: String? = null,

    @Json(name = "request_submitted")
    val requestSubmitted: String? = null,

    @Json(name = "information")
    val information: String? = null,

    @Json(name = "start")
    val start: String? = null,

    @Json(name = "end")
    val end: String? = null,

    @Json(name = "start_timing")
    val startTiming: String? = null,

    @Json(name = "end_timing")
    val endTiming: String? = null,

    @Json(name = "minutes")
    val minutes: String? = null,

    @Json(name = "hours")
    val hours: String? = null,

    @Json(name = "hour")
    val hour: String? = null,

    @Json(name = "select_interval")
    val selectInterval: String? = null,

    @Json(name = "seperator")
    val seperator: String? = null,

    @Json(name = "select_speciality")
    val selectSpeciality: String? = null,

    @Json(name = "years")
    val years: String? = null,

    @Json(name = "documents")
    val documents: String? = null,

    @Json(name = "search")
    val search: String? = null,

    @Json(name = "profile")
    val profile: String? = null,

    @Json(name = "services")
    val services: String? = null,

    @Json(name = "document")
    val document: String? = null,

    @Json(name = "clinic")
    val clinic: String? = null,

    @Json(name = "apply_filter")
    val applyFilter: String? = null,

    @Json(name = "select_address")
    val selectAddress: String? = null,

    @Json(name = "choose_location")
    val chooseLocation: String? = null,

    @Json(name = "close")
    val close: String? = null,

    @Json(name = "view_cart")
    val viewCart: String? = null,

    @Json(name = "alternate_available")
    val alternateAvailable: String? = null,

    @Json(name = "order_details")
    val orderDetails: String? = null,

    @Json(name = "date")
    val date: String? = null,

    @Json(name = "confirm")
    val confirm: String? = null,

    @Json(name = "payable_amount")
    val payableAmount: String? = null,

    @Json(name = "corporate_discount")
    val corporateDiscount: String? = null,

    @Json(name = "promo_discount")
    val promoDiscount: String? = null,

    @Json(name = "company_credit")
    val companyDiscount: String? = null,

    @Json(name = "payment_method")
    val paymentMethod: String? = null,

    @Json(name = "location_permissions")
    val locationPermissions: String? = null,

    @Json(name = "camera_permissions")
    val cameraPermissions: String? = null,

    @Json(name = "success")
    val success: String? = null,

    @Json(name = "quantity")
    val quantity: String? = null,

    @Json(name = "days")
    val days: String? = null,

    @Json(name = "no_doctor_found")
    val noDoctorFound: String? = null,

    @Json(name = "reschedule_appointment")
    val rescheduleAppointment: String? = null,
    @Json(name = "when_consultation")
    val whenConsultation: String? = null,
    @Json(name = "timing")
    val timing: String? = null,
    @Json(name = "no_slots_available")
    val noSlotsAvailable: String? = null,
    @Json(name = "home_collection_charges")
    val homeCollectionCharges: String? = null,
    @Json(name = "reshedule")
    val reshedule: String? = null,
    @Json(name = "prescription")
    val prescription: String? = null,
    @Json(name = "feedback_here")
    val feedbackHere: String? = null,
    @Json(name = "next_")
    val next_: String? = null,
    @Json(name = "resubmit")
    val resubmit: String? = null
)

data class TabString(

    @Json(name = "home")
    val home: String? = null,

    @Json(name = "select_language")
    val selectLanguage: String? = null,

    @Json(name = "faqs")
    val faq: String? = null,

    @Json(name = "my_profile")
    val myProfile: String? = null,

    @Json(name = "become_a_partner")
    val becomePartner: String? = null,

    @Json(name = "personal_profile")
    val personalProfile: String? = null,

    @Json(name = "my_partner_profile")
    val partnerProfile: String? = null,

    @Json(name = "my_connections")
    val myConnections: String? = null,

    @Json(name = "my_association")
    val myAssociations: String? = null,

    @Json(name = "my_medical_records")
    val myMedicalRecords: String? = null,

    @Json(name = "doctor_messages")
    val doctorMessages: String? = null,

    @Json(name = "my_orders_and_requests")
    val myOrdersRequests: String? = null,

    @Json(name = "profile")
    val profile: String? = null,

    @Json(name = "service")
    val service: String? = null,

    @Json(name = "login_to_continue")
    val loginToContinue: String? = null,

    @Json(name = "language")
    val language: String? = null,

    @Json(name = "patient_messages")
    val patientMessages: String? = null,

    @Json(name = "my_planner")
    val myPlanner: String? = null,

    @Json(name = "my_appointment")
    val myAppointment: String? = null,

    @Json(name = "partner_mode")
    val partnerMode: String? = null,


    @Json(name = "logout")
    val logout: String? = null,

    @Json(name = "contact_us")
    val contact_us: String? = null,
)

data class NotificationScreen(
    @Json(name = "notification")
    val notification: String? = null,

    @Json(name = "no_noti_available")
    val noNotiAvailable: String? = null
)

data class PlanningScreen(

    @Json(name = "empty_slot_message")
    val emptySlotMessage: String? = null,

    @Json(name = "weekly_planner")
    val weeklyPlanner: String? = null,

    @Json(name = "work_planner")
    val workPlanner: String? = null,

    @Json(name = "select_week_day")
    val selectWeekDay: String? = null,

    @Json(name = "start_time_date")
    val startTimingDate: String? = null,

    @Json(name = "end_time_date")
    val endTimingDate: String? = null,

    @Json(name = "sunday")
    val sunday: String? = null,

    @Json(name = "saturday")
    val saturday: String? = null,

    @Json(name = "friday")
    val friday: String? = null,

    @Json(name = "thursday")
    val thursday: String? = null,

    @Json(name = "wednesday")
    val wednesday: String? = null,

    @Json(name = "tuesday")
    val tuesday: String? = null,

    @Json(name = "monday")
    val monday: String? = null,

    @Json(name = "no_slots")
    val noSlots: String? = null,

    @Json(name = "end_timing")
    val endTiming: String? = null,

    @Json(name = "off_dates")
    val offDates: String? = null,

    @Json(name = "off_dates_content")
    val offDatesContent: String? = null,

    @Json(name = "no_off_dates")
    val noOffDates: String? = null,

    @Json(name = "add_off_dates")
    val addOffDates: String? = null,

    @Json(name = "start_time")
    val startTime: String? = null,

    @Json(name = "end_time")
    val endTime: String? = null,

    @Json(name = "weekly_content")
    val weeklyContent: String? = null,

    @Json(name = "time_slots")
    val timeSlots: String? = null,

    @Json(name = "consultation")
    val consultation: String? = null,

    @Json(name = "add_time_slot")
    val addTimeSlot: String? = null,

    @Json(name = "weekday")
    val weekday: String? = null,

    @Json(name = "start_timing")
    val startTiming: String? = null,

    @Json(name = "nav_title1")
    val navTitle: String? = null,

    @Json(name = "video_call")
    val videoCallQues: String? = null,

    @Json(name = "warnnig_para1")
    val warningPara: String? = null,

    @Json(name = "warnnig_para2")
    val warningPara2: String? = null,
)

data class ErrorMessages(

    @Json(name = "internet_error")
    val internetError: String? = null,

    @Json(name = "internet_error_msg")
    val internetErrorMsg: String? = null,

    @Json(name = "something_wrong")
    val somethingWrong: String? = null,

    @Json(name = "speciality_error")
    val specialityError: String? = null,

    @Json(name = "error_email_format")
    val errorEmailFormat: String? = null,

    @Json(name = "no_account")
    val noAccount: String? = null,

    @Json(name = "error_password_length")
    val errorPasswordLength: String? = null,


    )

data class Messages(

    @Json(name = "discount_not_available")
    val discount_not_available: String? = null,

    @Json(name = "not_valid_qr_code")
    val not_valid_qr_code: String? = null,

    @Json(name = "partner_not_available_now")
    val partner_not_available_now: String? = null,

    @Json(name = "invalid_tenant_id")
    val invalid_tenant_id: String? = null,

    @Json(name = "slot_already_booked")
    val slot_already_booked: String? = null,

    @Json(name = "speciality_delete_msg")
    val speciality_delete_msg: String? = null,

    @Json(name = "doc_delete_msg")
    val doc_delete_msg: String? = null,

    @Json(name = "profile_update_success")
    val profile_update_success: String? = null,

    @Json(name = "edu_delete_msg")
    val edu_delete_msg: String? = null,

    @Json(name = "work_delete_msg")
    val work_delete_msg: String? = null,

    @Json(name = "work_exp_added")
    val work_exp_added: String? = null,

    @Json(name = "no_attachment_found")
    val noAttachmentsFound: String? = null,

    @Json(name = "personalProfilePictureUploaded")
    val personalProfilePictureUploaded: String? = null,
    @Json(name = "speciality_cant_be_deleted")
    val speciality_cant_be_deleted: String? = null,
    @Json(name = "invalid_self_booking")
    val invalid_self_booking: String? = null,
    @Json(name = "speciality_updated")
    val speciality_updated: String? = null,
    @Json(name = "no_company_found_to_add")
    val no_company_found_to_add: String? = null,
    @Json(name = "invalid_emr_symptom")
    val invalid_emr_symptom: String? = null,
    @Json(name = "relation_deleted_failed")
    val relation_deleted_failed: String? = null,
    @Json(name = "completed_with_out_fees")
    val completed_with_out_fees: String? = null,
    @Json(name = "no_healthcare_linked")
    val no_healthcare_linked: String? = null,
    @Json(name = "entered_amount_exceeds_sub_limit")
    val entered_amount_exceeds_sub_limit: String? = null,
    @Json(name = "education_is_required")
    val education_is_required: String? = null,
    @Json(name = "user_already_exists")
    val user_already_exists: String? = null,
    @Json(name = "user_account_disabled")
    val user_account_disabled: String? = null,
    @Json(name = "insurance_already_added")
    val insurance_already_added: String? = null,
    @Json(name = "no_accounts_linked")
    val no_accounts_linked: String? = null,
    @Json(name = "booking_rejected")
    val booking_rejected: String? = null,
    @Json(name = "relation_deleted")
    val relation_deleted: String? = null,
    @Json(name = "already_shared")
    val already_shared: String? = null,
    @Json(name = "no_company_linked")
    val no_company_linked: String? = null,
    @Json(name = "speciality_deleted")
    val speciality_deleted: String? = null,
    @Json(name = "cnic_is_required")
    val cnic_is_required: String? = null,
    @Json(name = "booking_canceled")
    val booking_canceled: String? = null,
    @Json(name = "invalid_token")
    val invalid_token: String? = null,
    @Json(name = "insurance_link_delete_failed")
    val insurance_link_delete_failed: String? = null,
    @Json(name = "attachment_delete")
    val attachment_delete: String? = null,
    @Json(name = "journey_time")
    val journey_time: String? = null,
    @Json(name = "no_insurance_linked")
    val no_insurance_linked: String? = null,
    @Json(name = "booking_submitted")
    val booking_submitted: String? = null,
    @Json(name = "partner_account_initiated")
    val partner_account_initiated: String? = null,
    @Json(name = "off_date_deleted")
    val off_date_deleted: String? = null,
    @Json(name = "user_does_not_exists")
    val user_does_not_exists: String? = null,
    @Json(name = "booking_rescheduled")
    val booking_rescheduled: String? = null,
    @Json(name = "education_deleted")
    val education_deleted: String? = null,
    @Json(name = "slot_start_time_check")
    val slot_start_time_check: String? = null,
    @Json(name = "company_link_delete_failed")
    val company_link_delete_failed: String? = null,
    @Json(name = "invalid_emr")
    val invalid_emr: String? = null,
    @Json(name = "diastolic_bp")
    val diastolic_bp: String? = null,
    @Json(name = "healthcare_link_delete_failed")
    val healthcare_link_delete_failed: String? = null,
    @Json(name = "speciality_is_required")
    val speciality_is_required: String? = null,
    @Json(name = "update_offer_services")
    val update_offer_services: String? = null,
    @Json(name = "education_cant_be_deleted")
    val education_cant_be_deleted: String? = null,
    @Json(name = "slot_exists")
    val slot_exists: String? = null,
    @Json(name = "slots_add_msg")
    val slotsAddMsg: String? = null,
    @Json(name = "no_healthcare_available")
    val no_healthcare_available: String? = null,
    @Json(name = "lab_test_already_booked")
    val lab_test_already_booked: String? = null,
    @Json(name = "cnic_format")
    val cnic_format: String? = null,
    @Json(name = "otp_verification_success")
    val otp_verification_success: String? = null,
    @Json(name = "otp_verification_failure")
    val otp_verification_failure: String? = null,
    @Json(name = "slot_deleted")
    val slot_deleted: String? = null,
    @Json(name = "slot_exists_on_deletion")
    val slot_exists_on_deletion: String? = null,
    @Json(name = "healthcare_already_added")
    val healthcare_already_added: String? = null,
    @Json(name = "education_created")
    val education_created: String? = null,
    @Json(name = "no_insurance_to_add")
    val no_insurance_to_add: String? = null,
    @Json(name = "incorrect_password")
    val incorrect_password: String? = null,
    @Json(name = "slot_day_not_available")
    val slot_day_not_available: String? = null,
    @Json(name = "work_created")
    val work_created: String? = null,
    @Json(name = "follow_up_date")
    val follow_up_date: String? = null,
    @Json(name = "invalid_booking")
    val invalid_booking: String? = null,
    @Json(name = "completed_with_fees")
    val completed_with_fees: String? = null,
    @Json(name = "duty_id_required")
    val duty_id_required: String? = null,
    @Json(name = "member_status")
    val member_status: String? = null,
    @Json(name = "no_details_available")
    val no_details_available: String? = null,
    @Json(name = "off_date_added")
    val off_date_added: String? = null,
    @Json(name = "family_member_status")
    val family_member_status: String? = null,
    @Json(name = "systolic_bp")
    val systolic_bp: String? = null,
    @Json(name = "status_not_possible")
    val status_not_possible: String? = null,
    @Json(name = "attachment_submit")
    val attachment_submit: String? = null,
    @Json(name = "invalid_duty_start_date")
    val invalid_duty_start_date: String? = null,
    @Json(name = "cnic_updated")
    val cnic_updated: String? = null,
    @Json(name = "active_session")
    val active_session: String? = null,
    @Json(name = "company_already_added")
    val company_already_added: String? = null,
    @Json(name = "no_healthcare_to_add")
    val no_healthcare_to_add: String? = null,
    @Json(name = "relation_connected")
    val relation_connected: String? = null,
    @Json(name = "partner_is_off")
    val partner_is_off: String? = null,
    @Json(name = "work_deleted")
    val work_deleted: String? = null,
    @Json(name = "relation_already_exists")
    val relation_already_exists: String? = null,
    @Json(name = "symptom_name_description")
    val symptom_name_description: String? = null,
    @Json(name = "education_updated")
    val education_updated: String? = null,
    @Json(name = "email_not_sent")
    val email_not_sent: String? = null,
    @Json(name = "insurance_incorrect_info")
    val insurance_incorrect_info: String? = null,
    @Json(name = "relation_connection_failed")
    val relation_connection_failed: String? = null,
    @Json(name = "otp_sms_failure")
    val otp_sms_failure: String? = null,
    @Json(name = "relation_not_possible")
    val relation_not_possible: String? = null,
    @Json(name = "mandatory_attachments_required")
    val mandatoryAttachmentsRequired: String? = null,
    @Json(name = "claim_cancelled")
    val claim_cancelled: String? = null,
    @Json(name = "deleted_successfully")
    val deleted_successfully: String? = null
)

data class LinkedAccountScreen(

    @Json(name = "linked_desc")
    val linkedDesc: String? = null,

    @Json(name = "credit_funds")
    val creditFunds: String? = null,

    @Json(name = "credits_desc")
    val creditDescription: String? = null,

    @Json(name = "discounts")
    val discounts: String? = null,

    @Json(name = "terms_condition")
    val termsCondition: String? = null,

    @Json(name = "credit_desc")
    val creditDesc: String? = null,

    @Json(name = "company_validity")
    val companyValidity: String? = null,

    @Json(name = "company_locations")
    val companyLocations: String? = null,

    @Json(name = "credit_dependents")
    val creditDependents: String? = null,

    @Json(name = "discount_desc")
    val discountDesc: String? = null,

    @Json(name = "number_of_transactions")
    val numberOfTransactions: String? = null,

    @Json(name = "reset_after")
    val resetAfter: String? = null,

    @Json(name = "available_credit")
    val availableCredit: String? = null,

    @Json(name = "available_credits")
    val availableCredits: String? = null,

    @Json(name = "view_detail_terms")
    val viewDetailTerms: String? = null,

    @Json(name = "add_insurance_company")
    val addInsuranceCompany: String? = null,

    @Json(name = "no_credit")
    val noCredit: String? = null,

    @Json(name = "add_hospital_clinics")
    val addHospitalClinics: String? = null,

    @Json(name = "add_hospital_and_clinic")
    val addHospitalAndClinic: String? = null,

    @Json(name = "policy_num")
    val policyNum: String? = null,

    @Json(name = "linked_accounts")
    val linkedAccounts: String? = null,

    @Json(name = "valid_till")
    val validTill: String? = null,

    @Json(name = "linked_title")
    val linkedTitle: String? = null,

    @Json(name = "select_company")
    val selectCompany: String? = null,

    @Json(name = "my_companies")
    val myCompanies: String? = null,

    @Json(name = "my_insurances")
    val myInsurances: String? = null,

    @Json(name = "my_hospital_clinics")
    val myHospitalClinics: String? = null,

    @Json(name = "button_Link")
    val buttonLink: String? = null,

    @Json(name = "button_next")
    val buttonNext: String? = null,

    @Json(name = "link_new")
    val linkNew: String? = null,

    @Json(name = "policy_number")
    val policyNumber: String? = null,

    @Json(name = "certificate_id")
    val certificateId: String? = null,

    @Json(name = "link_your_company")
    val linkYourCompany: String? = null,

    @Json(name = "company_description")
    val companyDescription: String? = null,

    @Json(name = "email_description")
    val emailDescription: String? = null,

    @Json(name = "enter_company_email")
    val enterCompanyEmail: String? = null,

    @Json(name = "verify_email")
    val verifyYourEmail: String? = null,

    @Json(name = "verify_your_action")
    val verifyYourAction: String? = null,

    @Json(name = "code_sent")
    val codeSent: String? = null,

    @Json(name = "enter_verification_code")
    val enterVerificationCode: String? = null,

    @Json(name = "online_pharmacy_discount")
    val onlineDiscount: String? = null,
    @Json(name = "categories")
    val categories: String? = null,
    @Json(name = "excluded_products")
    val excludedProducts: String? = null,
    @Json(name = "discount_description")
    val discountDescription: String? = null,
    @Json(name = "promo_code_discount")
    val promoDiscount: String? = null,
    @Json(name = "max_cap")
    val maxCap: String? = null,
    @Json(name = "min_invoice")
    val minInvoice: String? = null,
)

data class ChatScreen(
    @Json(name = "chat")
    val chat: String? = null,

    @Json(name = "kb")
    val kb: String? = null,

    @Json(name = "min")
    val min: String? = null,

    @Json(name = "sec")
    val sec: String? = null,

    @Json(name = "today")
    val today: String? = null,

    @Json(name = "chooser_title")
    val chooserTitle: String? = null,

    @Json(name = "session_expires")
    val sessionExpires: String? = null,

    @Json(name = "renew_session")
    val renewSession: String? = null,

    @Json(name = "hours_left")
    val hoursLeft: String? = null,

    @Json(name = "hour_left")
    val hourLeft: String? = null,

    @Json(name = "min_left")
    val minLeft: String? = null,

    @Json(name = "session_complete")
    val sessionComplete: String? = null,

    @Json(name = "cancelled")
    val cancelled: String? = null,

    @Json(name = "message_consultation")
    val messageConsultation: String? = null,

    @Json(name = "send_msg")
    val sendMsg: String? = null,

    @Json(name = "no_chat_found")
    val noChatFound: String? = null,

    @Json(name = "search_here")
    val searchHere: String? = null,
)


data class userAuthScreen(
    @Json(name = "login")
    val login: String? = null,

    @Json(name = "verification_code_sent")
    val verificationCodeSent: String? = null,

    @Json(name = "login_features")
    val loginFeatures: String? = null,

    @Json(name = "start_journey")
    val startJourney: String? = null,

    @Json(name = "welcome")
    val welcome: String? = null,

    @Json(name = "new_app_message")
    val newAppMessage: String? = null,

    @Json(name = "description")
    val description: String? = null,

    @Json(name = "signup")
    val signup: String? = null,

    @Json(name = "health_check")
    val healthCheck: String? = null,

    @Json(name = "term_and_condition_check")
    val termAndConditionCheck: String? = null,

    @Json(name = "forgot_password")
    val forgotPassword: String? = null,

    @Json(name = "forgot_password_description")
    val forgotPasswordDescription: String? = null,

    @Json(name = "new_password")
    val newPassword: String? = null,

    @Json(name = "change_password")
    val changePassword: String? = null,

    @Json(name = "confirm_password")
    val confirmPassword: String? = null

)

data class LocationString(
    @Json(name = "street")
    val street: String? = null,

    @Json(name = "location")
    val location: String? = null,

    @Json(name = "floor_unit")
    val floorUnit: String? = null,

    @Json(name = "confirm_location")
    val confirmLocation: String? = null,

    @Json(name = "address_location")
    val addressLocation: String? = null,

    @Json(name = "location_permissions")
    val locationPermission: String? = null,

    @Json(name = "floor")
    val floor: String? = null
)


data class homeScreen(
    @Json(name = "emr")
    val emr: String? = null,

    @Json(name = "doctor")
    val doctor: String? = null,

    @Json(name = "hello_user")
    val helloUser: String? = null,

    @Json(name = "hello_user1")
    val helloUser1: String? = null,

    @Json(name = "hello_greet")
    val helloGreet: String? = null,

    @Json(name = "pharmacy")
    val pharmacy: String? = null,

    @Json(name = "lab_diagnostic")
    val labDiagnostic: String? = null,

    @Json(name = "home_healthcare")
    val homeHealthcare: String? = null,

    @Json(name = "m_opt_home_healthcare")
    val mOptHomeHealthcare: String? = null,

    @Json(name = "m_opt_lab_diagnostic")
    val mOptLabDiagnostic: String? = null,

    @Json(name = "m_opt_pharmacy")
    val mOptPharmacy: String? = null,

    @Json(name = "m_opt_doctor")
    val mOptDoctor: String? = null
)

data class FieldValidationStrings(
    @Json(name = "country_code_validation")
    val countryCodeValidation: String? = null,

    @Json(name = "password_validation")
    val passwordValidation: String? = null,

    @Json(name = "rating_validation")
    val ratingValidation: String? = null,

    @Json(name = "mobile_number_validation")
    val mobileNumberValidation: String? = null,

    @Json(name = "error_profession")
    val errorProfession: String? = null,

    @Json(name = "agree_terms")
    val agreeTerms: String? = null,

    @Json(name = "cnic_number")
    val cnicNumber: String? = null,

    @Json(name = "error_policy_number")
    val errorPolicyNumber: String? = null,

    @Json(name = "error_certificate_id")
    val errorCertificateId: String? = null,

    @Json(name = "error_cnic")
    val errorCNIC: String? = null,

    @Json(name = "email_validation")
    val emailValidation: String? = null,

    @Json(name = "name_validation")
    val nameValidation: String? = null,

    @Json(name = "cnic_short_validation")
    val cnicShortValidation: String? = null,

    @Json(name = "name_correct_validation")
    val nameCorrectValidation: String? = null,

    @Json(name = "password_correct_validation")
    val passwordCorrectValidation: String? = null,

    @Json(name = "confirm_password_correct_validation")
    val confirmPasswordCorrectValidation: String? = null,

    @Json(name = "mobile_num_empty")
    val mobileNumEmpty: String? = null,

    @Json(name = "gender_validation")
    val genderValidation: String? = null,

    @Json(name = "date_of_birth_validation")
    val dateOfBirthValidation: String? = null,

    @Json(name = "select_country_validation")
    val selectCountryValidation: String? = null,

    @Json(name = "select_city_validation")
    val selectCityValidation: String? = null,

    @Json(name = "verification_code_validation")
    val verificationCodeValidation: String? = null,

    @Json(name = "password_confirm_validation")
    val passwordConfirmValidation: String? = null,

    @Json(name = "verification_code_corrent_validation")
    val verificationCodeCorrentValidation: String? = null,

    @Json(name = "speciality_error")
    val specialityError: String? = null,

    @Json(name = "degree_empty")
    val degreeEmpty: String? = null,

    @Json(name = "institute_empty")
    val instituteEmpty: String? = null,

    @Json(name = "year_empty")
    val yearEmpty: String? = null,

    @Json(name = "upload_document")
    val uploadDocument: String? = null,

    @Json(name = "street_empty")
    val streetEmpty: String? = null,

    @Json(name = "category_empty")
    val categoryEmpty: String? = null,

    @Json(name = "other_empty")
    val otherEmpty: String? = null,

    @Json(name = "name_empty")
    val nameEmpty: String? = null,

    @Json(name = "number_empty")
    val numberEmpty: String? = null,

    @Json(name = "hourly_empty")
    val hourlyEmpty: String? = null,

    @Json(name = "dosage_quantity_empty")
    val dosageQuantityEmpty: String? = null,

    @Json(name = "dosage_quantity_zero")
    val dosageQuantityZero: String? = null,

    @Json(name = "record_date_empty")
    val recordDateEmpty: String? = null,

    @Json(name = "medications_empty")
    val medicationsEmpty: String? = null,

    @Json(name = "date_smaller")
    val dateSmaller: String? = null,

    @Json(name = "design_empty")
    val designEmpty: String? = null,

    @Json(name = "company_empty")
    val companyEmpty: String? = null,

    @Json(name = "date_filled_error")
    val dateFilledError: String? = null,

    @Json(name = "invoice_amount_empty")
    val invoiceAmountEmpty: String? = null,

    @Json(name = "invoice_amount_greater")
    val invoiceAmountGreater: String? = null,

    @Json(name = "lab_pharma_hospital_empty")
    val labPharmaHospitalEmpty: String? = null,

    @Json(name = "invalid_invoice_amount")
    val invalidInvoiceAmount: String? = null
)


data class TaskScreens(
    @Json(name = "appointments")
    val appointments: String? = null,

    @Json(name = "less_than_one_year")
    val lessThanOneYear: String? = null,

    @Json(name = "accept_home_visit")
    val acceptHomeVisit: String? = null,

    @Json(name = "cancel_msg")
    val cancelMsg: String? = null,

    @Json(name = "reschedule_appointment_desc")
    val rescheduleAppointmentDesc: String? = null,

    @Json(name = "complete_msg")
    val completeMsg: String? = null,

    @Json(name = "select_reason")
    val selectReason: String? = null,

    @Json(name = "home_services")
    val homeServices: String? = null,

    @Json(name = "accept_appointment")
    val acceptAppointment: String? = null,

    @Json(name = "accept_desc")
    val acceptDesc: String? = null,

    @Json(name = "reschedule_appointment")
    val rescheduleAppointment: String? = null,

    @Json(name = "start_journey")
    val startJourney: String? = null,

    @Json(name = "journey_des")
    val journeyDES: String? = null,

    @Json(name = "consultation")
    val consultation: String? = null,

    @Json(name = "consultation_desc")
    val consultationDesc: String? = null,

    @Json(name = "following_date")
    val followingDate: String? = null,

    @Json(name = "amount")
    val amount: String? = null,

    @Json(name = "reject_appointment")
    val rejectAppointment: String? = null,

    @Json(name = "reject_desc")
    val rejectDesc: String? = null,

    @Json(name = "reason")
    val reason: String? = null,

    @Json(name = "completed_end")
    val completedEnd: String? = null,

    @Json(name = "medical_staff_complete_msg")
    val medicalStaffCompleteMsg: String? = null,

    @Json(name = "completed_end_medical_staff")
    val completedEndMedicalStaff: String? = null,

    @Json(name = "rejected_end")
    val rejectedEnd: String? = null,

    @Json(name = "reschedule_end")
    val rescheduleEnd: String? = null,

    @Json(name = "reshedule")
    val reshedule: String? = null,

    @Json(name = "reject")
    val reject: String? = null,

    @Json(name = "accept")
    val accept: String? = null,

    @Json(name = "mark_complete")
    val markComplete: String? = null,

    @Json(name = "reschedule_request")
    val rescheduleRequest: String? = null,

    @Json(name = "time_interval_desc")
    val timeIntervalDesc: String? = null,

    @Json(name = "order_home_details")
    val orderHomeDetails: String? = null,

    @Json(name = "order_video_details")
    val orderVideoDetails: String? = null,

    @Json(name = "canceled_end")
    val canceledEnd: String? = null,

    @Json(name = "rejected_reason")
    val rejectedReason: String? = null,

    @Json(name = "no_appointments_msg")
    val noAppointmentsMsg: String? = null,

    @Json(name = "fee_collected")
    val feeCollected: String? = null,

    @Json(name = "send_message")
    val sendMessage: String? = null,

    @Json(name = "paid_in_advance")
    val paidInAdvance: String? = null,

    @Json(name = "total_amount")
    val totalAmount: String? = null,

    @Json(name = "select_available_now_title")
    val selectAvailableNowTitle: String? = null,

    @Json(name = "available_now_enabled_time")
    val availableNowEnabledTime: String? = null,

    @Json(name = "whole_day")
    val wholeDay: String? = null,

    @Json(name = "available")
    val available: String? = null
)

data class DialogsStrings(

    @Json(name = "app_update_title")
    val appUpdateTitle: String? = null,

    @Json(name = "app_update_desc")
    val appUpdateDesc: String? = null,

    @Json(name = "update")
    val update: String? = null,

    @Json(name = "later")
    val later: String? = null,

    @Json(name = "tenant_disable")
    val tenantDisable: String? = null,

    @Json(name = "walk_review")
    val walkInReview: String? = null,

    @Json(name = "promo_added")
    val promoAdded: String? = null,

    @Json(name = "reschedule_request")
    val rescheduleRequest: String? = null,

    @Json(name = "delete_education_desc")
    val deleteEducationDesc: String? = null,

    @Json(name = "accept_home_request")
    val acceptHomeRequest: String? = null,

    @Json(name = "data_lose_msg")
    val dataLoseMsg: String? = null,

    @Json(name = "cancel_changes")
    val cancelChanges: String? = null,

    @Json(name = "delete_account_request")
    val deleteAccountRequest: String? = null,

    @Json(name = "confirmation_send")
    val confirmationSend: String? = null,

    @Json(name = "unlink_company_desc")
    val unlinkCompanyDesc: String? = null,

    @Json(name = "unlink_company_title")
    val unlinkCompanyTitle: String? = null,

    @Json(name = "unlink_insurance_title")
    val unlinkInsuranceTitle: String? = null,

    @Json(name = "delete_desc")
    val deleteDesc: String? = null,

    @Json(name = "payment_declined_desc")
    val paymentDeclinedDesc: String? = null,

    @Json(name = "order_delete_msg")
    val orderDeleteMsg: String? = null,

    @Json(name = "select_hourly")
    val selectHourly: String? = null,

    @Json(name = "invalid_promo")
    val invalidPromoCode: String? = null,

    @Json(name = "distance_calculation")
    val distanceCalculation: String? = null,

    @Json(name = "please_add_dosage")
    val pleaseAddDosage: String? = null,

    @Json(name = "select_service")
    val selectService: String? = null,

    @Json(name = "delete_msg")
    val deleteMessage: String? = null,

    @Json(name = "voice_note_description")
    val voiceNoteDescription: String? = null,

    @Json(name = "voice_note_description_patient")
    val voiceNoteDescPatient: String? = null,

    @Json(name = "voice_note_description_medicalstaff")
    val voiceNoteDescMedicalstaff: String? = null,

    @Json(name = "reset_password_success")
    val resetPasswordSuccess: String? = null,

    @Json(name = "select_one_address")
    val selectOneAddress: String? = null,

    @Json(name = "select_booking_date")
    val selectBookingDate: String? = null,

    @Json(name = "select_slot")
    val selectSlot: String? = null,

    @Json(name = "select_patient")
    val selectPatient: String? = null,

    @Json(name = "write_review")
    val writeReview: String? = null,

    @Json(name = "call_dialog_review_desc")
    val callDialogReviewDesc: String? = null,

    @Json(name = "review_length")
    val reviewLength: String? = null,

    @Json(name = "verify_email_desc")
    val verifyEmailDesc: String? = null,

    @Json(name = "partner_detail_added")
    val partnerDetailAdded: String? = null,

    @Json(name = "partner_reject")
    val partnerRejected: String? = null,

    @Json(name = "confirm_delete")
    val confirmDelete: String? = null,

    @Json(name = "confirm_cancel")
    val confirmCancel: String? = null,

    @Json(name = "cancel_desc")
    val cancelDesc: String? = null,

    @Json(name = "thank_submitting")
    val thankSubmitting: String? = null,

    @Json(name = "file_size")
    val fileSize: String? = null,

    @Json(name = "mic_disabled_title")
    val micDisabledTitle: String? = null,

    @Json(name = "mic_disabled_desc")
    val micDisabledDesc: String? = null,

    @Json(name = "attachment_size")
    val attachmentSize: String? = null,

    @Json(name = "clear_cart_msg")
    val clearCartMsg: String? = null,

    @Json(name = "prescription_warning")
    val prescriptionWarning: String? = null,

    @Json(name = "prescription_desc")
    val prescriptionDesc: String? = null,

    @Json(name = "request_info")
    val requestInfo: String? = null,

    @Json(name = "location_disabled_title")
    val locationDisabledTitle: String? = null,

    @Json(name = "location_disabled_desc")
    val locationDisabledDesc: String? = null,

    @Json(name = "logout_msg")
    val logoutMsg: String? = null,

    @Json(name = "select_city")
    val selectCity: String? = null,

    @Json(name = "recording_already_added")
    val recordingAlreadyAdded: String? = null,

    @Json(name = "record_permissions")
    val recordPermissions: String? = null,

    @Json(name = "storage_permissions")
    val storagePermissions: String? = null,

    @Json(name = "permit_manual")
    val permitManual: String? = null,

    @Json(name = "add_voice_note")
    val addVoiceNote: String? = null,

    @Json(name = "patient_voice_note")
    val patientVoiceNote: String? = null,

    @Json(name = "are_you_sure")
    val areYouSure: String? = null,

    @Json(name = "medical_record_delete")
    val medicalRecordDelete: String? = null,

    @Json(name = "test_added_successfully")
    val testAddedSuccessfully: String? = null,

    @Json(name = "payment_declined")
    val paymentDeclined: String? = null,

    @Json(name = "credit_description")
    val creditDescription: String? = null,

    @Json(name = "add_promotion")
    val addPromotion: String? = null,

    @Json(name = "partner_available_slots")
    val partnerAvailableSlots: String? = null,

    @Json(name = "partner_no_available_slots")
    val partnerNoAvailableSlots: String? = null,

    @Json(name = "mark_unavailable_warning")
    val markUnavailableWarning: String? = null,

    @Json(name = "cancel_claim_msg")
    val cancelClaimMsg: String? = null,

    @Json(name = "msg_record_audio")
    val msgRecordAudio: String? = null,

    @Json(name = "package_validation")
    val packageValidation: String? = null,

    @Json(name = "walk_in_service_request")
    val walkInServiceRequest: String? = null,

    @Json(name = "walk_in_servcie_request_received")
    val walkInServiceRequestReceived: String? = null,

    @Json(name = "transaction_success_msg")
    val transactionSuccessMsg: String? = null,

    @Json(name = "transaction_success_msg_lab")
    val transactionSuccessMsgLab: String? = null,

    @Json(name = "msg_record_audio_walkin")
    val msgRecordAudioWalkin: String? = null,

    @Json(name = "not_linked_to_corporate")
    val notLinkedToCorporate: String? = null
)

data class PersonalProfileBasicScreen(
    @Json(name = "email")
    val email: String? = null,

    @Json(name = "additional_contact")
    val additionalContact: String? = null,

    @Json(name = "personal_details")
    val personalDetails: String? = null,

    @Json(name = "basic")
    val basic: String? = null,

    @Json(name = "family")
    val family: String? = null,

    @Json(name = "family_connection")
    val familyConnection: String? = null,

    @Json(name = "connected")
    val connected: String? = null,

    @Json(name = "less_year")
    val lessYear: String? = null,

    @Json(name = "sent")
    val sent: String? = null,

    @Json(name = "received")
    val received: String? = null,

    @Json(name = "verify_email")
    val verifyEmail: String? = null,

    @Json(name = "verified")
    val verified: String? = null,

    @Json(name = "add_connection")
    val addConnection: String? = null,

    @Json(name = "confirm_location")
    val confirmLocation: String? = null,

    @Json(name = "relation")
    val relation: String? = null,

    @Json(name = "personal_profile")
    val personalProfile: String? = null,

    @Json(name = "location_address")
    val locationAddress: String? = null


)

data class CallScreen(
    @Json(name = "call_again")
    val callAgain: String? = null,

    @Json(name = "ringing")
    val ringing: String? = null,

    @Json(name = "no_medical_records_found")
    val noMedicalRecordsFound: String? = null,

    @Json(name = "payment_mode")
    val paymentMode: String? = null,

    @Json(name = "video_consultation_fee")
    val videoConsultationFee: String? = null,

    @Json(name = "call_rejected_single")
    val callRejectedSingle: String? = null,

    @Json(name = "call_status_after_part_left")
    val callStatusAfterPartLeft: String? = null,

    @Json(name = "view_attachments")
    val viewAttachments: String? = null,

    @Json(name = "view_appointment_details")
    val viewAppointmentDetails: String? = null,

    @Json(name = "order")
    val order: String? = null,

    @Json(name = "view_records")
    val viewRecords: String? = null,

    @Json(name = "call_status_after_part_reject")
    val callStatusAfterPartReject: String? = null,

    @Json(name = "reconnecting")
    val reconnecting: String? = null,

    @Json(name = "slow_internet_connection")
    val slowInternetConnection: String? = null,

    @Json(name = "call_status")
    val callStatus: String? = null,
)

data class BookingScreen(
    @Json(name = "visit_type")
    val visitType: String? = null,

    @Json(name = "select_visit_type")
    val selectVisitType: String? = null,

    @Json(name = "clinic_hospital")
    val clinicHospital: String? = null,

    @Json(name = "no_slots_available")
    val noSlotsAvailable: String? = null,

    @Json(name = "request_appointment")
    val requestAppointment: String? = null,

    @Json(name = "no_speciality_found")
    val noSpecialityFound: String? = null,

    @Json(name = "no_doctor_found")
    val noDoctorFound: String? = null,

    @Json(name = "no_reviews")
    val noReviews: String? = null,

    @Json(name = "no_education_found")
    val noEducationFound: String? = null,

    @Json(name = "select_address")
    val selectAddress: String? = null,

    @Json(name = "new_request")
    val newRequest: String? = null,

    @Json(name = "view_details")
    val viewDetails: String? = null,

    @Json(name = "choose_address")
    val chooseAddress: String? = null,

    @Json(name = "req_submit_desc")
    val reqSubmitDesc: String? = null,

    @Json(name = "book_consult")
    val bookConsult: String? = null,

    @Json(name = "search_doctor")
    val searchDoctor: String? = null,

    @Json(name = "search_home_service")
    val searchHomeService: String? = null,

    @Json(name = "available_for_video_call")
    val availableForVideoCall: String? = null,

    @Json(name = "how_to_get_consult")
    val howToGetConsult: String? = null,

    @Json(name = "home_service")
    val homeService: String? = null,

    @Json(name = "no_home_service_found")
    val noHomeServiceFound: String? = null,

    @Json(name = "choose_medical_records")
    val chooseMedicalRecords: String? = null,

    @Json(name = "doc_consultation_video_desc")
    val docConsultationVideoDesc: String? = null,

    @Json(name = "doc_consultation_home_desc")
    val docConsultationHomeDesc: String? = null,

    @Json(name = "timings")
    val timings: String? = null,

    @Json(name = "when_consultation")
    val whenConsultation: String? = null,

    @Json(name = "available_now")
    val availableNow: String? = null

)

data class PartnerProfileScreen(
    @Json(name = "license_code")
    val licenseCode: String? = null,
    @Json(name = "partner_profile_info")
    val partnerProfileInfo: String? = null,
    @Json(name = "professional_detail_place_holder")
    val professionalDetailPlaceHolder: String? = null,
    @Json(name = "professional_details")
    val professionalDetails: String? = null,
    @Json(name = "professional_overview")
    val professionalOverview: String? = null,
    @Json(name = "service_info")
    val serviceInfo: String? = null,
    @Json(name = "specialties")
    val specialties: String? = null,
    @Json(name = "work_experiences")
    val workExperiences: String? = null,
    @Json(name = "years_of_experience")
    val yearsOfExperience: String? = null,
    @Json(name = "education")
    val education: String? = null,
    @Json(name = "cnic_back")
    val cnicBack: String? = null,
    @Json(name = "cnic_front")
    val cnicFront: String? = null,
    @Json(name = "upload_pic_cnic")
    val uploadPicCnic: String? = null,
    @Json(name = "view_pic_cnic")
    val viewPicCnic: String? = null,
    @Json(name = "designation")
    val designation: String? = null,
    @Json(name = "partner_profile")
    val partnerProfile: String? = null,
    @Json(name = "work_title")
    val workTitle: String? = null,
    @Json(name = "partner_details_placeholder")
    val partnerDetailOverview: String? = null,
    @Json(name = "profession_title")
    val professionTitle: String? = null,
    @Json(name = "cnic_upload")
    val uploadCNIC: String? = null,
    @Json(name = "cnic_uploaded")
    val cnicUloaded: String? = null,
    @Json(name = "add_education")
    val addEducation: String? = null,
    @Json(name = "degree")
    val degree: String? = null,
    @Json(name = "institute")
    val institute: String? = null,
    @Json(name = "cnic_success")
    val cnicSucces: String? = null,
    @Json(name = "cnic_front_back_required")
    val cnicFrontBackRequired: String? = null,
    @Json(name = "service_updated")
    val serviceUpdate: String? = null,
    @Json(name = "partner_detail_update")
    val partnerDetailUpdate: String? = null,
    @Json(name = "partner_detail_added")
    val partnerDetailAdded: String? = null,
)

data class LabPharmacyScreen(
    @Json(name = "pharmacy_title")
    val pharmacyTitle: String? = null,
    @Json(name = "do_you_have_a_prescription")
    val doYouHaveAPrescription: String? = null,
    @Json(name = "upload_prescription_desc")
    val uploadPrescriptionDesc: String? = null,
    @Json(name = "upload_prescription_lab_desc")
    val uploadPrescriptionLabDesc: String? = null,
    @Json(name = "search_by_name")
    val searchByName: String? = null,
    @Json(name = "e_g_panadol_nebulizer_ensure")
    val eGPanadolNebulizerEnsure: String? = null,
    @Json(name = "search_by_category")
    val searchByCategory: String? = null,
    @Json(name = "no_category_found")
    val noCategoryFound: String? = null,
    @Json(name = "empty_cart")
    val emptyCart: String? = null,
    @Json(name = "over_the_counter_products")
    val overTheCounterProduct: String? = null,
    @Json(name = "product_details")
    val productDetails: String? = null,
    @Json(name = "add_to_cart")
    val addToCart: String? = null,
    @Json(name = "pharmacy_service")
    val pharmacyServices: String? = null,
    @Json(name = "walk_in_pharmacy")
    val walkinPharmacy: String? = null,
    @Json(name = "enter_invoice")
    val enterInvoiceAmount: String? = null,
    @Json(name = "invoice_desc")
    val invoiceDesc: String? = null,
    @Json(name = "order_now")
    val orderNow: String? = null,
    @Json(name = "prescription")
    val prescription: String? = null,
    @Json(name = "prescription_details")
    val prescriptionDetails: String? = null,
    @Json(name = "delivery_address")
    val deliveryAddress: String? = null,
    @Json(name = "delivery_desc")
    val deliveryDesc: String? = null,
    @Json(name = "patient_details")
    val patientDetails: String? = null,
    @Json(name = "tablet_detail")
    val tabletDetail: String? = null,
    @Json(name = "indications")
    val indications: String? = null,
    @Json(name = "contraindications")
    val contraindications: String? = null,
    @Json(name = "related_items")
    val relatedItems: String? = null,
    @Json(name = "check_desc")
    val checkDesc: String? = null,
    @Json(name = "desc")
    val desc: String? = null,
    @Json(name = "send_request")
    val sendRequest: String? = null,
    @Json(name = "request_submit")
    val requestSubmit: String? = null,
    @Json(name = "submitted_msg")
    val submittedMsg: String? = null,
    @Json(name = "alert")
    val alert: String? = null,
    @Json(name = "diastolic")
    val diastolic: String? = null,
    @Json(name = "systolic")
    val systolic: String? = null,
    @Json(name = "request_submitted")
    val requestSubmitted: String? = null,
    @Json(name = "request_info")
    val requestInfo: String? = null,
    @Json(name = "order_prescription")
    val orderPrescription: String? = null,
    @Json(name = "na")
    val na: String? = null,
    @Json(name = "lab_Discount")
    val labDiscount: String? = null,
    @Json(name = "sample_collection_charges")
    val sampleCollectionCharges: String? = null,
    @Json(name = "payable_amount")
    val payableAmount: String? = null,
    @Json(name = "test_from_other_lab")
    val testFromOtherLab: String? = null,
    @Json(name = "prescription_not_required")
    val prescriptionNotRequired: String? = null,
    @Json(name = "prescription_required")
    val prescriptionRequired: String? = null,
    @Json(name = "clear_cart_msg")
    val clearCartMsg: String? = null,
    @Json(name = "is_sample_collection_required")
    val isSampleCollectionRequired: String? = null,
    @Json(name = "choose_sample_address")
    val chooseSampleAddress: String? = null,
    @Json(name = "prescription_desc_lab")
    val prescriptionDescLab: String? = null,
    @Json(name = "prescription_desc_lab_cart")
    val prescriptionDescLabCart: String? = null,
    @Json(name = "submit_request_lab")
    val submitRequestLab: String? = null,
    @Json(name = "location_add")
    val locationAdd: String? = null,
    @Json(name = "choose_address_for_pharmacy")
    val chooseAddressForPharmacy: String? = null,
    @Json(name = "choose_address_for_lab")
    val chooseAddressForLab: String? = null,
    @Json(name = "prefer_lab")
    val preferLab: String? = null,
    @Json(name = "prefer_lab_desc")
    val preferLabDesc: String? = null,
    @Json(name = "select_preferred_Lab")
    val selectPreferredLab: String? = null,
    @Json(name = "lab_test")
    val labTest: String? = null,
    @Json(name = "search_lab_test")
    val searchLabTest: String? = null,
    @Json(name = "select_lab_test")
    val selectLabTest: String? = null,
    @Json(name = "select_lab_branch")
    val selectLabBranch: String? = null,
    @Json(name = "search_by_medicine_name")
    val searchMedicineByName: String? = null,
    @Json(name = "search_lab_or_branch_name")
    val searchLabByName: String? = null,
    @Json(name = "no_labs_found")
    val noLabsFound: String? = null
)

data class CheckoutScreen(
    @Json(name = "add_promo_code")
    val addPromoCode: String? = null,
    @Json(name = "credit_promotions")
    val creditPromotions: String? = null,
    @Json(name = "screen_shot")
    val screenShot: String? = null,
    @Json(name = "payment_options")
    val paymentOptions: String? = null,
    @Json(name = "checkout")
    val checkout: String? = null,
    @Json(name = "confirm_and_pay")
    val confirmAndPay: String? = null,
    @Json(name = "amount")
    val amount: String? = null,
    @Json(name = "promo_code")
    val promoCode: String? = null,
    @Json(name = "for_string")
    val forString: String? = null,
    @Json(name = "thank_you")
    val thankYou: String? = null,
    @Json(name = "order_confirm_text")
    val orderConfirmText: String? = null,
    @Json(name = "order_no")
    val orderNo: String? = null,
    @Json(name = "view_details")
    val ViewDetails: String? = null,
    @Json(name = "empty_credits")
    val emptyCredits: String? = null,
    @Json(name = "home_collection_charges")
    val homeCollectionCharges: String? = null,
    @Json(name = "order_confirmation")
    val orderConfirmation: String? = null,
    @Json(name = "credit")
    val credit: String? = null,
)

data class MedicalRecordScreens(
    @Json(name = "observation")
    val observation: String? = null,
    @Json(name = "diagnosis")
    val diagnosis: String? = null,
    @Json(name = "prescription")
    val prescription: String? = null,
    @Json(name = "emr_nav")
    val emrNav: String? = null,
    @Json(name = "vitals")
    val vitals: String? = null,
    @Json(name = "symptoms")
    val symptoms: String? = null,
    @Json(name = "symptoms_description")
    val symptomsDescription: String? = null,
    @Json(name = "save_and_send")
    val saveAndSend: String? = null,
    @Json(name = "heart_rate")
    val heartRate: String? = null,
    @Json(name = "temperature")
    val temperature: String? = null,
    @Json(name = "blood_pressure")
    val bloodPressure: String? = null,
    @Json(name = "systolic_bp")
    val systolicBp: String? = null,
    @Json(name = "diastolic_bp")
    val diastolicBp: String? = null,
    @Json(name = "oxygen_level")
    val oxygenLevel: String? = null,
    @Json(name = "blood_sugar_level")
    val bloodSugarLevel: String? = null,
    @Json(name = "add_symptom")
    val addSymptom: String? = null,
    @Json(name = "create")
    val create: String? = null,
    @Json(name = "search_by_name")
    val searchByName: String? = null,
    @Json(name = "symptom_title")
    val symptomTitle: String? = null,
    @Json(name = "save_draft")
    val saveDraft: String? = null,
    @Json(name = "save_to_patient")
    val saveToPatient: String? = null,
    @Json(name = "diagnosis_list")
    val diagnosisList: String? = null,
    @Json(name = "nature_of_illness")
    val natureOfIllness: String? = null,
    @Json(name = "attachments")
    val attachments: String? = null,
    @Json(name = "attachments_desc")
    val attachmentsDesc: String? = null,
    @Json(name = "prescriptions")
    val prescriptions: String? = null,
    @Json(name = "medicines")
    val medicines: String? = null,
    @Json(name = "medicines_desc")
    val medicinesDesc: String? = null,
    @Json(name = "lab_diagnostic_tests")
    val labDiagnosticTests: String? = null,
    @Json(name = "lab_diagnostic_tests_desc")
    val labDiagnosticTestsDesc: String? = null,
    @Json(name = "medical_health")
    val medicalHealth: String? = null,
    @Json(name = "medical_health_desc")
    val medicalHealthDesc: String? = null,
    @Json(name = "add_medicine")
    val addMedicine: String? = null,
    @Json(name = "enter_details")
    val enterDetails: String? = null,
    @Json(name = "add_test")
    val addTest: String? = null,
    @Json(name = "add_recommendation")
    val addRecommendation: String? = null,
    @Json(name = "add_diagnosis")
    val addDiagnosis: String? = null,
    @Json(name = "descriptions")
    val descriptions: String? = null,
    @Json(name = "no_of_days")
    val noOfDays: String? = null,
    @Json(name = "dosage")
    val dosage: String? = null,
    @Json(name = "morning")
    val morning: String? = null,
    @Json(name = "afternoon")
    val afternoon: String? = null,
    @Json(name = "evening")
    val evening: String? = null,
    @Json(name = "glass")
    val glass: String? = null,
    @Json(name = "special_instructions")
    val specialInstructions: String? = null,
    @Json(name = "feedback_here")
    val feedbackHere: String? = null,
    @Json(name = "bpm")
    val bpm: String? = null,
    @Json(name = "f")
    val f: String? = null,
    @Json(name = "mmHg")
    val mmHg: String? = null,
    @Json(name = "oximeter")
    val oximeter: String? = null,
    @Json(name = "mg_dl")
    val mgDl: String? = null,
    @Json(name = "share_with_patient")
    val shareWithPatient: String? = null,
    @Json(name = "hourly_quantity")
    val hourlyQuantity: String? = null,
    @Json(name = "dosage_quantity")
    val dosageQuantity: String? = null,
    @Json(name = "medicines_instructions")
    val medicinesInstructions: String? = null,
    @Json(name = "emr_attachments")
    val emrAttachments: String? = null,
    @Json(name = "emr_documents")
    val emrDocuments: String? = null,
    @Json(name = "modify_text")
    val modifyText: String? = null,
    @Json(name = "date_and_time")
    val dateAndTime: String? = null,
    @Json(name = "cancel_changes")
    val cancelChanges: String? = null,
    @Json(name = "exit_description")
    val exitDescription: String? = null,
    @Json(name = "name_field_empty")
    val nameFieldEmpty: String? = null,
    @Json(name = "no_of_days_empty")
    val noOfDaysEmpty: String? = null,
    @Json(name = "days_should_not_be_0")
    val daysShouldNotBe0: String? = null,
    @Json(name = "dosage_quantity_is_empty")
    val dosageQuantityIsEmpty: String? = null,
    @Json(name = "dosage_quantity_should_not_be_0")
    val dosageQuantityShouldNotBe0: String? = null,
    @Json(name = "hours")
    val hours: String? = null,
    @Json(name = "large_file")
    val largeFile: String? = null,
    @Json(name = "microphone_disabled")
    val microphoneDisabled: String? = null,
    @Json(name = "microphone_settings")
    val microphoneSettings: String? = null,
    @Json(name = "voice_recording_msg")
    val voiceRecordingMsg: String? = null,
    @Json(name = "select_family")
    val selectFamily: String? = null,
    @Json(name = "edit_symptoms")
    val editSymptoms: String? = null,
    @Json(name = "edit_diagnosis")
    val editDiagnosis: String? = null,
    @Json(name = "edit_test")
    val editTest: String? = null,
    @Json(name = "edit_recommendation")
    val editRecommendation: String? = null,
    @Json(name = "dosage_day_wise")
    val dosageDayWise: String? = null,
    @Json(name = "dosage_hourly")
    val dosageHourly: String? = null,
    @Json(name = "medical_records")
    val medicalRecords: String? = null,
    @Json(name = "no_family_found")
    val noFamilyFound: String? = null,
    @Json(name = "share_record_with")
    val shareRecordWith: String? = null,
    @Json(name = "consultation")
    val consultaion: String? = null,
    @Json(name = "medications")
    val medications: String? = null,
    @Json(name = "reports")
    val reports: String? = null,
    @Json(name = "search_by_lab_diagnostic")
    val searchByLabDiagnostic: String? = null,
    @Json(name = "search_medicine")
    val searchMedicineByName: String? = null,
    @Json(name = "add_record")
    val addRecord: String? = null,
    @Json(name = "search_by_record_number")
    val searchByRecordNumber: String? = null,
    @Json(name = "add_document_image")
    val addDocumentImage: String? = null,
    @Json(name = "add_lab_reports_desc")
    val addLabReportDesc: String? = null,
    @Json(name = "record_date")
    val recordDate: String? = null,
    @Json(name = "record_date_description")
    val recordDateDescription: String? = null,
    @Json(name = "new_medical_record")
    val newMedicalRecord: String? = null,
    @Json(name = "save_time_prescription")
    val saveTimePrescription: String? = null,
    @Json(name = "save_record")
    val saveRecord: String? = null,
    @Json(name = "add_medicine_description")
    val addMedicineDesc: String? = null,
    @Json(name = "record")
    val record: String? = null,
    @Json(name = "add_details")
    val addDetails: String? = null,
    @Json(name = "number_of_days")
    val numberOfDays: String? = null,
    @Json(name = "shared_record")
    val sharedRecord: String? = null,
    @Json(name = "referred_by")
    val referredBy: String? = null,
    @Json(name = "medicine_type")
    val medicineType: String? = null,
    @Json(name = "test_and_procedure")
    val testAndProcedure: String? = null,
    @Json(name = "report_document")
    val reportDocument: String? = null,
    @Json(name = "report_document_desc")
    val reportDocumentDesc: String? = null,
    @Json(name = "upload_report")
    val uploadReport: String? = null,
    @Json(name = "enter_date")
    val enterDate: String? = null,
    @Json(name = "service_type")
    val serviceType: String? = null,
    @Json(name = "labs_and_diagnostics")
    val labAndDiagnostics: String? = null,
    @Json(name = "labs_and_diagnostics_desc")
    val labAndDiagnosticsDesc: String? = null,
    @Json(name = "consultation_records")
    val consultationRecords: String? = null,
    @Json(name = "vitals_symptoms")
    val vitalsSymptoms: String? = null,
    @Json(name = "medical_advice")
    val medicalAdvice: String? = null
)

data class MyOrdersScreens(
    @Json(name = "my_orders")
    val myOrders: String? = null,
    @Json(name = "current")
    val current: String? = null,
    @Json(name = "history")
    val history: String? = null,
    @Json(name = "unread")
    val unRead: String? = null,
    @Json(name = "upcoming")
    val upcoming: String? = null,
    @Json(name = "add_a_review")
    val addAReview: String? = null,
    @Json(name = "add_review")
    val addReview: String? = null,
    @Json(name = "after_review_home")
    val afterReviewHome: String? = null,
    @Json(name = "after_review_video")
    val afterReviewVideo: String? = null,
    @Json(name = "amount_unavailable")
    val amountUnavailable: String? = null,
    @Json(name = "appointment_rejected")
    val appointmentRejected: String? = null,
    @Json(name = "approval_pending")
    val approvalPending: String? = null,
    @Json(name = "approval_pending_appointment")
    val approvalPendingAppointment: String? = null,
    @Json(name = "booked_by")
    val bookedBy: String? = null,
    @Json(name = "booked_for")
    val bookedFor: String? = null,
    @Json(name = "cancel_appointment")
    val cancelAppointment: String? = null,
    @Json(name = "cancel_order")
    val cancelOrder: String? = null,
    @Json(name = "cancel_request")
    val cancelRequest: String? = null,
    @Json(name = "cancelation_msg")
    val cancelationMsg: String? = null,
    @Json(name = "canceled")
    val canceled: String? = null,
    @Json(name = "canceled_phar_lab_order")
    val canceledPharLabOrder: String? = null,
    @Json(name = "completed")
    val completed: String? = null,
    @Json(name = "confirmed_msg")
    val confirmedMsg: String? = null,
    @Json(name = "confirmed_msg_homecare")
    val confirmedMsgHomecare: String? = null,
    @Json(name = "delivery_charges")
    val deliveryCharges: String? = null,
    @Json(name = "duties_cancel")
    val dutiesCancel: String? = null,
    @Json(name = "error_api")
    val errorApi: String? = null,
    @Json(name = "home_care")
    val homeCare: String? = null,
    @Json(name = "home_collection")
    val homeCollection: String? = null,
    @Json(name = "home_collection_charges")
    val homeCollectionCharges: String? = null,
    @Json(name = "home_visit_complete")
    val homeVisitComplete: String? = null,
    @Json(name = "home_visit_dr_coming")
    val homeVisitDrComing: String? = null,
    @Json(name = "home_visit_duty")
    val homeVisitDuty: String? = null,
    @Json(name = "home_visit_fee")
    val homeVisitFee: String? = null,
    @Json(name = "homecare_coming")
    val homecareComing: String? = null,
    @Json(name = "homecare_multi_visit")
    val homecareMultiVisit: String? = null,
    @Json(name = "homecare_next_visit")
    val homecareNextVisit: String? = null,
    @Json(name = "lab_address")
    val labAddress: String? = null,
    @Json(name = "lab_feedback")
    val labFeedback: String? = null,
    @Json(name = "lab_home_confirm")
    val labHomeConfirm: String? = null,
    @Json(name = "lab_order_completed")
    val labOrderCompleted: String? = null,
    @Json(name = "lab_order_wo_report")
    val labOrderWoReport: String? = null,
    @Json(name = "lab_visit_confirm")
    val labVisitConfirm: String? = null,
    @Json(name = "location_permission")
    val locationPermission: String? = null,
    @Json(name = "location_permission_detail")
    val locationPermissionDetail: String? = null,
    @Json(name = "message_after_compltion")
    val messageAfterCompltion: String? = null,
    @Json(name = "message_consultation")
    val messageConsultation: String? = null,
    @Json(name = "message_consultation_fee")
    val messageConsultationFee: String? = null,
    @Json(name = "message_consultation_msg")
    val messageConsultationMsg: String? = null,
    @Json(name = "message_consultation_msg_other")
    val messageConsultationMsgOther: String? = null,
    @Json(name = "message_feedback")
    val messageFeedback: String? = null,
    @Json(name = "msg_consultation")
    val msgConsultation: String? = null,
    @Json(name = "multi_visit_completion")
    val multiVisitCompletion: String? = null,
    @Json(name = "next_duty_is")
    val nextDutyIs: String? = null,
    @Json(name = "no_details_added")
    val noDetailsAdded: String? = null,
    @Json(name = "not_available_medical_records")
    val notAvailableMedicalRecords: String? = null,
    @Json(name = "order_details")
    val orderDetails: String? = null,
    @Json(name = "order_num")
    val orderNum: String? = null,
    @Json(name = "pay_confirm")
    val payConfirm: String? = null,
    @Json(name = "payment_mode")
    val paymentMode: String? = null,
    @Json(name = "pending_order")
    val pendingOrder: String? = null,
    @Json(name = "pharmacy_alternate")
    val pharmacyAlternate: String? = null,
    @Json(name = "pharmacy_changes")
    val pharmacyChanges: String? = null,
    @Json(name = "pharmacy_confirm")
    val pharmacyConfirm: String? = null,
    @Json(name = "pharmacy_order_completed")
    val pharmacyOrderCompleted: String? = null,
    @Json(name = "pharmacy_order_delivered_feedback")
    val pharmacyOrderDeliveredFeedback: String? = null,
    @Json(name = "pharmacy_substitude")
    val pharmacySubstitude: String? = null,
    @Json(name = "request_pending_homecare")
    val requestPendingHomecare: String? = null,
    @Json(name = "reschedule_msg")
    val rescheduleMsg: String? = null,
    @Json(name = "review")
    val review: String? = null,
    @Json(name = "sample_collected")
    val sampleCollected: String? = null,
    @Json(name = "settings")
    val settings: String? = null,
    @Json(name = "time_remaining")
    val timeRemaining: String? = null,
    @Json(name = "unconfirmed_home_message")
    val unconfirmedHomeMessage: String? = null,
    @Json(name = "us")
    val us: String? = null,
    @Json(name = "video_call_feedback")
    val videoCallFeedback: String? = null,
    @Json(name = "video_consultation")
    val videoConsultation: String? = null,
    @Json(name = "visit_address")
    val visitAddress: String? = null,
    @Json(name = "visit_schedules")
    val visitSchedules: String? = null,
    @Json(name = "writeReview")
    val writeReview: String? = null,
    @Json(name = "no_orders_available")
    val noOrdersAvailable: String? = null,
    @Json(name = "cancel_orders_refunds")
    val cancelOrderRefunds: String? = null,
    @Json(name = "star")
    val star: String? = null,
    @Json(name = "stars")
    val stars: String? = null,
    @Json(name = "order_pending_desc")
    val orderPendingDesc: String? = null,
    @Json(name = "doctor_on_way")
    val doctorOnWay: String? = null,
    @Json(name = "order")
    val order: String? = null,
    @Json(name = "visit_complete_session")
    val visitCompleteSession: String? = null,
    @Json(name = "medical_staff_on_way")
    val medicalStaffOnWay: String? = null,
    @Json(name = "next_session")
    val nextSession: String? = null,
    @Json(name = "sample_pending")
    val samplePending: String? = null,
    @Json(name = "review_done")
    val reviewDone: String? = null,
    @Json(name = "delivery_alert")
    val deliveryAlert: String? = null,
    @Json(name = "pharmacy_complete_feedback")
    val pharmacyCompleteFeedback: String? = null,
    @Json(name = "call_thank_you")
    val callThankYou: String? = null,
    @Json(name = "book_staff_contact")
    val bookStaffContact: String? = null
)

data class ClaimScreen(
    @Json(name = "select_a_service")
    val selectService: String? = null,
    @Json(name = "msg_select_service")
    val msgSelectService: String? = null,
    @Json(name = "submit_claim")
    val submitClaim: String? = null,
    @Json(name = "msg_enter_invoice_amount")
    val msgEnterInvoiceAmount: String? = null,
    @Json(name = "enter_invoice_amount")
    val enterInvoiceAmount: String? = null,
    @Json(name = "enter_lab_pharma_hospital")
    val enterLabPharmaHospital: String? = null,
    @Json(name = "comments")
    val comments: String? = null,
    @Json(name = "head_review_exclusions")
    val headReviewExclusions: String? = null,
    @Json(name = "documents_to_support_your_claim")
    val documentsToSupportYourClaim: String? = null,
    @Json(name = "documents_supporting_your_claim")
    val documentsSupportingYourClaim: String? = null,
    @Json(name = "upload_document")
    val uploadDocument: String? = null,
    @Json(name = "invoice_")
    val invoice_: String? = null,
    @Json(name = "prescription_")
    val prescription_: String? = null,
    @Json(name = "bank_statement_")
    val bankStatement_: String? = null,
    @Json(name = "other")
    val other: String? = null,
    @Json(name = "checkout_claim")
    val checkoutClaim: String? = null,
    @Json(name = "claim_details")
    val claimDetails: String? = null,
    @Json(name = "review_exclusion")
    val reviewExclusion: String? = null,
    @Json(name = "claim")
    val claim: String? = null,
    @Json(name = "my_claims")
    val myClaims: String? = null,
    @Json(name = "contact_our_helpline")
    val contactOurHelpline: String? = null,
    @Json(name = "msg_claim_submitted")
    val msgClaimSubmitted: String? = null,
    @Json(name = "msg_claim_received")
    val msgClaimReceived: String? = null,
    @Json(name = "msg_claim_cancelled")
    val msgClaimCancelled: String? = null,
    @Json(name = "msg_claim_docs_required")
    val msgClaimDocsRequired: String? = null,
    @Json(name = "msg_claim_approved")
    val msgClaimApproved: String? = null,
    @Json(name = "msg_claim_rejected")
    val msgClaimRejected: String? = null,
    @Json(name = "msg_claim_not_settled")
    val msgClaimNotSettled: String? = null,
    @Json(name = "msg_claim_settled")
    val msgClaimSettled: String? = null,
    @Json(name = "msg_claim_settlement_in_progress")
    val msgClaimSettlementInProgress: String? = null,
    @Json(name = "msg_claim_settlement_on_hold")
    val msgClaimSettlementOnHold: String? = null,
    @Json(name = "msg_claim_on_hold")
    val msgClaimOnHold: String? = null,
    @Json(name = "pharma_claim_exclusion")
    val pharmaClaimExclusion: String? = null,
    @Json(name = "lab_claim_exclusion")
    val labClaimExclusion: String? = null,
    @Json(name = "other_claim_exclusion")
    val otherClaimExclusion: String? = null,
    @Json(name = "no_item_excluded")
    val noItemsExcluded: String? = null,
    @Json(name = "no_categories_found")
    val noCategoriesFound: String? = null,

    @Json(name = "heading_claim_exclusion")
    val headingClaimExclusion: String? = null,
    @Json(name = "excluded_items")
    val excludedItems: String? = null
)


data class WalkInScreens(
    @Json(name = "hospital_services")
    val hospitalServices: String? = null,
    @Json(name = "hospital_and_services")
    val hospitalAndServices: String? = null,
    @Json(name = "map_view")
    val mapView: String? = null,
    @Json(name = "nearby_facility")
    val nearbyFacility: String? = null,
    @Json(name = "nearby_list")
    val nearbyList: String? = null,
    @Json(name = "package_account")
    val packageAccount: String? = null,
    @Json(name = "search_hint")
    val searchHint: String? = null,
    @Json(name = "select_healthcare")
    val selectHealthcare: String? = null,
    @Json(name = "select_service")
    val selectService: String? = null,
    @Json(name = "invoice_desc")
    val invoiceDesc: String? = null,
    @Json(name = "search_hint_pharmacy")
    val searchHintPharmacy: String? = null,
    @Json(name = "search_hint_lab")
    val searchHintLab: String? = null,
    @Json(name = "walk_in_desc")
    val walkInDesc: String? = null,
    @Json(name = "walk_in_services")
    val walkInServices: String? = null,
    @Json(name = "walk_in_hospital")
    val walkInHospital: String? = null,
    @Json(name = "near_by_laboratories")
    val nearByLaboratories: String? = null,
    @Json(name = "pharmacy_service")
    val pharmacyServices: String? = null,
    @Json(name = "enter_invoice")
    val enterInvoiceAmount: String? = null,
    @Json(name = "walk_in_pharmacy")
    val walkInPharmacy: String? = null,
    @Json(name = "add_attachments")
    val addAttachment: String? = null,
    @Json(name = "walk_in_pharmacy_desc")
    val walkInPharmacyDesc: String? = null,
    @Json(name = "upload_document")
    val uploadDocument: String? = null,
    @Json(name = "laboratory_services")
    val laboratoryServices: String? = null,
    @Json(name = "walkin_laboratory")
    val walkinLaboratory: String? = null,
    @Json(name = "walk_in_laboratory_desc")
    val walkinLaboratoryDesc: String? = null,
    @Json(name = "submit_for_review")
    val submitForReview: String? = null,
    @Json(name = "resend_confirmation")
    val resendConfirmation: String? = null,
    @Json(name = "payment_")
    val payment_: String? = null,
    @Json(name = "msg_status_under_review")
    val msgStatusUnderReview: String? = null,
    @Json(name = "msg_status_hold")
    val msgStatusHold: String? = null,
    @Json(name = "msg_status_unauthorised")
    val msgStatusUnauthorised: String? = null,
    @Json(name = "msg_status_cancelled")
    val msgStatusCancelled: String? = null,
    @Json(name = "msg_status_unconfirmed")
    val msgStatusUnconfirmed: String? = null,
    @Json(name = "near_by_pharmacies")
    val nearbyPharmacies: String? = null,
    @Json(name = "on_hold")
    val creditsOnHold: String? = null
)