package com.homemedics.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.fatron.network_module.NetworkModule
import com.fatron.network_module.utils.TinyDB
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ApplicationClass : Application() {


    override fun onCreate() {

        super.onCreate()
        mApplicationClass = this
        NetworkModule.networkModule.initialize(this.applicationContext, BuildConfig.VERSION_NAME)
        //val locale=  TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
       // if(locale.isEmpty())
           // TinyDB.instance.putString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key, DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)

//        Timber.plant(TimberLineNumberDebugTree("--- MedIQ ---"))
        //dev
        setTenant("dbafcdd7-308c-454c-ba79-7456ca3cba15")
        //stage
        //setTenant("3182b61c-965f-493f-8a26-003e32c28617")
//        firebaseRemoteConfigsWrapper = FirebaseRemoteConfigsWrapper()
//
//        firebaseRemoteConfigsWrapper.fetchConfigs().observeForever { it ->
//            Log.e(SplashActivity::class.java.simpleName, "firebase remote config data is ")
//            if(it.isSuccessful)  fetchAndSetLanguage()
//            else
//                firebaseRemoteConfigsWrapper.initialize().observeForever {
//                    if(it.isSuccessful)
//                        fetchAndSetLanguage()
//                }
//        }

    }

    private fun setExceptionHandler() {
//        if (!BuildConfig.DEBUG) {
      //  Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
//        }
    }

    companion object {

        @JvmStatic
        private lateinit var mApplicationClass: ApplicationClass

        @JvmStatic
        val application: ApplicationClass by lazy { mApplicationClass }

        fun getContext(): Context {
            return application.baseContext
        }



    }

    private fun setTenant(tenantId: String) {
        val tid =
            TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.TENANT_ID.key)
        if (tid.isEmpty())
            TinyDB.instance.putString(
                com.fatron.network_module.utils.Enums.TinyDBKeys.TENANT_ID.key,
                tenantId
            )
    }

}