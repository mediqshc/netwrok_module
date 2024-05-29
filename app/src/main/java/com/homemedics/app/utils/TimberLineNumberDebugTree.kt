package com.homemedics.app.utils

import timber.log.Timber

/**
 * Makes logged out class names clickable in Logcat
 */
class TimberLineNumberDebugTree(private val tag: String) : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement) =
        "$tag: (${element.fileName}:${element.lineNumber}) #${element.methodName} "
}
