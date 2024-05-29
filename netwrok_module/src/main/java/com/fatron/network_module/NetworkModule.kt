package com.fatron.network_module

import android.content.Context
import com.fatron.newtork_module.Constants

class NetworkModule private constructor() {

    companion object {
        val networkModule by lazy { NetworkModule() }
    }

    private lateinit var mBaseUrl: String
    private lateinit var mContext: Context
    private lateinit var mAppVersion: String

    val context: Context
        get() = mContext

    val baseUrl: String
        get() = mBaseUrl

    val appVersion: String
        get() = mAppVersion

    fun initialize(context: Context, appVersion: String, baseUrl: String = Constants.BASE_URL) {
        mContext = context
        mAppVersion = appVersion
        mBaseUrl = baseUrl
    }

}