package com.fatron.newtork_module

import okhttp3.logging.HttpLoggingInterceptor

object Constants {


    

    val AUTH_CODE: Int = 401
    val SLOT_ALREADY_BOOKED_CODE: Int = 409
    val SOFT_UPDATE: Int = 1
    val FORCE_UPDATE: Int = 2
    const val BROADCAST_ACTION_API_FAILURE: String = "broadcast_action_api_failure"
    const val KEY_API_ERROR_BROADCAST_CODE: String = "api_broadcast_error_key_code"
    const val KEY_API_ERROR_UPDATED_VERSION_CODE: String = "api_broadcast_updated_version_code"
    const val KEY_API_ERROR_BROADCAST_MESSAGE: String = "api_broadcast_error_key_msg"

//    const val BASE_URL = "https://mediq-dev-api.herokuapp.com/api/"
    const val BASE_URL = "https://dev-apis.mediq.com.pk/api/"
    const val BASE_URL_LOC = "https://geolocation-db.com/"
//    const val BASE_URL = "http://b338-137-59-226-237.ngrok.io/api/v1/"
    var AUTH_TOKEN = "dafdd778c4af4751c19cbbcef767db04"

    const val API_KEY = "6xeBlmRD8HXEsm2y5uqTU9gNpbFfbbfdVj6nnwF9ZW5fFJ9otMXBezcCU18Ttesw"

    const val BASE_URL_MAP = "https://maps.googleapis.com/maps/api/directions/"

    val LOG_LEVEL_API = HttpLoggingInterceptor.Level.BODY

    const val API_CONNECT_TIMEOUT: Long = 10

    const val API_READ_TIMEOUT: Long = 10

    const val API_WRITE_TIMEOUT: Long = 10
}
