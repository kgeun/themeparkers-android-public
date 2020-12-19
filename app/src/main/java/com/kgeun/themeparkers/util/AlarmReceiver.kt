package com.kgeun.themeparkers.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kgeun.themeparkers.AlarmService
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.data.ShowAlarmItem
import com.kgeun.themeparkers.view.TPAlarmActivity
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject


class AlarmReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent?) {

        val atRepository: AttractionRepository by inject()
        val notiId = System.currentTimeMillis().toInt()

        val oIntent = Intent(context, TPAlarmActivity::class.java)
        oIntent.putExtra("showAlarmItem", intent?.getStringExtra("showAlarmItem"))
        oIntent.putExtra("atItem", intent?.getStringExtra("atItem"))
        oIntent.putExtra("beforeMin", intent?.getIntExtra("beforeMin", 0))
        oIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val CHANNEL_ID = "${context.packageName}_CHANNEL"

                registerNotificationChannel(context)

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    oIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                var atItem: Attraction? = null
                var showAlarmItem: ShowAlarmItem? = null
                val beforeMin = intent?.getIntExtra("beforeMin", 0)

                intent?.getStringExtra("atItem")?.let {
                    val saType = object : TypeToken<Attraction>() {}.type
                    atItem = Gson().fromJson<Attraction>(it, saType)
                }

                intent?.getStringExtra("showAlarmItem")?.let {
                    val saType = object : TypeToken<ShowAlarmItem>() {}.type
                    showAlarmItem = Gson().fromJson<ShowAlarmItem>(it, saType)
                }

                val notificationBuilder =
                    NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.smallicon)
                        .setContentTitle(atItem?.nameKR)
                        .setContentText("공연시간 ${beforeMin}분 전입니다.")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setAutoCancel(true)
                        .setFullScreenIntent(fullScreenPendingIntent, true)

                val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
                hashMap["eventTime"] = System.currentTimeMillis().toString()
                TPAnalytics.sendEvent("notify_time", hashMap.toMap())

                mNotificationManager.notify(
                    notiId,
                    notificationBuilder.build()
                )

            } else {
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val mp: MediaPlayer = MediaPlayer.create(
                        context.applicationContext,
                        notification
                    )
                    mp.start()
                }
                context.startActivity(oIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v!!.vibrate(
                VibrationEffect.createOneShot(
                    1000,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            //deprecated in API 26
            v!!.vibrate(1000)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            GlobalScope.launch {
                atRepository.removeExpiredAlarms {
                    if (AlarmService.isRunning) {
                        val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
                        hashMap["eventTime"] = System.currentTimeMillis().toString()
                        TPAnalytics.sendEvent("removeExpiredAlarms", hashMap.toMap())

                        context.stopService(Intent(context, AlarmService::class.java))
                    }
                }
            }
        }, 200)
    }
}