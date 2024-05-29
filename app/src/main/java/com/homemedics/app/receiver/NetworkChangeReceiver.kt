package com.homemedics.app.receiver

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.utils.DialogUtils
import com.homemedics.app.utils.getSafe
import com.homemedics.app.utils.getString

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (isOnline(context).not()) {
            DialogUtils(context as Activity)
                .showSingleButtonAlertDialog(
                    title = ApplicationClass.mGlobalData?.errorMessages?.internetError.getSafe(),
                    message = ApplicationClass.mGlobalData?.errorMessages?.internetErrorMsg.getSafe(),
                    buttonCallback = {}
                )
        }
    }

    fun isOnline(context: Context?): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        return (ni != null && ni.isConnected)
    }
}