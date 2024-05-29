package com.homemedics.app.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.fatron.network_module.models.request.fcm.UpdateFCMTokenRequest
import com.fatron.network_module.models.request.notification.CreateNotifRequest
import com.fatron.network_module.models.request.notification.PropertiesObj
import com.fatron.network_module.repository.ApiRepository
import com.fatron.network_module.utils.TinyDB
import com.google.android.datatransport.cct.internal.LogResponse.fromJson
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.homemedics.app.ApplicationClass
import com.homemedics.app.R
import com.homemedics.app.firebase.FirebaseRemoteConfigsWrapper
import com.homemedics.app.firebase.RemoteConfigKeys
import com.homemedics.app.firebase.RemoteConfigLanguage
import com.homemedics.app.locale.DefaultLocaleProvider
import com.homemedics.app.ui.activity.CallActivity
import com.homemedics.app.ui.activity.HomeActivity
import com.homemedics.app.ui.activity.SplashActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import kotlin.random.Random


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var callerName = ""
    private var mData: Map<String, String>? = null
    private lateinit var receiverEndRingingJob: Job

    init {
        val callActionsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                try {
                    if (intent?.action == actionCallAccepted) {
                        callAccepted = true
                        startRingingTimer()
                    }
                    else if(intent?.action == actionCallRejected) {
                        try {
                            receiverEndRingingJob.cancel()
                        }
                        catch (e: Exception){e.printStackTrace()}
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val filter = IntentFilter()
        filter.apply {
            addAction(actionCallAccepted)
            addAction(actionCallRejected)
        }
        LocalBroadcastManager.getInstance(getAppContext())
            .registerReceiver(callActionsReceiver, filter)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        if (isUserLoggedIn())
            sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Timber.e("sendRegistrationToServer")
        CoroutineScope(Dispatchers.IO).launch {
            token?.let {
                val request = UpdateFCMTokenRequest(
                    fcmToken = token,
                    deviceToken = getAndroidID(baseContext)
                )
                ApiRepository.updateFCMToken(request)
            }
        }
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"

        var callAccepted = false
        var noActionPerformed = true
        const val callNotificationId = 999
        const val twilioNotificationId = 999
        const val messageGroupNotificationId = 1000
        const val messageNotificationId = 1001
        const val callHoldCountDown = 1000L * 30
        const val defaultPNChannel = "MedIQ"
        const val callPNChannelId = "MedIQ Call"
        const val actionCallAccepted = "com.homemedics.app.CallAccepted"
        const val actionCallRejected = "com.homemedics.app.CallRejected"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        firebaseRemoteConfigsWrapper = FirebaseRemoteConfigsWrapper()
        fetchAndSetLanguage()

        Timber.e("invoked ${message.data}")
        mData = message.data

        if (isUserLoggedIn().not() && message.data["type_id"]?.toInt()
                .getSafe() != Enums.CallPNType.TYPE_CUSTOMER_REGISTRATION_COMPLETE.key
        )
            return

        try {
            if (message.data.containsKey("twi_body")) {
                if (ApplicationClass.twilioChatManager?.isNotificationShow.getSafe())
                    notificationTwilio(message.data)
            } else {
                val typeId = message.data["type_id"]?.toInt().getSafe()
                when (typeId) {
                    Enums.CallPNType.CALL.key -> {
                        if (callAccepted.not()) {
                            showHighPriorityNotification(message.data)
                            wakeupScreen()
                        }
                    }
                    Enums.CallPNType.PN_END_CALL.key -> {
                        endCall()
                    }
                    Enums.CallPNType.PN_REJECT_CALL.key -> {
                        endCall(isReject = true)
                    }
                    else ->
                        showHomeRedirectedNotification(message.data, typeId)


                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createNotification(request: CreateNotifRequest) = CoroutineScope(Dispatchers.IO).launch {
        ApiRepository.callCreateNotif(request)
    }


    private fun notificationTwilio(data: Map<String, String>) {
//        if (applicationContext.isAppIsInBackground()) {
//            val list = arrayListOf<NotificationsItem>()
//            var bodyText = ""
//            if (data.get("author")?.isNotEmpty().getSafe())
//                bodyText = getString(R.string.new_msg_received).replace(
//                    "[0]",
//                    data.get("author").getSafe()
//                )
//            list.add(
//                NotificationsItem(
//                    properties = PropertiesObj(sid = data.get("conversation_sid")),
//                    notifyUserId = DataCenter.getUser()?.id,
//                    body = bodyText,
//                    title = getString(R.string.new_msg),
//                    bookingId = "0",
//                    entityId = "0"
//                )
//            )
//            createNotification(CreateNotifRequest(notifications = list))
//        }
        val CHANNEL_ID = "channelID"
        val CHANNEL_NAME = "channelName"
        val NOTIF_ID = 0
        // Add as notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.BLUE
                enableLights(true)
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Constants.SID, data.get("conversation_sid").toString())
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(data.get("title"))
            .setContentText(data.get("twi_body"))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(data.get("twi_body"))
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.notification_icon)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifManger = NotificationManagerCompat.from(this)
        notifManger.notify(NOTIF_ID, notif)


    }

    private fun showHomeRedirectedNotification(
        data: Map<String, String>,
        typeId: Int,
        channelId: String = Enums.NotificationChannelIds.TYPE_DEFAULT.key
    ) {
        if (typeId == Enums.CallPNType.TYPE_MESSAGE_SESSION_EXPIRED.key && ApplicationClass.twilioChatManager?.isNotificationShow.getSafe()
                .not()
        ) {
            ApplicationClass.twilioChatManager?.sessionEnd?.value = true
        }
        var pendingIntent: PendingIntent? = null
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Enums.BundleKeys.pnNavType.key, typeId)
        intent.putExtra(Enums.BundleKeys.pnData.key, Gson().toJson(data))
        pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle(data["title"])
            .setContentText(data["body"])
            .setStyle(NotificationCompat.BigTextStyle().bigText(data["body"]))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifManger = NotificationManagerCompat.from(this)
        notifManger.notify(Random.nextInt(), notif)
    }

    private fun showNotification(
        notiId: Int = Random.nextInt(),
        title: String = "",
        message: String = "",
        channelId: String = Enums.NotificationChannelIds.TYPE_DEFAULT.key
    ) {
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(R.drawable.notification_icon)
            .setAutoCancel(true)
            .build()

        val notifManger = NotificationManagerCompat.from(this)
        notifManger.notify(notiId, notif)
    }

    private fun endCall(isReject: Boolean = false) {
        val intent = Intent(
            if(isReject)
                CallActivity.actionCallRejected
            else
                CallActivity.actionCallEnded
        )
        mData?.let { data ->
            if(data.containsKey("call_ended_by")){
                val callEndedBy = data["call_ended_by"]?.toInt()
                intent.putExtra(Enums.BundleKeys.pnCallEndedBy.key, callEndedBy)
            }
        }
        LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(callNotificationId)
    }

    private fun showHighPriorityNotification(data: Map<String, String>) {
        noActionPerformed = true

        val fullScreenIntent = Intent(this, CallActivity::class.java)
        fullScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0,
            getLoadedIntent(data), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder =
            NotificationCompat.Builder(this, callPNChannelId)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("MedIQ Call")
                .setContentText(data["clinical_team_name"])
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(fullScreenPendingIntent)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true)
                .setContent(getRemoteViews(data))
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val incomingCallNotification = notificationBuilder.build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(callNotificationId, incomingCallNotification)

        startRingingTimer()
    }

    private fun getRemoteViews(data: Map<String, String>): RemoteViews {
        val customButtonsView = RemoteViews(packageName, R.layout.view_call_notification_buttons)
        customButtonsView.setTextViewText(R.id.tvTitle, "MedIQ")
        customButtonsView.setTextViewText(R.id.tvDesc, "Incoming Call")

        //accept
        val acceptCallIntent = getLoadedIntent(data)
        acceptCallIntent.apply {
            putExtra("action", Enums.CallPNType.ACCEPT.key)
        }
        val acceptPendingIntent = PendingIntent.getActivity(
            this, 1,
            acceptCallIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        customButtonsView.setOnClickPendingIntent(R.id.bAccept, acceptPendingIntent)

        //reject
        val rejectCallIntent = getLoadedIntent(data)
        rejectCallIntent.apply {
            putExtra("action", Enums.CallPNType.REJECT.key)
        }
        val rejectPendingIntent = PendingIntent.getActivity(
            this, 2,
            rejectCallIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        customButtonsView.setOnClickPendingIntent(R.id.bReject, rejectPendingIntent)

        return customButtonsView
    }

    private fun getLoadedIntent(data: Map<String, String>): Intent {

        Timber.e("PN order: ${data["order"]}")

        val orderJson = JSONObject(data["order"])
        val orderId = orderJson.getInt("id")

        try {
            callerName = orderJson.getJSONObject("partner_user").getString("full_name")
            Timber.e("callerName: $callerName")
        }
        catch (e: Exception){e.printStackTrace()}

//        val order =
//            Gson().fromJson(orderJson.toString(), OrderResponse::class.java) as OrderResponse

        val fullScreenIntent = Intent(this, CallActivity::class.java)
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        fullScreenIntent.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Enums.BundleKeys.order.key, orderJson.toString())
            putExtra(Enums.BundleKeys.roomName.key, data["room_name"])
            putExtra(Enums.BundleKeys.token.key, data["token"])
            putExtra(Enums.BundleKeys.id.key, orderId)
            putExtra(Enums.BundleKeys.action.key, Enums.CallPNType.CALL.key)
            putExtra(Enums.BundleKeys.fromPush.key, true)
            putExtra(
                Enums.BundleKeys.isAppInBackground.key,
                applicationContext.isAppIsInBackground()
            )
        }
        return fullScreenIntent
    }

    private fun wakeupScreen() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isInteractive.not()) {
            val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK
                        or PowerManager.ACQUIRE_CAUSES_WAKEUP
                        or PowerManager.ON_AFTER_RELEASE,
                "mediq:notificationLock"
            )
            wl.acquire(10000);

            val wlCpu = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "mediq:myCpuLock"
            )
            wlCpu.acquire(10000);
        }
    }

    fun isUserLoggedIn(): Boolean =
        TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.TOKEN_USER.key)
            .isNotEmpty()

    private fun startRingingTimer() {
        receiverEndRingingJob = CoroutineScope(Dispatchers.IO).launch {
            delay(DataCenter.getMeta()?.callRingingTime.getSafe().toLong() * 1000)
            if (callAccepted.not()) {

                if(noActionPerformed){ //this is CR, will do it later
//                    showNotification(
//                        notiId = getNotificationIdFromString(callerName),
//                        title = getString(R.string.missed_call),
//                        message = getString(R.string.pn_m_missed_call).replace("[0]", callerName)
//                    )
                }

//                endCall()
                (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(callNotificationId)
                callAccepted = false
            }
        }
    }

    private fun getNotificationIdFromString(text: String): Int {
        val txt = if(text.length > 4) text.subSequence(0, 3) else text
        var idString = "9" //prefix in case of empty string
        txt.toString().toCharArray().forEach {
            idString += it.code
        }

        return idString.toInt()
    }

    // LANGUAGE SETTINGS
    private lateinit var firebaseRemoteConfigsWrapper: FirebaseRemoteConfigsWrapper
    private fun fetchAndSetLanguage() {
        val langString =if( TinyDB.instance.getString(com.fatron.network_module.utils.Enums.TinyDBKeys.LOCALE.key) == DefaultLocaleProvider.DEFAULT_LOCALE_UR)
            RemoteConfigKeys.URDU_LANGUAGE else
            RemoteConfigKeys.ENGLISH_LANGUAGE

        val language = getConfigurationData(
            firebaseRemoteConfigsWrapper.getConfigString(
                langString
            )
        )
        if (language != null)
            ApplicationClass.mGlobalData = language

    }

    private fun getConfigurationData(json: String): RemoteConfigLanguage? {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<RemoteConfigLanguage> =
            moshi.adapter(RemoteConfigLanguage::class.java)
        Log.e(SplashActivity::class.java.simpleName, "getConfigurationData: $json")
        return adapter.fromJson(json)
    }
}