@file:Suppress("UNCHECKED_CAST")

package com.fatron.network_module.repository

import android.util.Log
import com.fatron.network_module.models.request.AppointmentStatusRequest
import com.fatron.network_module.models.request.Surgery.SurgeryRequest
import com.fatron.network_module.models.request.activedays.ActiveDaysRequest
import com.fatron.network_module.models.request.aiquestion.GetQuestionsRequest
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
import com.fatron.network_module.models.request.emr.FaceScannerEMRVitalsRequest.VitalSignsRequest
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
import com.fatron.network_module.models.request.labtest.LabBranchListRequest
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
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.models.response.Scribe.SoapNotesRequest
import com.fatron.network_module.models.response.Surgery.SurgeryBooking
import com.fatron.network_module.models.response.aiPrescription.ORCRequest
import com.fatron.network_module.models.response.pharmacy.HospitalDiscountCenterRequest
import com.fatron.network_module.models.response.planschedule.deleteSlotRequest
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Part

object ApiRepository {

    var api = RetrofitBuilder.getRetrofitInstance(Enums.RetrofitBaseUrl.BASE_URL)
    private val googleApi = RetrofitBuilder.getRetrofitInstance(Enums.RetrofitBaseUrl.BASE_URL_MAP)
    private val locApi = RetrofitBuilder.getRetrofitInstance(Enums.RetrofitBaseUrl.BASE_URL_LOC)

    suspend fun login(request: LoginRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.login(request)
        }

    suspend fun logout(request: LogoutRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.logout(
                deviceToken = request.deviceToken
            )
        }

    suspend fun deleteAccount(request: DeleteAccountRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteAccount(request)
        }

    suspend fun verifyPhoneNum(request: LoginRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.verifyPhoneNum(request)
        }

    suspend fun createProfile(userRequest: UserRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.createProfile(userRequest)
        }

    suspend fun register(signupRequest: SignupRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.register(signupRequest)
        }

    suspend fun metaDataCall() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.metaData(TinyDB.instance.getString(Enums.TinyDBKeys.TENANT_ID.key))
        }

    suspend fun getProfileCall() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getProfile()
        }

    suspend fun sendOtp(otpRequest: OtpRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.sendOtp(otpRequest)
        }

//    suspend fun sendEconOtp(otpRequest: EconOtpRequest) =
//        safeApiCall(false, ResponseGeneral::class.java) {
//            api.sendEconOtp(otpRequest)
//        }
//
//    suspend fun subscribeEconPackage(subscribeRequest: EconSubscribeRequest) =
//        safeApiCall(false, ResponseGeneral::class.java) {
//            api.subscribe(subscribeRequest)
//        }
//
//    suspend fun unSubscribeEconPackage(unSubscribeRequest: EconOtpRequest) =
//        safeApiCall(false, ResponseGeneral::class.java) {
//            api.unsubscribe(unSubscribeRequest)
//        }
//
//    suspend fun verifyEconOtp(request: EconPackageSubscribeRequest) =
//        safeApiCall(false, ResponseGeneral::class.java) {
//            api.verifyEconOtp(request)
//        }

    suspend fun verifyOtp(verifyOtpRequest: ForgetPwdRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.verifyOtp(verifyOtpRequest)
        }

    suspend fun changePwd(verifyOtpRequest: ForgetPwdRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.forgetPassword(verifyOtpRequest)
        }

    suspend fun forgetPassword(forgetPwdRequest: ForgetPwdRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.forgetPassword(forgetPwdRequest)
        }

    suspend fun getCooperateCompList() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCooperateCompList()
        }

    //company
    suspend fun linkCompany(request: LinkCompanyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.linkCompany(request)
        }

    suspend fun deleteCompanyLink(request: DeleteLinkRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteCompanyLink(request)
        }

    suspend fun getInsuranceCompList() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getInsuranceCompList()
        }

    //insurances
    suspend fun linkInsuranceComp(request: LinkInsuranceRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.linkInsuranceComp(request)
        }

    suspend fun deleteInsuranceComp(request: DeleteLinkRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteInsuranceComp(request)
        }

    suspend fun getHospitalList() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getHospitalsList()
        }

    //hospitals
    suspend fun linkHospital(request: LinkCompanyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.linkHospital(request)
        }

    suspend fun deleteHospital(request: DeleteLinkRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteHospital(request)
        }

    suspend fun getLinkedAccounts(request: CompanyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLinkedAccounts(request)
        }

    suspend fun getFamilyConnections(emr: Boolean? = null) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getFamilyConnectionList(emr)
        }

    suspend fun createFamilyConnection(request: FamilyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.createFamilyConnection(request)
        }

    suspend fun deleteFamilyConnection(request: FamilyConnectionActionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteFamilyConnection(request)
        }

    suspend fun connectFamilyConnection(request: FamilyConnectionActionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.connectFamilyConnection(request)
        }

    suspend fun storeUserProfilePio(@Part profile_pic: MultipartBody.Part?) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeUserProfilePio(profile_pic)
        }

    suspend fun storeEducation(
        @Part document: ArrayList<MultipartBody.Part>?,
        educationRequest: EducationRequest
    ) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeEducation(
                educationRequest.degree.toRequestBody(),
                educationRequest.school.toRequestBody(),
                educationRequest.countryId.toString().toRequestBody(),
                educationRequest.year.toRequestBody(),
                document
            )
        }

    suspend fun callAddWorkExpApi(request: WorkExperience) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callAddWorkExpApi(request)
        }

    suspend fun calldeletePartnerWork(request: deletePartnerWork) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deletePartnerWork(request)
        }

    suspend fun calldeleteEducApi(request: deletePartnerEducation) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.calldeleteEducApi(request)
        }

    suspend fun calldeleteSpeciality(request: deletePartnerSpecialityReq) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.calldeleteSpeciality(request)
        }

    suspend fun storePartnerCnic(
        @Part cnic_front: MultipartBody.Part?,
        @Part cnic_back: MultipartBody.Part?,
        update: String = "false"
    ) = safeApiCall(false, ResponseGeneral::class.java) {
        api.storePartnerCnic(cnic_front, cnic_back, update.toRequestBody())
    }

    suspend fun storePartnerDetails(partnerDetailsRequest: PartnerDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storePartnerDetails(partnerDetailsRequest)
        }

    suspend fun getPartnerDetails() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPartnerDetails()
        }

    suspend fun storeSpecialities(specialitiesRequest: SpecialitiesRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storePartnerSpecialities(specialitiesRequest)
        }

    suspend fun storePartnerService(serviceRequest: ServiceRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storePartnerService(serviceRequest)
        }

    suspend fun sendEmail(request: EmailSendRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.sendEmail(request)
        }

    suspend fun verifyEmail(request: EmailVerifyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.verifyEmail(request)
        }


    suspend fun getSlotsApiCall() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getSlotsApiCall()
        }

    suspend fun addSlotsApiCall(request: AddSlotRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addSlotsApiCall(request)
        }

    suspend fun deleteSlotsApiCall(request: deleteSlotRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteSlotsApiCall(request)
        }

    suspend fun getActiveDays() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getActiveDays()
        }

    suspend fun addActiveDays(activeDaysRequest: ActiveDaysRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addActiveDays(activeDaysRequest)
        }

    suspend fun addOffDates(offDatesRequest: OffDatesRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addOffDates(offDatesRequest)
        }

    suspend fun deleteOffDates(deleteOffDatesRequest: DeleteOffDatesRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteOffDates(deleteOffDatesRequest)
        }

    suspend fun getOffDates() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getOffDates()
        }


    suspend fun getHealthCard() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getHealthCard()
        }

    suspend fun getDoctors(request: BDCFilterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getDoctors(request)
        }

    suspend fun getPartnerDetails(request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPartnerDetails(request)
        }

    suspend fun getPartnerAbout(request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPartnerAbout(request)
        }

    suspend fun getPartnerReviews(request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPartnerReviews(request)
        }

    suspend fun bookConsultation(request: BookConsultationRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.bookConsultation(request)
        }

    suspend fun deleteDocumentApiCall(request: DeleteAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteDocumentApiCall(request)
        }

    suspend fun getSlots(request: com.fatron.network_module.models.request.bdc.PartnerDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getSlots(request)
        }

    suspend fun addConsultationAttachment(
        booking_id: Int?,
        attachment_type: String,
        document: ArrayList<MultipartBody.Part>?
    ) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addConsultationAttachment(
                booking_id,
                attachment_type.toRequestBody(),
                document
            )
        }

    suspend fun saveAddressCall(request: UserLocation) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.saveAddressCall(request)
        }

    // appointments

    suspend fun getAppointments(request: AppointmentListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getAppointments(request)
        }

    suspend fun getAppointmentsServices() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getAppointmentsServices()
        }

    suspend fun appointmentsReschedule(request: AppointmentsActionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.appointmentsReschedule(request)
        }

    suspend fun appointmentsCompleted(request: AppointmentsActionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.appointmentsCompleted(request)
        }

    suspend fun appointmentsReject(request: AppointmentsActionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.appointmentsReject(request)
        }

    suspend fun appointmentsAccept(request: AppointmentsActionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.appointmentsAccept(request)
        }

    suspend fun getApptDetail(appointmentDetailReq: AppointmentDetailReq) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getApptDetail(appointmentDetailReq)
        }

    suspend fun getAttachments(appointmentDetailReq: AppointmentDetailReq) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getAttachments(appointmentDetailReq)
        }

    suspend fun callChangeStatus(appointmentStatusRequest: AppointmentStatusRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callChangeStatus(appointmentStatusRequest)
        }

    suspend fun checkoutDetails(request: AppointmentDetailReq) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.checkoutDetails(request)
        }

    suspend fun applyPackage(request: AppointmentDetailReq) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.applyPackage(request)
        }

    suspend fun removePackage(request: AppointmentDetailReq) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.removePackage(request)
        }

    suspend fun getOrders(request: OrdersRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getOrders(request)
        }

    suspend fun orderDetails(request: TokenRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.orderDetails(request)
        }

    suspend fun addReview(request: MyOrdersRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addReview(request)
        }

    suspend fun cancelOrder(request: TokenRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.cancelOrder(request)
        }

    suspend fun rescheduleOrder(request: RescheduleRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.rescheduleOrder(request)
        }

    suspend fun getHHCDuties(request: ScheduledVisitsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getHHCDuties(request)
        }

    suspend fun getTwilioVideoCallToken(tokenRequest: TokenRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getTwilioVideoCallToken(tokenRequest)
        }

    suspend fun addParticipantsToVideoCall(requestParticipantsRequest: ParticipantsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addParticipantsToVideoCall(requestParticipantsRequest)
        }

    suspend fun destroyRoom(requestParticipantsRequest: ParticipantsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.destroyRoom(requestParticipantsRequest)
        }

    suspend fun isRoomExist(requestRoomRequest: RoomRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.isRoomExist(requestRoomRequest)
        }

    suspend fun updateFCMToken(fcmToken: UpdateFCMTokenRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.updateFCMToken(fcmToken)
        }

    //home service

    suspend fun getHomeServiceList(request: HomeServiceListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getHomeServiceList(request)
        }

    suspend fun getHomeServiceDetails(request: HomeServiceDetailRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getHomeServiceDetails(request)
        }

    suspend fun callApiBookService(request: HomeServiceStoreRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callApiBookService(request)
        }

    suspend fun emrCreate(symptomsRequest: SymptomsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.emrCreate(symptomsRequest)
        }

    suspend fun getSymptoms(pageRequest: PageRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getSymptoms(pageRequest)
        }

    suspend fun storeEMRType(storeEMRTypeRequest: StoreEMRTypeRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeEMRType(storeEMRTypeRequest)
        }

    suspend fun emrDetails(emrDetailsRequest: EMRDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.emrDetails(emrDetailsRequest)
        }

    suspend fun emrDrafts(request: SymptomsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.emrDrafts(request)
        }

    suspend fun deleteEMRType(request: EMRTypeDeleteRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteEMRType(request)
        }

    suspend fun editEMRType(storeEMRTypeRequest: StoreEMRTypeRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.editEMRType(storeEMRTypeRequest)
        }

    suspend fun storeEMR(storeEMRRequest: StoreEMRRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeEMR(storeEMRRequest)
        }

    suspend fun getLabTests(pageRequest: PageRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLabTests(pageRequest)
        }

    suspend fun getMedicalHealthCares(pageRequest: PageRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getMedicalHealthCares(pageRequest)
        }

    suspend fun getDiagnosisList(pageRequest: PageRequest) =
        safeApiCall(false, ResponseResult::class.java) {
            api.getDiagnosisList(pageRequest)
        }

    //medicine

    suspend fun getMedicineList(pageRequest: PageRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getMedicineList(pageRequest)
        }

    suspend fun storeMedicine(request: StoreMedicineRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeMedicine(request)
        }

    suspend fun editMedicine(request: StoreMedicineRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.editMedicine(request)
        }

    suspend fun deleteMedicine(request: MedicineDeleteRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteMedicine(request)
        }

    suspend fun addEMRAttachment(
        emr_id: Int,
        attachment_type: String,
        emr_type: Int,
        document: ArrayList<MultipartBody.Part>?
    ) = safeApiCall(false, ResponseGeneral::class.java) {
        api.addEMRAttachment(
            emr_id,
            attachment_type.toRequestBody(),
            emr_type,
            document
        )
    }

    suspend fun deleteEMRDocument(request: DeleteAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteEMRDocument(request)
        }

    //customer EMR
    //consultation
    suspend fun getCustomerConsultationRecords(request: EMRConsultationFilterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCustomerConsultationRecords(request)
        }

    suspend fun getCustomerConsultationRecordDetails(request: EMRDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCustomerConsultationRecordDetails(request)
        }

    suspend fun customerEMRRecordShare(request: EMRShareWithRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.customerEMRRecordShare(request)
        }

    //records
    suspend fun addCustomerMedicineRecord(request: StoreMedicineRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addCustomerMedicineRecord(request)
        }

    suspend fun getCustomerEMRRecords(request: EMRRecordsFilterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCustomerEMRRecords(request)
        }

    suspend fun getCustomerEMRRecordsDetails(request: EMRDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCustomerEMRRecordsDetails(request)
        }

    suspend fun addCustomerEMRAttachment(
        emr_id: Int,
        emr_customer_type: Int?,
        emr_type_id: Int?,
        attachment_type: String,
        document: ArrayList<MultipartBody.Part>?
    ) = safeApiCall(false, ResponseGeneral::class.java) {
        api.addCustomerEMRAttachment(
            emr_id,
            emr_customer_type,
            emr_type_id,
            attachment_type.toRequestBody(),
            document
        )
    }

    suspend fun deleteCustomerEMRAttachment(request: DeleteAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteCustomerEMRAttachment(request)
        }

    suspend fun deleteCustomerEMRRecordType(request: EMRTypeDeleteRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteCustomerEMRRecordType(request)
        }

    suspend fun saveCustomerEMRRecord(request: EMRDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.saveCustomerEMRRecord(request)
        }

    suspend fun deleteCustomerEMRRecord(request: EMRDetailsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteCustomerEMRRecord(request)
        }

    suspend fun getReferredList() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getReferredList()
        }

    suspend fun attachEMRtoBDC(request: AttachEMRtoBDCRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.attachEMRtoBDC(request)
        }

    suspend fun detachEMRtoBDC(request: AttachEMRtoBDCRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.detachEMRtoBDC(request)
        }

    suspend fun downloadEMR(request: EMRDownloadRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.downloadEMR(request)
        }

    //chat
    suspend fun getPartnersList(conversationRequest: ConversationListRequest, url: String) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPartnersList(conversationRequest, url)
        }

    suspend fun getTwilioChatToken(twilioTokenRequest: TwilioTokenRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getTwilioChatToken(twilioTokenRequest)
        }

    suspend fun callChatSession(conversationSessionRequest: ConversationSessionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getTwilioSessionStart(conversationSessionRequest)
        }

    suspend fun callChatDetail(chatDetailRequest: chatDetailRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callChatDetail(chatDetailRequest)
        }

    suspend fun callSendEmrApi(sendEmrRequest: SendEmrRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callSendEmrApi(sendEmrRequest)
        }

    //paymob

    suspend fun getPaymobToken() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPaymobToken()
        }

    suspend fun paymobOrders(request: PaymobPaymentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.paymobOrders(request)
        }

    suspend fun paymobPayment(request: PaymobPaymentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.paymobPayment(request)
        }

    suspend fun paymobPaymentStatus(request: PaymobPaymentStatusRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.paymobPaymentStatus(request)
        }

    suspend fun getPharmacyCategories(request: PharmacyCategoriesRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPharmacyCategories(request)
        }

    suspend fun getPharmacyProductsList(request: PharmacyProductRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPharmacyProductsList(request)
        }

    suspend fun pharmacyProductDetails(request: PharmacyProductDetailRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.pharmacyProductDetails(request)
        }

    suspend fun pharmaAddToCart(request: PharmacyCartRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.pharmaAddToCart(request)
        }

    suspend fun pharmaClearCart(request: PharmacyOrderRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.pharmaClearCart(request)
        }

    suspend fun pharmacyQuantityUpdate(request: PharmacyCartRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.pharmacyQuantityUpdate(request)
        }

    suspend fun pharmacyOrderDetails(request: PharmacyOrderRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.pharmacyOrderDetails(request)
        }

    //lab tests

    suspend fun getLabTestCategories(request: PageRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLabTestCategories(request.page, request.cityId)
        }

    suspend fun getLabList(request: LabTestFilterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLabList(
                page = request.page,
                countryId = request.countryId,
                cityId = request.cityId,
                labTestId = request.labTestId
            )
        }

    suspend fun getLabBranchList(request: LabBranchListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLabBranchList(
                request.labId,
                request.cityId,
            )
        }

    suspend fun getCartDetails(request: LabTestCartRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCartDetails(request.bookingId)
        }

    suspend fun labTestAddToCart(request: LabTestCartRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.labTestAddToCart(request)
        }

    suspend fun labTestDeleteToCart(request: LabTestCartRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.labTestDeleteToCart(request)
        }

    suspend fun getLabTests(request: LabTestFilterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLabTests(request)
        }

    suspend fun updateHomeCollection(request: LabTestHomeCollectionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.updateHomeCollection(request)
        }

    //get notification
    suspend fun getNotification(request: NotificationRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getNotification(request)
        }

    //call read notification
    suspend fun callReadNotif(request: NotificationReadRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callReadNotif(request)
        }

    suspend fun callCreateNotif(request: CreateNotifRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            Log.e("api noti", "log")
            api.callCreateNotif(request)
        }

    suspend fun getNotificationsCount(request: NotificationReadRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getNotificationsCount(request)
        }

    suspend fun getPartnerAvailability(request: PartnerAvailabilityRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPartnerAvailability(request)
        }

    suspend fun setPartnerAvailability(request: PartnerAvailabilityRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.setPartnerAvailability(request)
        }

    //claim

    suspend fun submitPriorAuthorizationForm(request: PriorAuthorizationRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.submitPriorAuthorizationForm(request)
        }

    suspend fun priorAuthorizationFormInit() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.priorAuthInit()
        }

    suspend fun getQuestions(request: GetQuestionsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getQuestions(request)
        }

    suspend fun getAISummary() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getAISummary()
        }

    suspend fun getBotChat(request: GetQuestionsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getBotChat(request)
        }


    suspend fun postAudioQuestionAttachment( voiceParts: MultipartBody.Part) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.postAudioQuestionAttachment(
                voiceAnswer = voiceParts
            )
        }

    suspend fun getPriorAuthorizationForm(request: ListPriorAuthorizationsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPriorAuthorizationForm(request)
        }

    suspend fun getSurgery() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getSurgeryCatalogs()
        }
    suspend fun getSurgeons(catalod_id: SurgeryRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getSurgeons(request = catalod_id)
        }

    suspend fun createRequest(catalod_id: SurgeryBooking) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.submitRequest(request = catalod_id)
        }

    suspend fun getConsultationHistory() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getConsultationHistory()
        }

    suspend fun getSoapNotes(request: SoapNotesRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getSoapNotes(request)
        }

    suspend fun getPriorAuthorizationDetails(request: ListPriorAuthorizationsRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPriorAuthorizationDetails(request)
        }

    suspend fun getClaimServices(request: PageRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getClaimServices()
        }

    suspend fun createClaimId(request: ClaimRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.createClaimId(request)
        }

    suspend fun claimDetails(request: ClaimRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.claimDetails(request)
        }

    suspend fun getClaimConnections(request: ClaimConnectionExclusionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getClaimConnections(request)
        }

    suspend fun getConnectionExclusions(request: ClaimConnectionExclusionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getConnectionExclusions(request)
        }

    suspend fun connectClaim(request: ClaimConnectRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.connectClaim(request)
        }

    suspend fun storeClaimRequest(request: ClaimRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeClaimRequest(request)
        }

    suspend fun cancelClaim(request: ClaimStatusRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.cancelClaim(request)
        }

    suspend fun addClaimAttachment(request: AddClaimAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addClaimAttachment(
                claimId = request.claimId,
                claimCategoryId = request.claimCategoryId,
                attachmentType = request.attachmentType,
                documentType = request.documentType,
                attachments = request.attachments,
            )
        }

    suspend fun deleteClaimAttachment(request: DeleteAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteClaimAttachment(request)
        }

    suspend fun callGetAttachments(request: ClaimAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callGetClaimAttachments(request)
        }

    suspend fun getClaims(request: OrdersRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getOrders(request)
        }

    //prior auth

    suspend fun callGetPriorAuthAttachments(priorAuthorizationId: Int) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callGetPriorAuthAttachments(priorAuthorizationId)
        }

    suspend fun deletePriorAuthAttachment(priorAuthorizationAttachmentId: Int) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deletePriorAuthAttachment(priorAuthorizationAttachmentId)
        }

    suspend fun addPriorAuthAttachment(request: AddPriorAuthAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addPriorAuthAttachment(
                priorAuthorizationId = request.priorAuthorizationId,
                attachmentType = request.attachmentType,
                documentType = request.documentType,
                attachments = request.attachments,
            )
        }


    //walkin

    suspend fun addWalkInAttachment(request: AddWalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addWalkInAttachment(
                walkInPharmacyId = request.walkInPharmacyId,
                pharmacyId = request.pharmacyId,
                attachmentType = request.attachmentType,
                documentType = request.documentType,
                attachments = request.attachments,
            )
        }

    suspend fun deleteWalkInAttachment(request: WalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteWalkInAttachment(request)
        }

    suspend fun callWalkInGetAttachments(request: WalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callGetWalkInAttachments(request)
        }

    suspend fun walkInDetails(request: WalkInRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.walkInDetails(request)
        }

    suspend fun walkInStatus(request: ClaimStatusRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.walkInStatus(request)
        }

    // Walk-in pharmacy

    suspend fun getWalkInPharmacyList(request: WalkInListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInPharmacyList(request)
        }

    suspend fun getWalkInPharmacyConnections(request: WalkInConnectionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInPharmacyConnections(request)
        }

    suspend fun getORC(request: ORCRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getOCR(request)
        }

    suspend fun initialWalkInPharmacy(request: WalkInInitialRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.initialWalkInPharmacy(request)
        }

    suspend fun storeWalkInPharmacy(request: WalkInStoreRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeWalkInPharmacy(request)
        }

    suspend fun resendWalkInConfirmation(request: WalkInRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.resendWalkInConfirmation(request)
        }

    suspend fun getWalkInPharmaServiceTypes() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInPharmaServiceTypes()
        }

    //walkin lab

    suspend fun addWalkInLabAttachment(request: AddWalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addWalkInLabAttachment(
                walkInLaboratoryId = request.walkInLaboratoryId,
                labId = request.labId,
                attachmentType = request.attachmentType,
                documentType = request.documentType,
                attachments = request.attachments,
            )
        }

    suspend fun deleteWalkInLabAttachment(request: WalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteWalkInLabAttachment(request)
        }

    suspend fun callGetWalkInLabAttachments(request: WalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callGetWalkInLabAttachments(request)
        }

    suspend fun walkInLabDetails(request: WalkInRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.walkInLabDetails(request)
        }

    suspend fun walkInLabStatus(request: ClaimStatusRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.walkInLabStatus(request)
        }

    suspend fun resendWalkInLabConfirmation(request: WalkInRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.resendWalkInLabConfirmation(request)
        }

    suspend fun getWalkInLaboratoryList(request: WalkInListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInLaboratoryList(request)
        }

    suspend fun getWalkInLaboratoryConnections(request: WalkInConnectionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInLaboratoryConnections(request)
        }

    suspend fun initialWalkInLaboratory(request: WalkInInitialRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.initialWalkInLaboratory(request)
        }

    suspend fun storeWalkInLaboratory(request: WalkInStoreRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeWalkInLaboratory(request)
        }

    suspend fun getWalkInLabServiceTypes() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInLabServiceTypes()
        }

    //walkin hospital

    suspend fun addWalkInHospitalAttachment(request: AddWalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.addWalkInHospitalAttachment(
                walkInHospitalId = request.walkInHospitalId,
                healthcareId = request.healthcareId,
                attachmentType = request.attachmentType,
                documentType = request.documentType,
                attachments = request.attachments,
            )
        }

    suspend fun deleteWalkInHospitalAttachment(request: WalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.deleteWalkInHospitalAttachment(request)
        }

    suspend fun callGetWalkInHospitalAttachments(request: WalkInAttachmentRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.callGetWalkInHospitalAttachments(request)
        }

    suspend fun walkInHospitalDetails(request: WalkInRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.walkInHospitalDetails(request)
        }

    suspend fun walkInHospitalStatus(request: ClaimStatusRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.walkInHospitalStatus(request)
        }

    suspend fun resendWalkInHospitalConfirmation(request: WalkInRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.resendWalkInHospitalConfirmation(request)
        }

    suspend fun getWalkInHospitalList(request: WalkInListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInHospitalList(request)
        }

    suspend fun getWalkInHospitalConnections(request: WalkInConnectionRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInHospitalConnections(request)
        }

    suspend fun initialWalkInHospital(request: WalkInInitialRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.initialWalkInHospital(request)
        }

    suspend fun storeWalkInHospital(request: WalkInStoreRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.storeWalkInHospital(request)
        }

    suspend fun getWalkInHospitalServicesList(request: WalkInInitialRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInHospitalServicesList(request)
        }

    suspend fun getWalkInHospitalServiceTypes() =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInHospitalServiceTypes()
        }

    suspend fun getWalkInPharmacyDiscount(request: WalkInListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInPharmacyDiscount(request)
        }

    suspend fun getWalkInLaboratoryDiscount(request: WalkInListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInLaboratoryDiscount(request)
        }

    suspend fun getWalkInHospitalDiscount(request: WalkInListRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getWalkInHospitalDiscount(request)
        }

    suspend fun scanPharmacyQRCode(request: QRCodeRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.scanPharmacyQRCode(request)
        }

    suspend fun scanLaboratoryQRCode(request: QRCodeRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.scanLaboratoryQRCode(request)
        }

    suspend fun scanHospitalQRCode(request: QRCodeRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.scanHospitalQRCode(request)
        }

    suspend fun getCompaniesServices(request: CompanyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCompaniesServices(request)
        }

    suspend fun getCompaniesDiscounts(request: CompanyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCompaniesDiscounts(request)
        }

    suspend fun getCompaniesTermsAndConditions(request: CompanyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getCompaniesTermsAndConditions(request)
        }

    suspend fun verifyCompaniesEmail(request: EmailSendRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.verifyCompaniesEmail(request)
        }

    suspend fun companiesVerificationCode(request: EmailVerifyRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.companiesVerificationCode(request)
        }


    suspend fun HospitalDiscountCenter(request: HospitalDiscountCenterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getHospitalDiscountCenter(request)
        }

    suspend fun LabDiscountCenter(request: HospitalDiscountCenterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getLabDiscountCenter(request)
        }

    suspend fun PharmacyDiscountCenter(request: HospitalDiscountCenterRequest) =
        safeApiCall(false, ResponseGeneral::class.java) {
            api.getPharmacyDiscountCenter(request)
        }

//    suspend fun getPackages(phone: String? = null) =
//        safeApiCall(false, ResponseGeneral::class.java) {
//            api.getPackages(phone)
//        }
}
