package com.homemedics.app.utils

import android.content.Context
import android.content.Intent
import android.os.Looper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.homemedics.app.ui.activity.SplashActivity
import kotlin.system.exitProcess


class ExceptionHandler(var context: Context) : Thread.UncaughtExceptionHandler {


    override fun uncaughtException(p0: Thread?, p1: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(p1)
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        object : Thread() {
            override fun run() {
                Looper.prepare()
                Looper.loop()
            }
        }.start()

        try {
            Thread.sleep(2000) // Let the Toast display before app will get shutdown
        } catch (e: InterruptedException) {
            // Ignored.
        }

        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)


    }


}
