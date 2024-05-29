package com.fatron.network_module.utils

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

fun Context.T(msg: String) {
    GlobalScope.launch {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@T, msg, Toast.LENGTH_LONG).show()
        }
    }
}

fun getAndroidID(context: Context): String {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )
}

fun getAndroidVersion(context: Context): String? {

    try {
        val versionName: String = context.getPackageManager()
            .getPackageInfo(context.getPackageName(), 0).versionCode.toString()
        Log.d("APP_VERSION_CODE", versionName)
        return versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        return null
    }

}

fun getDateInFormat(time: String, currentFormat: String, format: String): String {
    try {
        val date = SimpleDateFormat(currentFormat).parse(time)

        val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
        return dateFormatter.format(date)

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

