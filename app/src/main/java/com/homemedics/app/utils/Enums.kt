package com.homemedics.app.utils

import com.homemedics.app.locale.DefaultLocaleProvider

object Enums {

    enum class BundleKeys(val key: String) {
        fromPush("fromPush"),
        action("action"),
        isAppInBackground("isAppInBackground"),
        id("id"),
        bookingId("bookingId"),
        partnerUserName("partnerUserName"),
        roomName("room_name"),
        token("token"),
        appointmentNo("appointmentNo"),
        partnerProfilePic("partnerProfilePic"),
        order("order"),
        partnerProfileResponse("partnerProfileResponse"),
        partnerSlotsResponse("partnerSlotsResponse"),
        bookConsultationRequest("bookConsultationRequest"),
        fromBDC("fromBDC"),
        fromChat("fromChat"),
        pnNavType("pnNavType"),
        pnData("pnData"),
        pnCallEndedBy("pnCallEndedBy"),
    }

    enum class LANGUAGES (val key:Int, val value: String){
        EN(0, DefaultLocaleProvider.DEFAULT_LOCALE_EN),
        UR(1,DefaultLocaleProvider.DEFAULT_LOCALE_UR)
    }

    enum class Profession(val key: Int, val value: String) {
        CUSTOMER(0, "customer"),
        DOCTOR(1, "doctor"),
        MEDICAL_STAFF(2, "medical_staff")
    }

    enum class ApplicationStatus(val key: Int) {
        CUSTOMER(0),
        UNDER_REVIEW(1),
        APPROVED(2),
        REJECTED(3),
        DISABLED(4)
    }


    enum class AttachmentType(val key: Int) {
        VOICE(1),
        IMAGE(2),
        DOC(3)
    }

    enum class PartnerCnic(val key: String) {
        FRONT("cnic_front"),
        BACK("cnic_back")
    }

    enum class LinkedAccountType(val key: Int) {
        TYPE_COMPANY(0),
        TYPE_HOSPITAL(1),
        TYPE_INSURANCE(2)
    }

    enum class MultipleViewItemType(val key: Int) {
        TYPE_NORMAL(0),
        TYPE_LARGE_ROUND_THUMB(1),
    }

    enum class NotificationChannelIds(val key: String, val title: String, val desc: String) {
        TYPE_DEFAULT("0", "Default", "Default notification"),
        TYPE_PN_CALL("1", "PN_CALL", "For incoming calls"),
        TYPE_PN_END_CALL("2", "PN_END_CALL", "Silent PN"),
        TYPE_LANGUAGE_SWITCH("3", "LANGUAGE_SWITCH", "When system language switched"),
        TYPE_USER_LOGOUT("4", "USER_LOGOUT", "When user logs out"),
        TYPE_BOOKING_CREATED("5", "BOOKING_CONFIRMATION", "Booking created by user"),
        TYPE_BOOKING_COMPLETED("6", "BOOKING_COMPLETED", "Booking completed by user"),
        TYPE_BOOKING_RESCHEDULED_PROCESSED(
            "7",
            "BOOKING_RESCHEDULED_PROCESSED",
            "Booking rescheduled by user or admin"
        ),
        TYPE_MESSAGE_SESSION_EXPIRED(
            "8",
            "MESSAGE_SESSION_EXPIRED",
            "Message session has been expired"
        ),
        TYPE_HOME_VISIT_UPDATE("9", "BOOKING_STARTED", "Task or order is now started"),
        TYPE_PARTNER_REQUEST_APPROVED("10", "PARTNER_REQUEST_APPROVED", "Request approved"),
        TYPE_MEDICAL_RECORD_SHARED(
            "11",
            "MEDICAL_RECORD_SHARED",
            "Someone shared a medical record"
        ),
        TYPE_EMR_SHARED_WITH_DOCTOR("12", "MESSAGE_SESSION_EXPIRED1", "MESSAGE_SESSION_EXPIRED"),
        TYPE_SMS_VERIFICATION("13", "MESSAGE_SESSION_EXPIRED2", "MESSAGE_SESSION_EXPIRED"),
        TYPE_FAMILY_MEMBER_INVITED("14", "MESSAGE_SESSION_EXPIRED3", "MESSAGE_SESSION_EXPIRED"),
        TYPE_CUSTOMER_REGISTRATION_COMPLETE("15", "MESSAGE_SESSION_EXPIRED4", "MESSAGE_SESSION_EXPIRED"),
        TYPE_EMPLOYEE_REGISTRATION_COMPLETE("16", "MESSAGE_SESSION_EXPIRED5", "MESSAGE_SESSION_EXPIRED"),
        TYPE_PAYMENT_RECEIVED("17", "MESSAGE_SESSION_EXPIRED6", "MESSAGE_SESSION_EXPIRED"),
        TYPE_BOOKING_REMINDER("18", "MESSAGE_SESSION_EXPIRED7", "MESSAGE_SESSION_EXPIRED"),
        TYPE_BOOKING_RESCHEDULED_REQUEST("19", "BOOKING_RESCHEDULED_REQUEST", "BOOKING_RESCHEDULED_REQUEST"),
        TYPE_BOOKING_RESCHEDULED_ACCEPTED("20", "MESSAGE_SESSION_EXPIRED", "MESSAGE_SESSION_EXPIRED"),
        TYPE_CHAT_MESSAGE("21", "MESSAGE_SESSION_EXPIRED", "MESSAGE_SESSION_EXPIRED"),
        TYPE_MESSAGE_SESSION_START("22", "MESSAGE_SESSION_STARTED", "MESSAGE_SESSION_STARTED"),
        TYPE_PARTNER_REQUEST_REJECTED("23", "PARTNER_REQUEST_REJECTED", "PARTNER_REQUEST_REJECTED"),
        TYPE_BOOKING_CANCELLED("24", "BOOKING_CANCELLED", "BOOKING_CANCELLED"),
        TYPE_DELIVERY_COMPLETED("25", "DELIVERY_COMPLETED", "DELIVERY_COMPLETED"),
        TYPE_FAMILY_MEMBER_ADDED("26", "FAMILY_MEMBER_ADDED", "FAMILY_MEMBER_ADDED"),
        TYPE_PARTNER_REQUEST_PROCESSED("27", "PARTNER_REQUEST_PROCESSED", "PARTNER_REQUEST_PROCESSED"),
        TYPE_FAMILY_MEMBER_LINKED("28", "FAMILY_MEMBER_LINKED", "FAMILY_MEMBER_LINKED"),
        TYPE_EMR_REPORT_UPLOAD("29", "EMR_REPORT_UPLOAD", "EMR_REPORT_UPLOAD"),
        TYPE_EMPLOYEE_LINKED("30", "TYPE_EMPLOYEE_LINKED", "TYPE_EMPLOYEE_LINKED"),
        TYPE_CUSTOMER_LINKED("31", "TYPE_CUSTOMER_LINKED", "TYPE_CUSTOMER_LINKED"),
        TYPE_BOOKING_RESCHEDULED_ACCEPTED_BY_CUSTOMER("32", "TYPE_BOOKING_RESCHEDULED_ACCEPTED_BY_CUSTOMER", "TYPE_BOOKING_RESCHEDULED_ACCEPTED_BY_CUSTOMER"),
        TYPE_PARTNER_BOOKING_REMINDER("33", "TYPE_PARTNER_BOOKING_REMINDER", "TYPE_PARTNER_BOOKING_REMINDER"),
        TYPE_HOME_VISIT_BOOKING_CONFIRMATION("34", "TYPE_HOME_VISIT_BOOKING_CONFIRMATION", "TYPE_HOME_VISIT_BOOKING_CONFIRMATION"),
        TYPE_HOME_HEALTHCARE_PAYMENT_CONFIRMATION("35", "TYPE_HOME_HEALTHCARE_PAYMENT_CONFIRMATION", "TYPE_HOME_HEALTHCARE_PAYMENT_CONFIRMATION"),
        TYPE_WALK_IN_TRANSACTION("36", "TYPE_WALK_IN_TRANSACTION", "TYPE_WALK_IN_TRANSACTION"),
        TYPE_WALK_IN_APPROVED("37", "TYPE_WALK_IN_APPROVED", "TYPE_WALK_IN_APPROVED"),
        TYPE_WALK_IN_REJECTED("38", "TYPE_WALK_IN_REJECTED", "TYPE_WALK_IN_REJECTED"),
        TYPE_WALK_IN_DOCUMENT("39", "TYPE_WALK_IN_DOCUMENT", "TYPE_WALK_IN_DOCUMENT"),
        TYPE_CLAIM_TRANSACTION("40", "TYPE_CLAIM_TRANSACTION", "TYPE_CLAIM_TRANSACTION"),
        TYPE_CLAIM_DOCUMENT("41", "TYPE_CLAIM_DOCUMENT", "TYPE_CLAIM_DOCUMENT"),
        TYPE_CLAIM_APPROVED("42", "TYPE_CLAIM_APPROVED", "TYPE_CLAIM_APPROVED"),
        TYPE_CLAIM_REJECTED("43", "TYPE_CLAIM_REJECTED", "TYPE_CLAIM_REJECTED"),
        TYPE_CLAIM_SETTLEMENT_ON_HOLD("44", "TYPE_CLAIM_SETTLEMENT_ON_HOLD", "TYPE_CLAIM_SETTLEMENT_ON_HOLD"),
        TYPE_CLAIM_SETTLED("45", "TYPE_CLAIM_SETTLED", "TYPE_CLAIM_SETTLED"),
        TYPE_WALK_IN_REQUEST("46", "TYPE_WALK_IN_REQUEST", "TYPE_WALK_IN_REQUEST"),
        WALK_IN_REQUEST_CONFIRMED("47", "WALK_IN_REQUEST_CONFIRMED", "WALK_IN_REQUEST_CONFIRMED"),
        WALK_IN_CANCELLED_CUSTOMER("48", "WALK_IN_CANCELLED_CUSTOMER", "WALK_IN_CANCELLED_CUSTOMER"),
        WALK_IN_CANCELLED_ADMIN("49", "WALK_IN_REQUEST_CONFIRMED", "WALK_IN_REQUEST_CONFIRMED"),
        CLAIM_CANCELLED_USER("50", "WALK_IN_CANCELLED_USER", "WALK_IN_CANCELLED_USER")
    }

    enum class PlannerMode(val key: String) {
        PLANNER_MODE("planner_mode")
    }

    enum class FirstTimeUnique(val key: String) {
        FIRST_TIME_UNIQUE("first_time_unique")
    }

    enum class AppointmentType(val key: Int, val value: String) {
        UPCOMING(0, "Upcoming"),
        UNREAD(1, "Unread"),
        HISTORY(2, "History")
    }

    enum class AppointmentStatusType(val key: Int, val value: String, val label: String) {
        APPROVALPENDING(1, "approval_pending", "Approval Pending"),
        CONFIRM(2, "confirmed", "Confirmed"),
        REJECT(3, "rejected", "Rejected"),
        COMPLETE(4, "completed", "Completed"),
        RESCHEDULED(5, "reschedule", "Rescheduled"),
        START(6, "started", "Confirmed"),
        CANCEL(7, "canceled", "Cancelled"),
        CONFIRMATIONPENDING(8, "confirmation_pending", "Confirmation Pending"),
        REVIEWPENDING(9, "review_pending", "Review Pending"),
        RESCHEDULING(10, "rescheduling", "Rescheduling"),
        SAMPLE_COLLECTED(11, "sample_collected", "Sample Collected")
    }

    enum class DutyStatusType(val key: Int, val value: String, val label: String) {
        PENDING(1, "pending", "Pending"),
        COMPLETED(2, "completed", "Completed"),
        STARTED(3, "started", "Pending"),
        CANCELLED(4, "cancelled", "Cancelled")
    }

    enum class PartnerType(val key: Int, val value: String) {
        DOCTOR(1, "Doctor"),
        MEDICAL_STAFF(2, "Medical Staff")
    }

    enum class OrdersType(val key: Int, val value: String) {
        CURRENT(1, "current"),
        HISTORY(2, "history")
    }

    enum class CallPNType(val key: Int) {
        CALL(1),
        PN_END_CALL(2),
        PN_REJECT_CALL(3),
        TYPE_USER_LOGOUT(4),
        TYPE_BOOKING_CREATED(5),
        TYPE_BOOKING_COMPLETED(6),
        TYPE_BOOKING_RESCHEDULED_PROCESSED(7),
        TYPE_MESSAGE_SESSION_EXPIRED(8),
        TYPE_HOME_VISIT_UPDATE(9),
        TYPE_PARTNER_REQUEST_APPROVED(10),
        TYPE_MEDICAL_RECORD_SHARED(11),
        TYPE_EMR_SHARED_WITH_DOCTOR(12),
        TYPE_SMS_VERIFICATION(13),
        TYPE_FAMILY_MEMBER_INVITED(14),
        TYPE_CUSTOMER_REGISTRATION_COMPLETE(15),
        TYPE_EMPLOYEE_REGISTRATION_COMPLETE(16),
        TYPE_PAYMENT_RECEIVED(17),
        TYPE_BOOKING_REMINDER(18),
        TYPE_BOOKING_RESCHEDULED_REQUEST(19),
        BOOKING_RESCHEDULED_ACCEPTED(20),
        TYPE_CHAT_MESSAGE(21),
        TYPE_MESSAGE_SESSION_START(22),
        TYPE_PARTNER_REQUEST_REJECTED(23),
        TYPE_BOOKING_CANCELLED(24),
        TYPE_DELIVERY_COMPLETED(25),
        TYPE_FAMILY_MEMBER_ADDED(26),
        TYPE_PARTNER_REQUEST_PROCESSED(27),
        TYPE_FAMILY_MEMBER_LINKED(28),
        TYPE_EMR_REPORT_UPLOAD(29),
        TYPE_EMPLOYEE_LINKED(30),
        TYPE_CUSTOMER_LINKED(31),
        TYPE_BOOKING_RESCHEDULED_ACCEPTED_BY_CUSTOMER(32),
        TYPE_PARTNER_BOOKING_REMINDER(33),
        TYPE_HOME_VISIT_BOOKING_CONFIRMATION(34),
        TYPE_HOME_HEALTHCARE_PAYMENT_CONFIRMATION(35),
        TYPE_WALK_IN_TRANSACTION(36),
        TYPE_WALK_IN_APPROVED(37),
        TYPE_WALK_IN_REJECTED(38),
        TYPE_WALK_IN_DOCUMENT(39),
        TYPE_CLAIM_TRANSACTION(40),
        TYPE_CLAIM_DOCUMENT(41),
        TYPE_CLAIM_APPROVED(42),
        TYPE_CLAIM_REJECTED(43),
        TYPE_CLAIM_SETTLEMENT_ON_HOLD(44),
        TYPE_CLAIM_SETTLED(45),
        TYPE_WALK_IN_REQUEST(46),
        WALK_IN_REQUEST_CONFIRMED(47),
        WALK_IN_CANCELLED_CUSTOMER(48),
        WALK_IN_CANCELLED_ADMIN(49),
        WALK_IN_CANCELLED_USER(50),
        ACCEPT(101),
        REJECT(102)
    }


    enum class ConversationType(val key: Int, val value: String) {
        CHATS(1, "chats"),
        HISTORY(2, "history")
    }

    enum class EMRType(val key: Int) {
        CONSULTATION(1),
        REPORTS(2),
        MEDICATION(3),
        VITALS(4)
    }

    enum class EMRTypesMeta(val key: Int) {
        SYMPTOMS(1),
        DIAGNOSIS(2),
        LAB_TEST(3),
        MEDICAL_HEALTHCARE(4)
    }

    enum class DosageType(val key: Int) {
        DAILY(1),
        HOURLY(2),
    }

    enum class EMRVitalsUnits(val key: Int, val value: String, val label: String) {
        HEART_RATE(1, "bmp", "heart_rate"),
        TEMPERATURE(2, "F", "temperature"),
        SYSTOLIC_DIASTOLIC(3, "mmHg", "systolic_bp"),
        DIASTOLIC(3, "mmHg", "diastolic_bp"),
        OXYGEN_LEVEL(4, "%Oximeter", "oxygen_level"),
        BLOOD_SUGAR(5, "mg/dl", "blood_sugar_level")
    }

    enum class EMRAttachmentsType(val key: Int, val value: String) {
        DIAGNOSIS(1, "diagnosis"),
        PRESCRIPTION(2, "prescription"),
    }

    enum class HomeHealthcareVisitType(val key: Int) {
        SINGLE_VISIT(1),
        MULTIPLE_VISIT(2),
    }

    enum class SessionStatuses(val key: Int, val value: String, val label: String) {
        INITIATED(0, "initiated", "Start"),
        STARTED(1, "started", "Send a message"),
        ENDED(2, "ended", "Session ended")
    }

    enum class PaymentMethod(val id: Int) {
        COD(1),
        JAZZ_CASH(2),
        DEBIT_CREDIT_CARD(4),
    }

    enum class ClaimWalkInStatus(val id: Int) {
        APPROVAL_PENDING(1),
        REJECTED(3),
        CANCELLED(7),
        COMPLETED(4),
        UNDER_REVIEW(13),
        ON_HOLD(14),
        SETTLEMENT_IN_PROGRESS(15),
        SETTLEMENT_ON_HOLD(16),
        SETTLED(17),
        PACKAGE_SELECTION_PENDING(18),
        UNAUTHORISED(19),
    }
}