package com.homemedics.app.locale

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import timber.log.Timber
import java.util.*


class LocaleManager(
    private var context: Context,
    private val localeFallbackChecker: LocaleFallbackChecker
) {

    private var locale: Locale = Locale(localeFallbackChecker.currentLanguage)


    fun updateContext(context: Context) {
        this.context = context
    }

    fun getDeviceLocale(): String {
        localeFallbackChecker.checkIsDeviceLocalEN()
        localeFallbackChecker.checkIsDeviceLocalAR()
        return localeFallbackChecker.currentLocale
    }

    fun getCurrentLocale(locale: String): String {
        return if (!DefaultLocaleProvider.DEFAULT_LOCALE_UR.equals(locale)) {
            DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
        } else
            DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_UR
    }


    @Suppress("DEPRECATION")
    fun updateLanguage(languageString: String) {
        locale = Locale(languageString.split("-")[0])
        Locale.setDefault(locale)
        val res: Resources = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        context.createConfigurationContext(config)
        updateResourcesLocale(context, locale)
        updateResourcesLocaleLegacy(context, locale)
    }
//
//    fun updateBaseContext(context: Context): Context {
//        Locale.setDefault(locale)
//
//        return
//    }

    private fun updateResourcesLocaleLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    private fun updateResourcesLocale(context: Context, locale: Locale): Context? {
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    fun updateLocaleData(context: Context, lang: String) {
        updateContext(context)
        Timber.e("lang $lang")
        updateLanguage(lang)
    }
}