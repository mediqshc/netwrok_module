package com.homemedics.app.locale

import android.content.res.Resources
import javax.inject.Inject

class LocaleFallbackChecker @Inject constructor() {
    var currentLocale: String = DefaultLocaleProvider.DEFAULT_LOCALE_EN
    var currentLanguage: String = DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN

    fun checkIsDeviceLocalEN() {
        if ( Resources.getSystem().configuration.locales[0].language.startsWith(
                DefaultLocaleProvider.DEFAULT_LOCALE_EN
            )
        ) {
            currentLocale = DefaultLocaleProvider.DEFAULT_LOCALE_EN
            currentLanguage = DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_EN
        }
    }

    fun checkIsDeviceLocalAR() {
        if (Resources.getSystem().configuration.locales[0].language.startsWith(
                DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_UR
            )
        ) {
            currentLocale = DefaultLocaleProvider.DEFAULT_LOCALE_UR
            currentLanguage = DefaultLocaleProvider.DEFAULT_LOCALE_LANGUAGE_UR
        }
    }
}