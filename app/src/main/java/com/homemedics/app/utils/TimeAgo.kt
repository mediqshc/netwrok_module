package com.homemedics.app.utils

import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.firebase.ChatScreen
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil

object TimeAgo {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getTimeAgo(millis: Long, dateFormat: String): String? {
        var time = millis
        if (time < 1000000000000L) {
            time *= 1000
        }

        val date = getDateInFormat(millis, "yyyy-MM-dd")
        val now = System.currentTimeMillis()
        val currentDate = getDateInFormat(now, "yyyy-MM-dd")

        if (time > now || time <= 0) {
            return null
        }
        val diff = now - time
        val cmp = date.compareTo(currentDate)

        return if ( cmp==0) {
          ApplicationClass.mGlobalData?.chatScreen?.today
        }
//        if (diff < MINUTE_MILLIS) {
////            "Just now"
//            getString(R.string.today)
//        } else if (diff < 2 * MINUTE_MILLIS) {
////            "a minute ago"
//            getString(R.string.today)
//        } else if (diff < 50 * MINUTE_MILLIS) {
////            "${diff / MINUTE_MILLIS} minutes ago"
//            getString(R.string.today)
//        } else if (diff < 90 * MINUTE_MILLIS) {
////            "an hour ago"
//            getString(R.string.today)
//        } else if (diff < 24 * HOUR_MILLIS) {
////            "${diff / HOUR_MILLIS} hours ago"
//            getString(R.string.today)
//        }
//        else if (diff < 48 * HOUR_MILLIS) {
        else if (diff < 48 * HOUR_MILLIS) {
//            "yesterday"
            getString(R.string.yesterday)
        } else {
//            "${diff / DAY_MILLIS} days ago"
            SimpleDateFormat(dateFormat, Locale.getDefault()).format(Date(millis))
        }
    }

    fun timeLeft(timeLeft:String?,timeLeftString:ChatScreen?):String?{
     return  if((  timeLeft ?: "0").toLong() < "60".toLong())
            timeLeftString?.minLeft?.replace("[#]", timeLeft.getSafe())
        else if((timeLeft ?: "0").toLong() >="60".toLong() && (timeLeft ?: "0") <"120")
            timeLeftString?.hourLeft?.replace("[#]",
                ceil (( timeLeft ?:  "0").toDouble() /  60).toInt() .toString()
            )
        else  timeLeftString?.hoursLeft?.replace("[#]", ceil((timeLeft ?:  "0").toDouble() / 60).toInt().toString())

    }
}