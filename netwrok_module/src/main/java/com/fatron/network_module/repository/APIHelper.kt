package com.fatron.network_module.repository

import android.util.Log
import com.fatron.network_module.NetworkModule
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.fatron.network_module.models.response.ResponseGeneral
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

sealed class ResponseResult<out H> {
    data class Success<out T>(val data: T) : ResponseResult<T>()

    data class Failure(val error: Throwable) : ResponseResult<Nothing>()

    data class ApiError(val generalResponse: ResponseGeneral<Any>) : ResponseResult<Nothing>()

    object Pending : ResponseResult<Nothing>()
    object Loader : ResponseResult<Nothing>()
    object NOT_FOUND : ResponseResult<Nothing>()
    object Complete : ResponseResult<Nothing>()
}

val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

suspend inline fun <T> safeApiCall(
    root: Boolean, type: Type, crossinline body: suspend () -> T
): ResponseResult<*> {
    try {
        // blocking block
        val response = withContext(Dispatchers.IO) {
            body()
        }

        val parse = response as ResponseGeneral<*>

        Log.e("APIHelper", parse.toString())

        if (parse.status != 200) {
            try {
                ResponseResult.ApiError(parse as ResponseGeneral<Any>)
            } catch (exception: Exception) {
                exception.printStackTrace()
                ResponseGeneral(
                    "API PARSING ERROR",
                    600,
                    "",
                    listOf("Something went wrong"),
                    0, 0, 0,
                    "Something went wrong",

                    )
            }
        }

        return ResponseResult.Success(response)

    } catch (e: Exception) {
        Log.e("APIHelper", "Exception: ${e.localizedMessage}")
        e.printStackTrace()
        when (e) {
            is HttpException -> {

                try {
                    return if (e.code() == 500) {
                        ResponseResult.Failure(Throwable("Something went wrong"))
                    } else {
                        val data =
                            errorResponseParse(e.response()?.errorBody()?.string().toString())
                        if (data?.errors.isNullOrEmpty().not()) {
                            data?.message = data?.errors?.get(0)
                        }
                        ResponseResult.ApiError((data ?: e.response()) as ResponseGeneral<Any>)
                    }

                } catch (exception: Exception) {
                    exception.printStackTrace()
                    ResponseGeneral(
                        "API PARSING ERROR",
                        600,
                        { },
                        listOf("Something went wrong"),
                        0, 0, 0,
                        "Something went wrong"
                    )
                }
            }
            is SocketTimeoutException -> {
                try {
                    return ResponseResult.Failure(Throwable("Please check your internet connection and try again."))
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    ResponseGeneral(
                        "API PARSING ERROR",
                        600,
                        { },
                        listOf("Please check your internet connection and try again."),
                        0, 0, 0,
                        "Please check your internet connection and try again."
                    )
                }
            }
            is UnknownHostException, is ConnectException -> {
                return ResponseResult.Failure(Throwable("Please check your internet connection and try again."))
            }
            is SSLHandshakeException -> {
//                NetworkModule.networkModule.a♦
                return ResponseResult.Failure(Throwable("Your phone date is inaccurate! Adjust your clock and try again."))
            }
        }
        return ResponseResult.Failure(e)
    }
}

fun errorResponseParse(responseString: String): ResponseGeneral<Any>? {
    return try {
        GsonBuilder().create().fromJson(responseString)
    } catch (exception: Exception) {
//        exception.printStackTrace()
        ResponseGeneral(
            "API PARSING ERROR",
            600,
            {},
            listOf("Something went wrong"),
            0,
            0,
            0,
            "Something went wrong"
        )
    }
}

inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

