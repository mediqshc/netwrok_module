package com.fatron.network_module.repository

import com.fatron.network_module.models.request.AppointmentStatusRequest
import com.fatron.network_module.models.request.activedays.ActiveDaysRequest
import com.fatron.network_module.models.request.appointments.AppointmentDetailReq
import com.fatron.network_module.models.request.appointments.AppointmentListRequest
import com.fatron.network_module.models.request.appointments.AppointmentsActionRequest
import com.fatron.network_module.models.request.auth.*
import com.fatron.network_module.models.request.bdc.BDCFilterRequest
import com.fatron.network_module.models.request.bdc.BookConsultationRequest
import com.fatron.network_module.models.request.bdc.DeleteAttachmentRequest
import com.fatron.network_module.models.request.chat.*
import com.fatron.network_module.models.request.checkout.paymob.PaymobPaymentRequest
import com.fatron.network_module.models.request.checkout.paymob.PaymobPaymentStatusRequest
import com.fatron.network_module.models.request.claim.*
import com.fatron.network_module.models.request.email.EmailSendRequest
import com.fatron.network_module.models.request.email.EmailVerifyRequest
import com.fatron.network_module.models.request.emr.AttachEMRtoBDCRequest
import com.fatron.network_module.models.request.emr.EMRDownloadRequest
import com.fatron.network_module.models.request.emr.EMRShareWithRequest
import com.fatron.network_module.models.request.emr.StoreEMRRequest
import com.fatron.network_module.models.request.emr.customer.consultation.EMRConsultationFilterRequest
import com.fatron.network_module.models.request.emr.customer.records.EMRRecordsFilterRequest
import com.fatron.network_module.models.request.emr.medicine.MedicineDeleteRequest
import com.fatron.network_module.models.request.emr.medicine.StoreMedicineRequest
import com.fatron.network_module.models.request.emr.type.*
import com.fatron.network_module.models.request.family.FamilyConnectionActionRequest
import com.fatron.network_module.models.request.family.FamilyRequest
import com.fatron.network_module.models.request.fcm.UpdateFCMTokenRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceDetailRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceListRequest
import com.fatron.network_module.models.request.homeservice.HomeServiceStoreRequest
import com.fatron.network_module.models.request.labtest.LabTestCartRequest
import com.fatron.network_module.models.request.labtest.LabTestFilterRequest
import com.fatron.network_module.models.request.labtest.LabTestHomeCollectionRequest
import com.fatron.network_module.models.request.linkaccount.CompanyRequest
import com.fatron.network_module.models.request.linkaccount.DeleteLinkRequest
import com.fatron.network_module.models.request.linkaccount.LinkCompanyRequest
import com.fatron.network_module.models.request.linkaccount.LinkInsuranceRequest
import com.fatron.network_module.models.request.notification.CreateNotifRequest
import com.fatron.network_module.models.request.notification.NotificationReadRequest
import com.fatron.network_module.models.request.notification.NotificationRequest
import com.fatron.network_module.models.request.offdates.DeleteOffDatesRequest
import com.fatron.network_module.models.request.offdates.OffDatesRequest
import com.fatron.network_module.models.request.orders.MyOrdersRequest
import com.fatron.network_module.models.request.orders.OrdersRequest
import com.fatron.network_module.models.request.orders.RescheduleRequest
import com.fatron.network_module.models.request.orders.ScheduledVisitsRequest
import com.fatron.network_module.models.request.partnerprofile.*
import com.fatron.network_module.models.request.pharmacy.*
import com.fatron.network_module.models.request.planschedule.AddSlotRequest
import com.fatron.network_module.models.request.user.UserLocation
import com.fatron.network_module.models.request.user.UserRequest
import com.fatron.network_module.models.request.video.ParticipantsRequest
import com.fatron.network_module.models.request.video.RoomRequest
import com.fatron.network_module.models.request.video.TokenRequest
import com.fatron.network_module.models.request.walkin.*
import com.fatron.network_module.models.response.AppointmentStatusResponse
import com.fatron.network_module.models.response.DeleteSlotsResponse
import com.fatron.network_module.models.response.EconResponse
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.activedays.ActiveDaysResponse
import com.fatron.network_module.models.response.activedays.AddActiveDaysResponse
import com.fatron.network_module.models.response.appointments.AppointmentListResponse
import com.fatron.network_module.models.response.appointments.AppointmentResponse
import com.fatron.network_module.models.response.appointments.AppointmentServicesResponse
import com.fatron.network_module.models.response.appointments.AttachmentResponse
import com.fatron.network_module.models.response.auth.ForgetPwdResponse
import com.fatron.network_module.models.response.auth.OtpResponse
import com.fatron.network_module.models.response.bdc.BDCFilterResponse
import com.fatron.network_module.models.response.bdc.PartnerSlotsResponse
import com.fatron.network_module.models.response.becomepartner.PartnerCnicResponse
import com.fatron.network_module.models.response.chat.ConversationResponse
import com.fatron.network_module.models.response.chat.ConverstaionListResponse
import com.fatron.network_module.models.response.chat.TwilioTokenResponse
import com.fatron.network_module.models.response.checkout.CheckoutDetailResponse
import com.fatron.network_module.models.response.checkout.paymob.PaymobOrderResponse
import com.fatron.network_module.models.response.checkout.paymob.PaymobPaymentResponse
import com.fatron.network_module.models.response.checkout.paymob.PaymobTokenResponse
import com.fatron.network_module.models.response.claim.*
import com.fatron.network_module.models.response.email.HospitalDiscountCenter
import com.fatron.network_module.models.response.email.VerifyEmailResponse
import com.fatron.network_module.models.response.emr.EMRDownloadResponse
import com.fatron.network_module.models.response.emr.StoreEMRResponse
import com.fatron.network_module.models.response.emr.customer.consultation.ConsultationRecordsListResponse
import com.fatron.network_module.models.response.emr.customer.medicine.ReferredListResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerEMRRecordResponse
import com.fatron.network_module.models.response.emr.customer.records.CustomerRecordsListResponse
import com.fatron.network_module.models.response.emr.type.*
import com.fatron.network_module.models.response.family.FamilyConnection
import com.fatron.network_module.models.response.family.FamilyResponse
import com.fatron.network_module.models.response.homeservice.HomeServiceDetailResponse
import com.fatron.network_module.models.response.homeservice.HomeServiceListResponse
import com.fatron.network_module.models.response.labtest.LabBranchListResponse
import com.fatron.network_module.models.response.labtest.LabTestCategoriesResponse
import com.fatron.network_module.models.response.labtest.LabTestListResponse
import com.fatron.network_module.models.response.linkaccount.*
import com.fatron.network_module.models.response.meta.MetaDataResponse
import com.fatron.network_module.models.response.notification.NotificationCountResponse
import com.fatron.network_module.models.response.notification.NotificationResponse
import com.fatron.network_module.models.response.offdates.OffDatesResponse
import com.fatron.network_module.models.response.orders.ScheduledDutiesListResponse
import com.fatron.network_module.models.response.ordersdetails.OrderResponse
import com.fatron.network_module.models.response.ordersdetails.OrdersListResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerAvailabilityResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerProfileResponse
import com.fatron.network_module.models.response.partnerprofile.PartnerReviewsResponse
import com.fatron.network_module.models.response.pharmacy.*
import com.fatron.network_module.models.response.planschedule.AddSlotResponse
import com.fatron.network_module.models.response.planschedule.SlotsDataResponse
import com.fatron.network_module.models.response.planschedule.deleteSlotRequest
import com.fatron.network_module.models.response.user.ProfilePicResponse
import com.fatron.network_module.models.response.user.UserLocationResponse
import com.fatron.network_module.models.response.user.UserResponse
import com.fatron.network_module.models.response.video.RoomResponse
import com.fatron.network_module.models.response.video.VideoTokenResponse
import com.fatron.network_module.models.response.walkin.WalkInDiscountsResponse
import com.fatron.network_module.models.response.walkin.WalkInQRCodeResponse
import com.fatron.network_module.models.response.walkin.WalkInResponse
import com.fatron.network_module.models.response.walkin.services.WalkInServices
import com.fatron.network_module.models.response.walkinpharmacy.ServiceTypes
import com.fatron.network_module.models.response.walkinpharmacy.WalkInInitialResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInPharmacyListResponse
import com.fatron.network_module.models.response.walkinpharmacy.WalkInStoreResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*
import  com.fatron.network_module.models.response.packages.Package

interface RetrofitAPI {


    companion object {
        const val HEADER_POSTFIX = ": "
        const val HEADER_TAG = "@"
        const val HEADER_TAG_PUBLIC = "public"
//        val user = TinyDB.instance.getObject(
//            Enums.TinyDBKeys.USERPROFILE.key,
//            PatientProfileResponse::class.java
//        ) as PatientProfileResponse
//        val patientId = user.patientId
    }

    @POST("v1/login")
    suspend fun login(@Body loginRequest: LoginRequest): ResponseGeneral<UserResponse>

    @GET("v1/logout")
    suspend fun logout(
        @Query("device_token", encoded = true) deviceToken: String?
    ): ResponseGeneral<*>

    @POST("v1/phone_number/verification")
    suspend fun verifyPhoneNum(@Body loginRequest: LoginRequest): ResponseGeneral<UserResponse>

    @POST("v1/econ/subscribe")
    suspend fun subscribe(@Body loginRequest: EconSubscribeRequest): ResponseGeneral<EconResponse>

    @POST("v1/econ/unsubscribe")
    suspend fun unsubscribe(@Body loginRequest: EconOtpRequest): ResponseGeneral<EconResponse>

    @POST("v1/econ/otp")
    suspend fun sendEconOtp(@Body loginRequest: EconOtpRequest): ResponseGeneral<EconResponse>

    @POST("v1/econ/verify-otp")
    suspend fun verifyEconOtp(@Body loginRequest: EconSubscribeRequest): ResponseGeneral<UserResponse>


    @GET("v1/meta")
    suspend fun metaData(
        @Query(
            "tid",
            encoded = true
        ) tid: String
    ): ResponseGeneral<MetaDataResponse>

    @GET("v1/profile/basic")
    suspend fun getProfile(): ResponseGeneral<UserResponse>

    @POST("v1/profile/basic/create")
    suspend fun createProfile(@Body userRequest: UserRequest): ResponseGeneral<UserResponse>

    @POST("v1/register")
    suspend fun register(@Body signupRequest: SignupRequest): ResponseGeneral<UserResponse>

    @POST("v1/otp")
    suspend fun sendOtp(@Body otpRequest: OtpRequest): ResponseGeneral<OtpResponse>

    @POST("v1/otp/verify")
    suspend fun verifyOtp(@Body verifyOtpRequest: ForgetPwdRequest): ResponseGeneral<ForgetPwdRequest>

    @POST("v1/forget_password")
    suspend fun forgetPassword(@Body forgetPwdRequest: ForgetPwdRequest): ResponseGeneral<ForgetPwdResponse>

    @POST("v1/delete/account")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): ResponseGeneral<*>

    //companies
    @GET("v1/companies/cooperate")
    suspend fun getCooperateCompList(): ResponseGeneral<CompanyListResponse>

    @POST("v1/companies/link")
    suspend fun linkCompany(@Body request: LinkCompanyRequest): ResponseGeneral<*>

    @POST("v1/companies/delete/link")
    suspend fun deleteCompanyLink(@Body request: DeleteLinkRequest): ResponseGeneral<*>

    //insurances
    @GET("v1/insurances")
    suspend fun getInsuranceCompList(): ResponseGeneral<CompanyListResponse>

    @POST("v1/insurances/link")
    suspend fun linkInsuranceComp(@Body request: LinkInsuranceRequest): ResponseGeneral<*>

    @POST("v1/insurances/delete/link")
    suspend fun deleteInsuranceComp(@Body request: DeleteLinkRequest): ResponseGeneral<*>

    //hospitals
    @GET("v1/healthcares/hospital_and_clinics")
    suspend fun getHospitalsList(): ResponseGeneral<CompanyListResponse>

    @POST("v1/healthcares/link")
    suspend fun linkHospital(@Body request: LinkCompanyRequest): ResponseGeneral<*>

    @POST("v1/healthcares/delete/link")
    suspend fun deleteHospital(@Body request: DeleteLinkRequest): ResponseGeneral<*>

    @POST("v1/linkedAccounts")
    suspend fun getLinkedAccounts(@Body request: CompanyRequest): ResponseGeneral<LinkedAccountsResponse>

    @GET("v1/profile/family")
    suspend fun getFamilyConnectionList(@Query("emr") emr: Boolean? = null): ResponseGeneral<FamilyResponse>

    @POST("v1/econ/packages")
    suspend fun getPackages(
        @Query("network") network: String? = null,
        @Query("phone") phone: String? = null
    ): ResponseGeneral<List<Package>>

    @POST("v1/profile/family/create")
    suspend fun createFamilyConnection(@Body request: FamilyRequest): ResponseGeneral<FamilyConnection>

    @POST("v1/profile/family/delete")
    suspend fun deleteFamilyConnection(@Body request: FamilyConnectionActionRequest): ResponseGeneral<*>

    @POST("v1/profile/family/connect")
    suspend fun connectFamilyConnection(@Body request: FamilyConnectionActionRequest): ResponseGeneral<FamilyConnection>

    @Multipart
    @POST("v1/profile/basic/picture/store")
    suspend fun storeUserProfilePio(
        @Part profile_pic: MultipartBody.Part?
    ): ResponseGeneral<ProfilePicResponse>

    @POST("v1/partner/work/store")
    suspend fun callAddWorkExpApi(@Body request: WorkExperience): ResponseGeneral<*>

    @Multipart
    @POST("v1/partner/cnic/store")
    suspend fun storePartnerCnic(
        @Part cnic_front: MultipartBody.Part?,
        @Part cnic_back: MultipartBody.Part?,
        @Part("update") update: RequestBody?,
    ): ResponseGeneral<PartnerCnicResponse>

    @POST("v1/partner/details/store")
    suspend fun storePartnerDetails(
        @Body partnerDetailsRequest: PartnerDetailsRequest
    ): ResponseGeneral<PartnerProfileResponse>

    @GET("v1/partner/details")
    suspend fun getPartnerDetails(): ResponseGeneral<PartnerProfileResponse>

    @POST("v1/partner/specialties/store")
    suspend fun storePartnerSpecialities(@Body specialitiesRequest: SpecialitiesRequest): ResponseGeneral<Any>

    @POST("v1/partner/services/store")
    suspend fun storePartnerService(@Body serviceRequest: ServiceRequest): ResponseGeneral<Any>

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/partner/education/store")
    suspend fun storeEducation(
        @Part("degree") degree: RequestBody,
        @Part("school") school: RequestBody,
        @Part("country_id") country_id: RequestBody,
        @Part("year") year: RequestBody,
        @Part education_documents: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<*>

    @POST("v1/partner/work/delete")
    suspend fun deletePartnerWork(@Body deletePartnerWork: deletePartnerWork): ResponseGeneral<Any>

    @POST("v1/partner/education/delete")
    suspend fun calldeleteEducApi(@Body deletePartnerEducation: deletePartnerEducation): ResponseGeneral<Any>

    @POST("v1/partner/speciality/delete")
    suspend fun calldeleteSpeciality(@Body deletePartnerSpecialityReq: deletePartnerSpecialityReq): ResponseGeneral<Any>


    @POST("v1/email/verification/send")
    suspend fun sendEmail(@Body emailSendRequest: EmailSendRequest): ResponseGeneral<*>

    @POST("v1/email/verify")
    suspend fun verifyEmail(@Body emailVerifyRequest: EmailVerifyRequest): ResponseGeneral<VerifyEmailResponse>

    @GET("v1/schedule/slots")
    suspend fun getSlotsApiCall(): ResponseGeneral<SlotsDataResponse>

    @POST("v1/schedule/slots/add")
    suspend fun addSlotsApiCall(@Body addSlotRequest: AddSlotRequest): ResponseGeneral<AddSlotResponse>

    @POST("v1/schedule/slots/delete")
    suspend fun deleteSlotsApiCall(@Body deleteSlotRequest: deleteSlotRequest): ResponseGeneral<DeleteSlotsResponse>

    @GET("v1/schedule/active_days")
    suspend fun getActiveDays(): ResponseGeneral<ActiveDaysResponse>

    @POST("v1/schedule/active_days/add")
    suspend fun addActiveDays(@Body activeDaysRequest: ActiveDaysRequest): ResponseGeneral<AddActiveDaysResponse>

    @POST("v1/schedule/off_dates/add")
    suspend fun addOffDates(@Body offDatesRequest: OffDatesRequest): ResponseGeneral<DeleteSlotsResponse>

    @POST("v1/schedule/off_dates/delete")
    suspend fun deleteOffDates(@Body deleteOffDatesRequest: DeleteOffDatesRequest): ResponseGeneral<Any>

    @GET("v1/schedule/off_dates")
    suspend fun getOffDates(): ResponseGeneral<OffDatesResponse>

    @POST("v1/booking/partners/search")
    suspend fun getDoctors(@Body request: BDCFilterRequest): ResponseGeneral<BDCFilterResponse>

    @POST("v1/booking/partner/consultation")
    suspend fun getPartnerDetails(@Body request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest): ResponseGeneral<PartnerProfileResponse>

    @POST("v1/booking/partner/about")
    suspend fun getPartnerAbout(@Body request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest): ResponseGeneral<PartnerProfileResponse>

    @POST("v1/booking/partner/reviews")
    suspend fun getPartnerReviews(@Body request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest): ResponseGeneral<PartnerReviewsResponse>

    @POST("v1/booking/partner/consultation/store")
    suspend fun bookConsultation(@Body request: BookConsultationRequest): ResponseGeneral<PartnerProfileResponse>

    @POST("v1/profile/store/address")
    suspend fun saveAddressCall(@Body request: UserLocation): ResponseGeneral<UserLocationResponse>

    //delete document
    @POST("v1/booking/partner/consultation/attachment/delete")
    suspend fun deleteDocumentApiCall(@Body request: DeleteAttachmentRequest): ResponseGeneral<*>

    @POST("v1/booking/partner/consultation/slots")
    suspend fun getSlots(@Body request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest): ResponseGeneral<PartnerSlotsResponse>

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/booking/partner/consultation/attachment")
    suspend fun addConsultationAttachment(
        @Part("booking_id") booking_id: Int?,
        @Part("attachment_type") year: RequestBody,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<DeleteAttachmentRequest>

    //appointments

    @POST("v1/appointments")
    suspend fun getAppointments(@Body request: AppointmentListRequest): ResponseGeneral<AppointmentListResponse>

    @GET("v1/appointments/partner/services")
    suspend fun getAppointmentsServices(): ResponseGeneral<AppointmentServicesResponse>

    @POST("v1/appointments/reschedule")
    suspend fun appointmentsReschedule(
        @Body appointmentsActionRequest: AppointmentsActionRequest
    ): ResponseGeneral<Any>

    @POST("v1/appointments/completed")
    suspend fun appointmentsCompleted(
        @Body appointmentsActionRequest: AppointmentsActionRequest
    ): ResponseGeneral<Any>

    @POST("v1/appointments/reject")
    suspend fun appointmentsReject(
        @Body appointmentsActionRequest: AppointmentsActionRequest
    ): ResponseGeneral<Any>

    @POST("v1/appointments/accept")
    suspend fun appointmentsAccept(
        @Body appointmentsActionRequest: AppointmentsActionRequest
    ): ResponseGeneral<Any>


    @POST("v1/appointments/details")
    suspend fun getApptDetail(@Body appointmentDetailReq: AppointmentDetailReq): ResponseGeneral<AppointmentResponse>

    @POST("v1/booking/partner/get/attachments")
    suspend fun getAttachments(@Body appointmentDetailReq: AppointmentDetailReq): ResponseGeneral<AttachmentResponse>

    @POST("v1/appointments/status")
    suspend fun callChangeStatus(@Body appointmentStatusRequest: AppointmentStatusRequest): ResponseGeneral<AppointmentStatusResponse>

    @POST("v1/checkout/details")
    suspend fun checkoutDetails(@Body request: AppointmentDetailReq): ResponseGeneral<CheckoutDetailResponse>

    @POST("v1/orders")
    suspend fun getOrders(@Body ordersRequest: OrdersRequest): ResponseGeneral<OrdersListResponse>

    @POST("v1/orders/details")
    suspend fun orderDetails(@Body myOrdersRequest: TokenRequest): ResponseGeneral<OrderResponse>

    @POST("v1/orders/review/add")
    suspend fun addReview(@Body myOrdersRequest: MyOrdersRequest): ResponseGeneral<*>

    @POST("v1/orders/cancel")
    suspend fun cancelOrder(@Body myOrdersRequest: TokenRequest): ResponseGeneral<*>

    @POST("v1/orders/reschedule")
    suspend fun rescheduleOrder(@Body rescheduleRequest: RescheduleRequest): ResponseGeneral<*>

    @POST("v1/home-service/duties")
    suspend fun getHHCDuties(@Body rescheduleRequest: ScheduledVisitsRequest): ResponseGeneral<ScheduledDutiesListResponse>

    @POST("v1/video/start-call")
    suspend fun getTwilioVideoCallToken(@Body tokenRequest: TokenRequest): ResponseGeneral<VideoTokenResponse>

    @POST("v1/video/add-participants")
    suspend fun addParticipantsToVideoCall(@Body requestParticipantsRequest: ParticipantsRequest): ResponseGeneral<*>

    @POST("v1/video/end-call")
    suspend fun destroyRoom(@Body requestParticipantsRequest: ParticipantsRequest): ResponseGeneral<*>

    @POST("v1/video/is-room-exists")
    suspend fun isRoomExist(@Body requestRoomRequest: RoomRequest): ResponseGeneral<RoomResponse>

    @POST("v1/video/update-fcm-token")
    suspend fun updateFCMToken(@Body fcmToken: UpdateFCMTokenRequest): ResponseGeneral<*>

    //homeservice
    @POST("v1/home-service/services")
    suspend fun getHomeServiceList(@Body homeServiceListRequest: HomeServiceListRequest): ResponseGeneral<HomeServiceListResponse>

    @POST("v1/home-service/booking")
    suspend fun getHomeServiceDetails(@Body homeServiceDetailRequest: HomeServiceDetailRequest): ResponseGeneral<HomeServiceDetailResponse>

    @POST("v1/home-service/booking/store")
    suspend fun callApiBookService(@Body homeServiceStoreRequest: HomeServiceStoreRequest): ResponseGeneral<*>

    // EMR
    @POST("v1/emr/create")
    suspend fun emrCreate(@Body symptomsRequest: SymptomsRequest): ResponseGeneral<EmrCreateResponse>

    @POST("v1/emr/symptoms/list")
    suspend fun getSymptoms(@Body pageRequest: PageRequest): ResponseGeneral<GenericEMRListResponse>

    @POST("v1/emr/type/store")
    suspend fun storeEMRType(@Body storeEMRTypeRequest: StoreEMRTypeRequest): ResponseGeneral<StoreEMRTypeResponse>

    @POST("v1/emr/details")
    suspend fun emrDetails(@Body request: EMRDetailsRequest): ResponseGeneral<EMRDetailsResponse>

    @POST("v1/emr/draft")
    suspend fun emrDrafts(@Body symptomsRequest: SymptomsRequest): ResponseGeneral<EMRDetailsResponse>

    @POST("v1/emr/type/delete")
    suspend fun deleteEMRType(@Body request: EMRTypeDeleteRequest): ResponseGeneral<*>

    @POST("v1/emr/type/edit")
    suspend fun editEMRType(@Body storeEMRTypeRequest: StoreEMRTypeRequest): ResponseGeneral<EMRTypeEditResponse>

    @POST("v1/emr/store")
    suspend fun storeEMR(@Body storeEMRRequest: StoreEMRRequest): ResponseGeneral<StoreEMRResponse>

    @POST("v1/emr/lab_tests/list")
    suspend fun getLabTests(@Body pageRequest: PageRequest): ResponseGeneral<GenericEMRListResponse>

    @POST("v1/emr/medical_healthcares/list")
    suspend fun getMedicalHealthCares(@Body pageRequest: PageRequest): ResponseGeneral<GenericEMRListResponse>

    @POST("v1/emr/diagnosis/list")
    suspend fun getDiagnosisList(@Body pageRequest: PageRequest): ResponseGeneral<GenericEMRListResponse>

    //medicine

    @POST("v1/emr/medicines/list")
    suspend fun getMedicineList(@Body pageRequest: PageRequest): ResponseGeneral<GenericEMRListResponse>

    @POST("v1/emr/medicines/store")
    suspend fun storeMedicine(@Body request: StoreMedicineRequest): ResponseGeneral<*>

    @POST("v1/emr/medicines/edit")
    suspend fun editMedicine(@Body request: StoreMedicineRequest): ResponseGeneral<*>

    @POST("v1/emr/medicines/delete")
    suspend fun deleteMedicine(@Body request: MedicineDeleteRequest): ResponseGeneral<*>

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/emr/attachments")
    suspend fun addEMRAttachment(
        @Part("emr_id") emr_id: Int,
        @Part("attachment_type") attachment_type: RequestBody,
        @Part("emr_type") emr_type: Int,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<DeleteAttachmentRequest>

    @POST("v1/emr/attachments/delete")
    suspend fun deleteEMRDocument(@Body request: DeleteAttachmentRequest): ResponseGeneral<*>

    //customer EMR
    //consultation
    @POST("v1/emr/customer/consultation/records")
    suspend fun getCustomerConsultationRecords(@Body request: EMRConsultationFilterRequest): ResponseGeneral<ConsultationRecordsListResponse>

    @POST("v1/emr/customer/consultation/record/details")
    suspend fun getCustomerConsultationRecordDetails(@Body request: EMRDetailsRequest): ResponseGeneral<EmrDetails>

    @POST("v1/emr/customer/consultation/record/share")
    suspend fun customerEMRRecordShare(@Body request: EMRShareWithRequest): ResponseGeneral<*>

    //records
    @POST("v1/emr/customer/records")
    suspend fun getCustomerEMRRecords(@Body request: EMRRecordsFilterRequest): ResponseGeneral<CustomerRecordsListResponse>

    @POST("v1/emr/customer/records/details")
    suspend fun getCustomerEMRRecordsDetails(@Body request: EMRDetailsRequest): ResponseGeneral<CustomerEMRRecordResponse>

    @POST("v1/emr/customer/records/add")
    suspend fun addCustomerMedicineRecord(@Body request: StoreMedicineRequest): ResponseGeneral<EMRTypeEditResponse>

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/emr/customer/records/attachments")
    suspend fun addCustomerEMRAttachment(
        @Part("emr_id") emr_id: Int,
        @Part("emr_customer_type") emr_customer_type: Int?,
        @Part("emr_type_id") emr_type_id: Int?,
        @Part("attachment_type") attachment_type: RequestBody,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<AttachmentResponse>

    @POST("v1/emr/customer/records/attachments/delete")
    suspend fun deleteCustomerEMRAttachment(@Body request: DeleteAttachmentRequest): ResponseGeneral<AttachmentResponse>

    @POST("v1/emr/customer/records/delete")
    suspend fun deleteCustomerEMRRecordType(@Body request: EMRTypeDeleteRequest): ResponseGeneral<*>

    @POST("v1/emr/customer/save")
    suspend fun saveCustomerEMRRecord(@Body request: EMRDetailsRequest): ResponseGeneral<*>

    @POST("v1/emr/customer/delete")
    suspend fun deleteCustomerEMRRecord(@Body request: EMRDetailsRequest): ResponseGeneral<*>

    @GET("v1/emr/customer/records/referred_list")
    suspend fun getReferredList(): ResponseGeneral<ReferredListResponse>

    @POST("v1/emr/customer/attach")
    suspend fun attachEMRtoBDC(@Body request: AttachEMRtoBDCRequest): ResponseGeneral<*>

    @POST("v1/emr/customer/detach")
    suspend fun detachEMRtoBDC(@Body request: AttachEMRtoBDCRequest): ResponseGeneral<*>

    @POST("v1/emr/customer/records/download")
    suspend fun downloadEMR(@Body request: EMRDownloadRequest): ResponseGeneral<EMRDownloadResponse>

    //chat
    @POST()
    suspend fun getPartnersList(
        @Body conversationRequest: ConversationListRequest,
        @Url url: String
    ): ResponseGeneral<ConverstaionListResponse>

    @POST("v1/chat/get/token")
    suspend fun getTwilioChatToken(@Body twilioTokenRequest: TwilioTokenRequest): ResponseGeneral<TwilioTokenResponse>

    @POST("v1/chat/start/session")
    suspend fun getTwilioSessionStart(@Body conversationRequest: ConversationSessionRequest): ResponseGeneral<*>

    @POST("v1/chat/channel/details")
    suspend fun callChatDetail(@Body chatDetailRequest: chatDetailRequest): ResponseGeneral<ConversationResponse>

    @POST("v1/emr/customer/doctor")
    suspend fun callSendEmrApi(@Body sendEmrRequest: SendEmrRequest): ResponseGeneral<Any>

    //paymob

    @GET("v1/paymob/token")
    suspend fun getPaymobToken(): ResponseGeneral<PaymobTokenResponse>

    @POST("v1/paymob/orders")
    suspend fun paymobOrders(@Body request: PaymobPaymentRequest): ResponseGeneral<PaymobOrderResponse>

    @POST("v1/paymob/payment")
    suspend fun paymobPayment(@Body request: PaymobPaymentRequest): ResponseGeneral<PaymobPaymentResponse>

    @POST("v1/paymob/payment/status")
    suspend fun paymobPaymentStatus(@Body request: PaymobPaymentStatusRequest): ResponseGeneral<ConversationResponse>

    // pharmacy
    @POST("v1/pharmacy/categories")
    suspend fun getPharmacyCategories(@Body request: PharmacyCategoriesRequest): ResponseGeneral<PharmacyCategoriesResponse>

    @POST("v1/pharmacy/products")
    suspend fun getPharmacyProductsList(@Body request: PharmacyProductRequest): ResponseGeneral<PharmacyProductsListResponse>

    @POST("v1/pharmacy/product/details")
    suspend fun pharmacyProductDetails(@Body request: PharmacyProductDetailRequest): ResponseGeneral<PharmacyProduct>

    @POST("v1/pharmacy/add/cart")
    suspend fun pharmaAddToCart(@Body request: PharmacyCartRequest): ResponseGeneral<PharmacyCartResponse>

    @POST("v1/pharmacy/clear/cart")
    suspend fun pharmaClearCart(@Body request: PharmacyOrderRequest): ResponseGeneral<*>

    @POST("v1/pharmacy/product/quantity/update")
    suspend fun pharmacyQuantityUpdate(@Body request: PharmacyCartRequest): ResponseGeneral<*>

    @POST("v1/pharmacy/order/details")
    suspend fun pharmacyOrderDetails(@Body request: PharmacyOrderRequest): ResponseGeneral<OrderDetailsResponse>

    //lab test

    @GET("v1/lab_test/categories/list")
    suspend fun getLabTestCategories(
        @Query("page") page: Int? = 0,
        @Query("city_id") cityId: Int? = 0,
    ): ResponseGeneral<PharmacyCategoriesResponse>

    @GET("v1/lab/list")
    suspend fun getLabList(
        @Query("page") page: Int? = 0,
        @Query("country_id") countryId: Int? = null,
        @Query("city_id") cityId: Int? = null,
        @Query("lab_test_category_id") categoryId: Int? = null,
        @Query("lab_test_id") labTestId: Int? = null,
    ): ResponseGeneral<LabTestCategoriesResponse>

    @GET("v1/lab/branch/list")
    suspend fun getLabBranchList(
        @Query("lab_id") labId: Int? = null,
        @Query("city_id") cityId: Int? = null,
    ): ResponseGeneral<LabBranchListResponse>

    @GET("v1/lab/cart/view")
    suspend fun getCartDetails(
        @Query("booking_id") bookingId: Int? = null,
    ): ResponseGeneral<OrderDetailsResponse>

    @POST("v1/lab/cart/add")
    suspend fun labTestAddToCart(@Body request: LabTestCartRequest): ResponseGeneral<PharmacyCartResponse>

    @POST("v1/lab/cart/delete")
    suspend fun labTestDeleteToCart(@Body request: LabTestCartRequest): ResponseGeneral<PharmacyCartResponse>

    @POST("v1/emr/lab_tests/list")
    suspend fun getLabTests(@Body pageRequest: LabTestFilterRequest): ResponseGeneral<LabTestListResponse>

    @POST("v1/lab/cart/home_collection")
    suspend fun updateHomeCollection(@Body request: LabTestHomeCollectionRequest): ResponseGeneral<OrderDetailsResponse>

    @POST("v1/notifications")
    suspend fun getNotification(@Body request: NotificationRequest): ResponseGeneral<NotificationResponse>

    @POST("v1/notification/read")
    suspend fun callReadNotif(@Body request: NotificationReadRequest): ResponseGeneral<Any>

    @POST("v1/notification/count")
    suspend fun getNotificationsCount(@Body request: NotificationReadRequest): ResponseGeneral<NotificationCountResponse>

    @POST("v1/notification/create")
    suspend fun callCreateNotif(@Body request: CreateNotifRequest): ResponseGeneral<Any>

    @POST("v1/partner/set/availability")
    suspend fun setPartnerAvailability(@Body request: PartnerAvailabilityRequest): ResponseGeneral<PartnerAvailabilityResponse>

    @POST("v1/partner/get/availability")
    suspend fun getPartnerAvailability(@Body request: PartnerAvailabilityRequest): ResponseGeneral<PartnerAvailabilityResponse>

    //claim

    @POST("v1/claim/services")
    suspend fun getClaimServices(): ResponseGeneral<ClaimServicesResponse>

    @POST("v1/claim/initial")
    suspend fun createClaimId(@Body request: ClaimRequest): ResponseGeneral<ClaimResponse>

    @POST("v1/claim/detail")
    suspend fun claimDetails(@Body request: ClaimRequest): ResponseGeneral<ClaimResponse>

    @POST("v1/claim/connections")
    suspend fun getClaimConnections(@Body request: ClaimConnectionExclusionRequest): ResponseGeneral<ClaimConnectionsResponse>

    @POST("v1/claim/review_exclusion")
    suspend fun getConnectionExclusions(@Body request: ClaimConnectionExclusionRequest): ResponseGeneral<ClaimConnectionExclusionResponse>

    @POST("v1/claim/connect")
    suspend fun connectClaim(@Body request: ClaimConnectRequest): ResponseGeneral<ClaimConnectResponse>

    @POST("v1/claim/store")
    suspend fun storeClaimRequest(@Body request: ClaimRequest): ResponseGeneral<ClaimConnectionsResponse>

    @POST("v1/claim/status")
    suspend fun cancelClaim(
        @Body request: ClaimStatusRequest
    ): ResponseGeneral<*>

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/claim/attachments/store")
    suspend fun addClaimAttachment(
        @Part("claim_id") claimId: Int?,
        @Part("claim_category_id") claimCategoryId: Int?,
        @Part("attachment_type") attachmentType: Int?,
        @Part("document_type") documentType: Int?,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<DeleteAttachmentRequest>

    @POST("v1/claim/attachments/delete")
    suspend fun deleteClaimAttachment(@Body request: DeleteAttachmentRequest): ResponseGeneral<*>

    @POST("v1/claim/attachments")
    suspend fun callGetClaimAttachments(@Body request: ClaimAttachmentRequest): ResponseGeneral<AttachmentResponse>

    //walkin

    // walk-in pharmacy

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/walk_in_pharmacy/attachments/store")
    suspend fun addWalkInAttachment(
        @Part("walk_in_pharmacy_id") walkInPharmacyId: Int?,
        @Part("pharmacy_id") pharmacyId: Int?,
        @Part("attachment_type") attachmentType: Int?,
        @Part("document_type") documentType: Int?,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<DeleteAttachmentRequest>

    @POST("v1/walk_in_pharmacy/attachments/delete")
    suspend fun deleteWalkInAttachment(@Body request: WalkInAttachmentRequest): ResponseGeneral<*>

    @POST("v1/walk_in_pharmacy/attachments")
    suspend fun callGetWalkInAttachments(@Body request: WalkInAttachmentRequest): ResponseGeneral<AttachmentResponse>

    @POST("v1/walk_in_pharmacy/detail")
    suspend fun walkInDetails(@Body request: WalkInRequest): ResponseGeneral<WalkInResponse>

    @POST("v1/walk_in_pharmacy/status")
    suspend fun walkInStatus(
        @Body request: ClaimStatusRequest
    ): ResponseGeneral<*>

    @POST("v1/walk_in_pharmacy/list")
    suspend fun getWalkInPharmacyList(@Body request: WalkInListRequest): ResponseGeneral<WalkInPharmacyListResponse>

    @POST("v1/walk_in_pharmacy/connections")
    suspend fun getWalkInPharmacyConnections(@Body request: WalkInConnectionRequest): ResponseGeneral<ClaimConnectionsResponse>

    @POST("v1/walk_in_pharmacy/initial")
    suspend fun initialWalkInPharmacy(@Body request: WalkInInitialRequest): ResponseGeneral<WalkInInitialResponse>

    @POST("v1/walk_in_pharmacy/store")
    suspend fun storeWalkInPharmacy(@Body request: WalkInStoreRequest): ResponseGeneral<WalkInStoreResponse>

    @POST("v1/walk_in_pharmacy/resend_confirmation")
    suspend fun resendWalkInConfirmation(@Body request: WalkInRequest): ResponseGeneral<*>

    @GET("v1/walk_in_pharmacy/service_types")
    suspend fun getWalkInPharmaServiceTypes(): ResponseGeneral<ServiceTypes>

    //walkin lab

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/walk_in_laboratory/attachments/store")
    suspend fun addWalkInLabAttachment(
        @Part("walk_in_laboratory_id") walkInLaboratoryId: Int?,
        @Part("lab_id") labId: Int?,
        @Part("attachment_type") attachmentType: Int?,
        @Part("document_type") documentType: Int?,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<DeleteAttachmentRequest>

    @POST("v1/walk_in_laboratory/attachments/delete")
    suspend fun deleteWalkInLabAttachment(@Body request: WalkInAttachmentRequest): ResponseGeneral<*>

    @POST("v1/walk_in_laboratory/attachments")
    suspend fun callGetWalkInLabAttachments(@Body request: WalkInAttachmentRequest): ResponseGeneral<AttachmentResponse>

    @POST("v1/walk_in_laboratory/detail")
    suspend fun walkInLabDetails(@Body request: WalkInRequest): ResponseGeneral<WalkInResponse>

    @POST("v1/walk_in_laboratory/status")
    suspend fun walkInLabStatus(
        @Body request: ClaimStatusRequest
    ): ResponseGeneral<*>

    @POST("v1/walk_in_laboratory/resend_confirmation")
    suspend fun resendWalkInLabConfirmation(@Body request: WalkInRequest): ResponseGeneral<*>

    @POST("v1/walk_in_laboratory/list")
    suspend fun getWalkInLaboratoryList(@Body request: WalkInListRequest): ResponseGeneral<WalkInPharmacyListResponse>

    @POST("v1/walk_in_laboratory/connections")
    suspend fun getWalkInLaboratoryConnections(@Body request: WalkInConnectionRequest): ResponseGeneral<ClaimConnectionsResponse>

    @POST("v1/walk_in_laboratory/initial")
    suspend fun initialWalkInLaboratory(@Body request: WalkInInitialRequest): ResponseGeneral<WalkInInitialResponse>

    @POST("v1/walk_in_laboratory/store")
    suspend fun storeWalkInLaboratory(@Body request: WalkInStoreRequest): ResponseGeneral<WalkInStoreResponse>

    @GET("v1/walk_in_laboratory/service_types")
    suspend fun getWalkInLabServiceTypes(): ResponseGeneral<ServiceTypes>

    //walkin hospital

    @JvmSuppressWildcards
    @Multipart
    @POST("v1/walk_in_hospital/attachments/store")
    suspend fun addWalkInHospitalAttachment(
        @Part("walk_in_hospital_id") walkInHospitalId: Int?,
        @Part("healthcare_id") healthcareId: Int?,
        @Part("attachment_type") attachmentType: Int?,
        @Part("document_type") documentType: Int?,
        @Part attachments: ArrayList<MultipartBody.Part>?
    ): ResponseGeneral<DeleteAttachmentRequest>

    @POST("v1/walk_in_hospital/attachments/delete")
    suspend fun deleteWalkInHospitalAttachment(@Body request: WalkInAttachmentRequest): ResponseGeneral<*>

    @POST("v1/walk_in_hospital/attachments")
    suspend fun callGetWalkInHospitalAttachments(@Body request: WalkInAttachmentRequest): ResponseGeneral<AttachmentResponse>

    @POST("v1/walk_in_hospital/detail")
    suspend fun walkInHospitalDetails(@Body request: WalkInRequest): ResponseGeneral<WalkInResponse>

    @POST("v1/walk_in_hospital/status")
    suspend fun walkInHospitalStatus(
        @Body request: ClaimStatusRequest
    ): ResponseGeneral<*>

    @POST("v1/walk_in_hospital/resend_confirmation")
    suspend fun resendWalkInHospitalConfirmation(@Body request: WalkInRequest): ResponseGeneral<*>

    @POST("v1/walk_in_hospital/list")
    suspend fun getWalkInHospitalList(@Body request: WalkInListRequest): ResponseGeneral<WalkInPharmacyListResponse>

    @POST("v1/walk_in_hospital/connections")
    suspend fun getWalkInHospitalConnections(@Body request: WalkInConnectionRequest): ResponseGeneral<ClaimConnectionsResponse>

    @POST("v1/walk_in_hospital/initial")
    suspend fun initialWalkInHospital(@Body request: WalkInInitialRequest): ResponseGeneral<WalkInInitialResponse>

    @POST("v1/walk_in_hospital/store")
    suspend fun storeWalkInHospital(@Body request: WalkInStoreRequest): ResponseGeneral<WalkInStoreResponse>

    @POST("v1/walk_in_hospital/services")
    suspend fun getWalkInHospitalServicesList(@Body request: WalkInInitialRequest): ResponseGeneral<WalkInServices>

    @GET("v1/walk_in_hospital/service_types")
    suspend fun getWalkInHospitalServiceTypes(): ResponseGeneral<ServiceTypes>

    @POST("v1/walk_in_pharmacy/discount")
    suspend fun getWalkInPharmacyDiscount(@Body request: WalkInListRequest): ResponseGeneral<WalkInDiscountsResponse>

    @POST("v1/walk_in_laboratory/discount")
    suspend fun getWalkInLaboratoryDiscount(@Body request: WalkInListRequest): ResponseGeneral<WalkInDiscountsResponse>

    @POST("v1/walk_in_hospital/discount")
    suspend fun getWalkInHospitalDiscount(@Body request: WalkInListRequest): ResponseGeneral<WalkInDiscountsResponse>

    @POST("v1/walk_in_pharmacy/qr_code")
    suspend fun scanPharmacyQRCode(@Body request: QRCodeRequest): ResponseGeneral<WalkInQRCodeResponse>

    @POST("v1/walk_in_laboratory/qr_code")
    suspend fun scanLaboratoryQRCode(@Body request: QRCodeRequest): ResponseGeneral<WalkInQRCodeResponse>

    @POST("v1/walk_in_hospital/qr_code")
    suspend fun scanHospitalQRCode(@Body request: QRCodeRequest): ResponseGeneral<WalkInQRCodeResponse>

    // Packages
    @POST("v1/packages/apply")
    suspend fun applyPackage(@Body request: AppointmentDetailReq): ResponseGeneral<*>

    @POST("v1/packages/remove")
    suspend fun removePackage(@Body request: AppointmentDetailReq): ResponseGeneral<*>

    // my connections
    @POST("v1/companies/services")
    suspend fun getCompaniesServices(@Body request: CompanyRequest): ResponseGeneral<CompanyServicesResponse>

    @POST("v1/companies/discounts")
    suspend fun getCompaniesDiscounts(@Body request: CompanyRequest): ResponseGeneral<CompanyDiscountsResponse>

    @POST("v1/companies/terms_and_conditions")
    suspend fun getCompaniesTermsAndConditions(@Body request: CompanyRequest): ResponseGeneral<CompanyTermsAndConditionsResponse>

    @POST("v1/companies/verify_email")
    suspend fun verifyCompaniesEmail(@Body request: EmailSendRequest): ResponseGeneral<VerifyEmailResponse>

    @POST("v1/companies/verification_code")
    suspend fun companiesVerificationCode(@Body request: EmailVerifyRequest): ResponseGeneral<VerifyEmailResponse>

    @POST("v1/walk_in_hospital/discount/visit")
    suspend fun getHospitalDiscountCenter(@Body request: HospitalDiscountCenterRequest): ResponseGeneral<HospitalDiscountCenter>

    @POST("v1/walk_in_laboratory/discount/visit")
    suspend fun getLabDiscountCenter(@Body request: HospitalDiscountCenterRequest): ResponseGeneral<HospitalDiscountCenter>
}
