package com.kgeun.themeparkers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import com.kgeun.themeparkers.util.registerNotificationChannel
import com.kgeun.themeparkers.view.TPAlarmActivity


class AlarmService : Service() {
//    val CHANNEL_NAME = "알람 설정"

    companion object {
        var isRunning = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val CHANNEL_ID = "${packageName}_CHANNEL"

        val oIntent = Intent(this, TPMainActivity::class.java)
        oIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            oIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        isRunning = true
        TPPrefCtl.setServiceRunning(true)
        registerNotificationChannel(this)
        val mBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.smallicon)
                .setContentTitle("공연시간 알람 대기 중")
                .setContentIntent(contentIntent)

        startForeground(System.currentTimeMillis().toInt(), mBuilder.build())

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        TPPrefCtl.setServiceRunning(false)
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        isRunning = false
        TPPrefCtl.setServiceRunning(false)
        super.onTaskRemoved(rootIntent)
    }

//
//    fun initializeNotification() {
//        val builder = NotificationCompat.Builder(this, "1")
//        builder.setSmallIcon(R.mipmap.ic_launcher)
//        val style = NotificationCompat.BigTextStyle()
//        style.bigText("설정을 보려면 누르세요.")
//        style.setBigContentTitle(null)
//        style.setSummaryText("서비스 동작중")
//        builder.setContentText(null)
//        builder.setContentTitle(null)
//        builder.setOngoing(true)
//        builder.setStyle(style)
//        builder.setWhen(0)
//        builder.setShowWhen(false)
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
//        builder.setContentIntent(pendingIntent)
//        val manager = getSystemService<Any>(Context.NOTIFICATION_SERVICE) as NotificationManager?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            manager!!.createNotificationChannel(
//                NotificationChannel(
//                    "1",
//                    "undead_service",
//                    NotificationManager.IMPORTANCE_NONE
//                )
//            )
//        }
//        val notification: Notification = builder.build()
//        startForeground(1, notification)
//    }
//
//    fun onDestroy() {
//        super.onDestroy()
//        val calendar: Calendar = Calendar.getInstance()
//        calendar.setTimeInMillis(System.currentTimeMillis())
//        calendar.add(Calendar.SECOND, 3)
//        val intent = Intent(this, AlarmReceiver::class.java)
//        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
//        val alarmManager = getSystemService<Any>(Context.ALARM_SERVICE) as AlarmManager?
//        alarmManager!![AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()] = sender
//    }
//
//    fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//        val calendar: Calendar = Calendar.getInstance()
//        calendar.setTimeInMillis(System.currentTimeMillis())
//        calendar.add(Calendar.SECOND, 3)
//        val intent = Intent(this, AlarmReceiver::class.java)
//        val sender = PendingIntent.getBroadcast(this, 0, intent, 0)
//        val alarmManager = getSystemService<Any>(Context.ALARM_SERVICE) as AlarmManager?
//        alarmManager!![AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()] = sender
//    }
//
//    companion object {
//        var serviceIntent: Intent? = null
//    }
}