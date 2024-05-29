package com.fatron.network_module.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.ngi.netwrok_module.BuildConfig
import com.fatron.network_module.NetworkModule
import com.fatron.network_module.utils.Enums
import com.fatron.network_module.utils.TinyDB
import com.fatron.newtork_module.Constants
import com.fatron.network_module.models.response.ResponseGeneral
import com.fatron.network_module.utils.getAndroidID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream

object RetrofitBuilder {

    private val retrofitHashMap = HashMap<String, RetrofitAPI>()


    fun getRetrofitInstance(url: Enums.RetrofitBaseUrl): RetrofitAPI {

        val baseUrl = url.baseUrl

        if (!retrofitHashMap.containsKey(baseUrl) ||
            retrofitHashMap[baseUrl] == null
        ) {

            synchronized(this) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .client(
                        getOkHttpClient(
                            NetworkModule.networkModule.context,
                            enableNetworkInterceptor(baseUrl)
                        )
                    )

                val restAPI = retrofit.build().create<RetrofitAPI>(RetrofitAPI::class.java)

                retrofitHashMap[baseUrl] = restAPI

                return restAPI
            }
        }

        return retrofitHashMap[baseUrl]!!
    }

    private fun enableNetworkInterceptor(baseUrl: String): Boolean {
        return baseUrl == Enums.RetrofitBaseUrl.BASE_URL.baseUrl
    }

    private fun getOkHttpClient(context: Context, isHostUrl: Boolean): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = Constants.LOG_LEVEL_API
        }

        val builder = OkHttpClient.Builder()

            .addInterceptor(chuckerInterceptor(context))
            .connectTimeout(Constants.API_CONNECT_TIMEOUT, TimeUnit.MINUTES)
            .readTimeout(Constants.API_READ_TIMEOUT, TimeUnit.MINUTES)
            .writeTimeout(Constants.API_WRITE_TIMEOUT, TimeUnit.MINUTES)

        if (isHostUrl)
            builder.addNetworkInterceptor(CustomNetworkInterceptor(context))

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(interceptor)
        }

        return builder.build()
    }

    private fun chuckerInterceptor(context: Context): ChuckerInterceptor {
        // Create the Collector
        val chuckerCollector = ChuckerCollector(
            context = context,
            // Toggles visibility of the push notification
            showNotification = true,
            // Allows to customize the retention period of collected data
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )

        // Create the Interceptor
        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            // The previously created Collector
            .collector(chuckerCollector)
            // The max body content length in bytes, after this responses will be truncated.
            .maxContentLength(250_000L)
            // List of headers to replace with ** in the Chucker UI
            .redactHeaders("Auth-Token", "Bearer")
            .redactHeaders("lang")
            // Read the whole response body even when the client does not consume the response completely.
            // This is useful in case of parsing errors or when the response body
            // is closed before being read like in Retrofit with Void and Unit types.
            .alwaysReadResponseBody(true)
            // Use decoder when processing request and response bodies. When multiple decoders are installed they
            // are applied in an order they were added.
            //.addBodyDecoder(decoder)
            // Controls Android shortcut creation. Available in SNAPSHOTS versions only at the moment

            .build()
        return chuckerInterceptor
    }


    private class CustomNetworkInterceptor(private val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

//            try {
            val original = chain.request()


            val tinyDB = TinyDB.instance


//            val token = Constants.AUTH_TOKEN

            val token = tinyDB.getString(
                Enums.TinyDBKeys.TOKEN_USER.key
            )

            val headerTag = original.header(RetrofitAPI.HEADER_TAG)

            if (BuildConfig.DEBUG) {
                Log.e("Layer", "Network Layer [ACCESS Token: $token \nUrl: ${original.url}]")

            }

            val builder = original.newBuilder()

            if (headerTag == null && !token.equals(
                    "",
                    ignoreCase = true
                )
            ) {
                builder.addHeader("Authorization", "Bearer $token")
            }

            val appVersion = NetworkModule.networkModule.appVersion
            if (appVersion.isNullOrBlank().not()) {
                appVersion?.let { builder.addHeader("x-app-version", it) }
            }
            val language = TinyDB.instance.getString(
                Enums.TinyDBKeys.LOCALE.key,
                Enums.LocalLanguage.EN.key
            )
            val tenantId = TinyDB.instance.getString(
                Enums.TinyDBKeys.TENANT_ID.key
            )

            val id = getAndroidID(context)

            val request = builder
                .addHeader("x-device-type", "Android")
                .addHeader("x-device-id", id)
                .addHeader("x-localization", language)
                .addHeader("x-tid", tenantId)
//                .
//                .addHeader("api-token", Constants.API_KEY)
                .removeHeader(RetrofitAPI.HEADER_TAG)
                .method(original.method, original.body)
                .build()


            val response = chain.proceed(request)

            if (response.isSuccessful) {

                try {
                    val bias = ByteArrayInputStream(response.peekBody(10000000).bytes())
                    val gzis = GZIPInputStream(bias)
                    val reader = InputStreamReader(gzis)
                    val inputStream = BufferedReader(reader)

                    val readed: String = inputStream.readLine()

                    println(readed)
                    val obj =
                        Gson().fromJson(
                            readed,
                            ResponseGeneral::class.java
                        )

                    if (obj.status == Constants.AUTH_CODE) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(
                            Intent().apply {
                                putExtra(Constants.KEY_API_ERROR_BROADCAST_CODE, obj.status)
                                putExtra(Constants.KEY_API_ERROR_BROADCAST_MESSAGE, obj.message)
                                action = Constants.BROADCAST_ACTION_API_FAILURE
                            }
                        )
                    } else if (obj.update_available == Constants.FORCE_UPDATE
                        || obj.update_available == Constants.SOFT_UPDATE
                    ) {

                        LocalBroadcastManager.getInstance(context).sendBroadcast(
                            Intent().apply {
                                putExtra(
                                    Constants.KEY_API_ERROR_BROADCAST_CODE,
                                    obj.update_available
                                )
                                putExtra(
                                    Constants.KEY_API_ERROR_UPDATED_VERSION_CODE,
                                    obj.androidVersion
                                )
                                putExtra(
                                    Constants.KEY_API_ERROR_BROADCAST_MESSAGE,
                                    obj.message ?: ""
                                )
                                action = Constants.BROADCAST_ACTION_API_FAILURE
                            }
                        )
    //                    return res
                    }

            } catch (e: Exception) {
                e.printStackTrace()
            }

            } else if (!response.isSuccessful) {

                if (response.code == Constants.AUTH_CODE) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(
                        Intent().apply {
                            putExtra(Constants.KEY_API_ERROR_BROADCAST_CODE, response.code)
                            putExtra(
                                Constants.KEY_API_ERROR_BROADCAST_MESSAGE,
                                response.message
                            )
                            action = Constants.BROADCAST_ACTION_API_FAILURE
                        }
                    )
                }


//                EventBus.getDefault().post(response)
            }

            if (BuildConfig.DEBUG) {
                CoroutineScope(Dispatchers.Main).launch {
//                    Toast.makeText(
//                        NetworkModule.networkModule.context,
//                        "${(response.receivedResponseAtMillis - response.sentRequestAtMillis)} ms",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }

            return response
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//
//            return getEmptyResponse()
        }
    }

    private fun getEmptyResponse(): Response {
        return Response.Builder()
            .request(
                Request.Builder()
                    .url("http://dummy/request")
                    .get()
                    .build()
            )
            .code(0)
            .protocol(Protocol.HTTP_1_1)
            .build()
    }

    fun destroyApiInterface() {
        retrofitHashMap.clear()
    }

}