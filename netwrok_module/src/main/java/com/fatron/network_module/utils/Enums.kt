package com.fatron.network_module.utils

import com.fatron.newtork_module.Constants

object Enums {
    enum class RetrofitBaseUrl(val baseUrl: String) {
        BASE_URL(Constants.BASE_URL),
        BASE_URL_MAP(Constants.BASE_URL_MAP),
        BASE_URL_LOC(Constants.BASE_URL_LOC)
    }

    enum class TinyDBKeys(val key: String) {
        LOCALE("locale"),
        TOKEN_USER("auth_token_user"),
        FCM_TOKEN("fcm_token"),
        USER("user"),
        CHATTOKEN("token"),
        NAME("name"),
        STRIPE_KEY("STRIPE_KEY"),
        META("meta"),
        SAVED_PAYMENT_METHOD("SAVED_PAYMENT_METHOD"),
        REMOTE_CONFIG_FETCHED("REMOTE_CONFIG_SET"),
        TENANT_ID("TenantId"),
        BOOKING_ID("bookingId"),
        LAB_TEST_BOOKING_ID("labTestBookingId"),
        REQUEST("request")
    }

    enum class LocalLanguage(val key: String){
        EN("en"),
        UR("ur"),
    }
}