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
import com.google.firebase.FirebaseApp
import com.homemedics.app.firebase.FirebaseRemoteConfigsWrapper
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.locale.LocaleFallbackChecker
import com.homemedics.app.locale.LocaleManager
import com.homemedics.app.utils.*
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class ApplicationClass : Application() {

    private lateinit var firebaseRemoteConfigsWrapper: FirebaseRemoteConfigsWrapper
    override fun onCreate() {

        super.onCreate()
        mApplicationClass = this
        FirebaseApp.initializeApp(this)
        NetworkModule.networkModule.initialize(this.applicationContext, BuildConfig.VERSION_NAME)
        createNotificationChannels()
        createHighPriorityNotificationChannel()
//        setExceptionHandler()
        twilioChatManager = TwilioChatManager()
        localeManager = LocaleManager(this, LocaleFallbackChecker())
        val locale=  TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key)
        if(locale.isEmpty())
            TinyDB.instance.putString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key, DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN)

        Timber.plant(TimberLineNumberDebugTree("--- MedIQ ---"))
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
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
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

        var twilioChatManager: TwilioChatManager? = null
        lateinit var localeManager: LocaleManager
        var mGlobalData: RemoteConfigLanguage? = null

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

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

//            val channelDefault = Enums.NotificationChannelIds.TYPE_DEFAULT
//            val mChannel = NotificationChannel(
//                channelDefault.key,
//                channelDefault.title,
//                NotificationManager.IMPORTANCE_DEFAULT)
//            mChannel.description = channelDefault.desc
//            mNotificationManager.createNotificationChannel(mChannel)


            val channelList = arrayListOf<NotificationChannel>()
            Enums.NotificationChannelIds.values().forEach { item ->
                val mChannel = NotificationChannel(
                    item.key,
                    item.title,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                mChannel.description = item.desc

                channelList.add(mChannel)
            }

            mNotificationManager.createNotificationChannels(channelList)
        }
    }

    private fun createHighPriorityNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val descriptionText = getString(R.string.call_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(
                MyFirebaseMessagingService.callPNChannelId,
                MyFirebaseMessagingService.callPNChannelId,
                importance
            )

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .build()

            mChannel.setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
                audioAttributes
            )
            mChannel.description = descriptionText
            notificationManager.createNotificationChannel(mChannel)
        }
    }
}