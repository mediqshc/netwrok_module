package com.homemedics.app.utils

import androidx.lifecycle.MutableLiveData
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R

object

Constants {

    val SID: String="sid"
    val BOOKINGID: String = "bookingId"
    val BOOKING_ID: String = "booking_id"
    val UUID: String = "uuid"
    val EMR_NUM: String = "emr_number"
    val EMR_TYPE: String = "emr_type"
    val EMR_ID: String = "emr_id"
    val EMR_SHARED: String = "emr_shared"
    val AUDIO_LENGTH: String = "audio_length"
    val SESSION_EXPIRE: String = "session_expire"
    val TITLE: String = "title"
    var ORDER_DETAIL_BOOKING_ID = "order_detail_booking_id"
    var PHARMACY_ORDER_DETAIL_BOOKING_ID = "pharmacy_order_detail_booking_id"
    var LAB_ORDER_DETAIL_BOOKING_ID = "lab_order_detail_booking_id"
    var HOSPITAL_ORDER_DETAIL_BOOKING_ID = "hospital_order_detail_booking_id"
    val KEY_API_ERROR_BROADCAST_CODE: Int = 405
    val BROADCAST_ACTION_API_FAILURE: Int = 400
    val AUTH_CODE: Int = 401
    val FORCE_UPDATE: Int = 1
    val FILE_SIZE: Int = 2000
    val FILE_SIZE_BOOKING: Int = 16000
    val SOFT_UPDATE: Int = 2
    val KEY_API_ERROR_UPDATED_VERSION_CODE: Int = 1
    val KEY_API_ERROR_BROADCAST_MESSAGE: String = "KEY_API_ERROR_BROADCAST_MESSAGE"
    val FCM_TOKEN: MutableLiveData<String> = MutableLiveData()
    val INTERNET_ERROR: String =
        ApplicationClass.application.applicationContext.getString(R.string.internet_error)
    var showUpdateDialog = true
    const val ZOOM_LEVEL = 18f
    const val ZOOM_LEVEL_WALK_IN = 12f
    const val placeApiHit = 3
    const val MEDIA_TYPE_IMAGE = "image/*"
    const val PROFILE_PIC = "profile_pic"
    const val OUTGOING = 0
    const val HEADER = -1
    const val SESSIONEND = -2
    const val INCOMING = 1
    const val OUTGOINGTEXT = 2
    const val OUTGOINGMEDIAIMAGE = 3
    const val OUTGOINGMEDIADOC = 4
    const val OUTGOINGEMR = 6
    const val OUTGOINGMEDIAAUDIO = 5
    const val INCOMINGTEXT =7
    const val INCOMINGMEDIAIMAGE =8
    const val INCOMINGMEDIAAUDIO =9
    const val INCOMINGMEDIADOC =10
    const val INCOMINGEMR =11
    const val READ =1
    const val DELIVERED =0


    const val UNDEFINED = 0
    const val SENDING = 1
    const val SENT = 2
    const val ERROR = 3

    const val START = "\u2066"
    const val END = "\u2069"
    const val HASH = "\u0023"
    const val PIPE = "\u007C"
    const val COLON = "\u003A"
    const val MULTIPLY = "\u0078"
    const val PLUS = "\u002B"
    const val MINUS = "\u002D"
    const val GREATER_THAN = "\u003E"
    const val PERCENTAGE = "\u0025"
    const val COMMA = "\u002C"
}