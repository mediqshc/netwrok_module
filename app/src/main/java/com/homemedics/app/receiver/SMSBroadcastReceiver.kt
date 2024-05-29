package com.homemedics.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SMSBroadcastReceiver : BroadcastReceiver() {

    private var otpReceiver: OTPReceiveListener? = null

    fun initOTPListener(receiver: OTPReceiveListener) {
        this.otpReceiver = receiver
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            if (extras != null) {
                val status = extras.get(SmsRetriever.EXTRA_STATUS) as Status
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val message = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                        val pattern = Pattern.compile("\\d{6}")
                        val matcher = pattern.matcher(message)
                        if (matcher.find()) {
                            otpReceiver?.onOTPReceived(matcher.group(0))
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        otpReceiver?.onOTPTimeOut()
                    }
                }
            }
        }
    }
}

interface OTPReceiveListener {
    fun onOTPReceived(otp: String?)
    fun onOTPTimeOut()
}