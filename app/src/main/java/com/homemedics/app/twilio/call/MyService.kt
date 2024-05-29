package com.homemedics.app.twilio.call

import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.Service
import android.os.Build
import android.media.AudioAttributes
import com.homemedics.app.R
import android.graphics.BitmapFactory
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.homemedics.app.ui.activity.CallActivity
import java.lang.UnsupportedOperationException

class MyService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notifyIntent = Intent(this, CallActivity::class.java)
        //notifyIntent.putExtra("isCallGoing",true)
        //notifyIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)

        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
            PendingIntent.FLAG_MUTABLE,
        )

        val nm = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel: NotificationChannel? = null
            val att = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            channel = NotificationChannel("222", "my_channel", NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }
        var builder: NotificationCompat.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = NotificationCompat.Builder(
                applicationContext, "222"
            )
                .setContentTitle(getString(R.string.notification_title))
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(applicationContext, R.color.activated))
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher) //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.electro))
                .setContentText(getString(R.string.notification_text))
                    .setWhen(System.currentTimeMillis())  // the time stamp, you will probably use System.currentTimeMillis() for most scenarios
                    .setUsesChronometer(true)
            //.setContentIntent(pi);
        }
        builder?.priority = NotificationCompat.PRIORITY_HIGH
        nm.notify(123, builder?.build())
        builder?.apply {
            setContentIntent(notifyPendingIntent)
        }
        builder?.setOngoing(true)
        startForeground(123, builder?.build())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        startService(restartServiceIntent)
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(restartServiceIntent);
//        } else {
//            startService(restartServiceIntent);
//        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()
        //startForeground(1,new Notification());
    }
}