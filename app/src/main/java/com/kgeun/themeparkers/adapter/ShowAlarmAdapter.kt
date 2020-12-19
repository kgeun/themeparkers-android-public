package com.kgeun.themeparkers.adapter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.debop.kodatimes.minutes
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.data.ShowAlarmItem
import com.kgeun.themeparkers.databinding.ListItemShowAlarmBinding
import com.kgeun.themeparkers.util.AlarmReceiver
import com.kgeun.themeparkers.util.getTodayString
import com.kgeun.themeparkers.util.hashAtCodeAndTime
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.koin.core.KoinComponent
import org.koin.core.inject


class ShowAlarmAdapter : ListAdapter<ShowAlarmItem, RecyclerView.ViewHolder>(ShowAlarmDiffCallback()), KoinComponent {

    var atItem: Attraction? = null
    private val atRepository: AttractionRepository by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ShowTimeViewHolder(
            ListItemShowAlarmBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ShowTimeViewHolder).bind(currentList[position])
    }

    inner class ShowTimeViewHolder(
        private val binding: ListItemShowAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShowAlarmItem) {
            binding.apply {
                alarmItem = item
                executePendingBindings()

                val nowSec = DateTime.now().secondOfDay()

                val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HHmm")
                val formatter2: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")

//                val showTimeDate: DateTime = formatter.parseDateTime(item.showTime)
                val showTimeDate: DateTime = if (item.showTime.indexOf(":") < 0) {
                    formatter.parseDateTime(item.showTime)
                } else {
                    formatter2.parseDateTime(item.showTime)
                }

                val showTime: LocalTime = if (item.showTime.indexOf(":") < 0) {
                    formatter.parseLocalTime(item.showTime)
                } else {
                    formatter2.parseLocalTime(item.showTime)
                }

                if (showTimeDate.secondOfDay().get() < nowSec.get()) {
                    txtShowTime.isEnabled = false
                }

                if ((showTimeDate - 5.minutes()).secondOfDay().get() < nowSec.get()) {
                    btnBefore5Min.isEnabled = false
                    txtBefore5Min.isEnabled = false
                }

                if ((showTimeDate - 15.minutes()).secondOfDay().get() < nowSec.get()) {
                    btnBefore15Min.isEnabled = false
                    txtBefore15Min.isEnabled = false
                }

                if ((showTimeDate - 30.minutes()).secondOfDay().get() < nowSec.get()) {
                    btnBefore30Min.isEnabled = false
                    txtBefore30Min.isEnabled = false
                }

                btnBefore5Min.setOnClickListener {
                    if (!it.isSelected) {
                        Toast.makeText(
                            it.context,
                            "${item.showTime} 공연 5분 전 알람이 설정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        registerAlarm(item.atCode, (showTime - 5.minutes()).millisOfDay, atItem!!, item, 5)
                    } else {
                        Toast.makeText(
                            it.context,
                            "${item.showTime} 공연 5분 전 알람이 해제되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        unregisterAlarm(item.atCode, (showTime - 5.minutes()).millisOfDay)
                    }

                    item.before5min = !item.before5min
                    submitShowAlarm(item)
                }

                btnBefore15Min.setOnClickListener {
                    if (!it.isSelected) {
                        Toast.makeText(
                            it.context,
                            "${item.showTime} 공연 15분 전 알람이 설정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        registerAlarm(item.atCode, (showTime - 15.minutes()).millisOfDay, atItem!!, item, 15)
                    } else {
                        Toast.makeText(
                            it.context,
                            "${item.showTime} 공연 15분 전 알람이 해제되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        unregisterAlarm(item.atCode, (showTime - 15.minutes()).millisOfDay)
                    }

                    item.before15min = !item.before15min
                    submitShowAlarm(item)
                }

                btnBefore30Min.setOnClickListener {

                    if (!it.isSelected) {
                        Toast.makeText(
                            it.context,
                            "${item.showTime} 공연 30분 전 알람이 설정되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()

                        registerAlarm(item.atCode, (showTime - 30.minutes()).millisOfDay, atItem!!, item, 30)
                    } else {
                        Toast.makeText(
                            it.context,
                            "${item.showTime} 공연 30분 전 알람이 해되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        unregisterAlarm(item.atCode, (showTime - 30.minutes()).millisOfDay)
                    }

                    item.before30min = !item.before30min
                    submitShowAlarm(item)
                }
            }
        }

        fun registerAlarm(atCode: String, time: Int, atItem: Attraction, alarmItem: ShowAlarmItem, beforeMin: Int) {
            val alarmManager = binding.root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            var intent = Intent(binding.root.context, AlarmReceiver::class.java)
            intent.putExtra("showAlarmItem", Json.encodeToString(alarmItem))
            intent.putExtra("atItem", Json.encodeToString(atItem))
            intent.putExtra("beforeMin", beforeMin)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)


            var pendingIntent = PendingIntent.getBroadcast(
                binding.root.context, hashAtCodeAndTime(
                    atCode,
                    time
                ),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.forID("Asia/Seoul"))
            val showDate: DateTime = dateFormatter.parseDateTime(getTodayString())
            val alarmTime = showDate.millis

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime + time,
                pendingIntent
            )

            val time2 = DateTime(alarmTime + time)
        }

        fun unregisterAlarm(atCode: String, time: Int) {
            val alarmManager = binding.root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            var intent = Intent(binding.root.context, AlarmReceiver::class.java)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            var pendingIntent = PendingIntent.getBroadcast(
                binding.root.context,
                hashAtCodeAndTime(
                    atCode,
                    time
                ),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            alarmManager.cancel(pendingIntent)
        }


        fun submitShowAlarm(
            item: ShowAlarmItem
        ) {
            if (!item.before5min && !item.before15min && !item.before30min) {
                GlobalScope.launch {
                    atRepository.deleteAtrcAlarm(item.atCode, item.showTime)
                }
            } else {
                GlobalScope.launch {
                    atRepository.insertShowAlarm(item)
                }
            }
        }

        fun makeAlarm(item: ShowAlarmItem) {
            val alarmManager = binding.root.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            fun registerAlarm(valid: Boolean, registerItem: ShowAlarmItem, time: Int) {
                var intent = Intent(binding.root.context, AlarmReceiver::class.java)
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                var pendingIntent = PendingIntent.getBroadcast(
                    binding.root.context, hashAtCodeAndTime(
                        registerItem.atCode,
                        time
                    ), intent, PendingIntent.FLAG_UPDATE_CURRENT
                )

                val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withZone(DateTimeZone.forID("Asia/Seoul"))

                val showDate: DateTime = dateFormatter.parseDateTime(registerItem.alarmDate)
                val alarmTime = showDate.millis

                if (valid) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarmTime + time,
                            pendingIntent
                        )
                    }

                    val time2 = DateTime(alarmTime + time, DateTimeZone.forID("Asia/Seoul"))
                } else {
                    alarmManager.cancel(pendingIntent)
                    val time = DateTime(time.toLong())
                }
            }
        }
    }
}

private class ShowAlarmDiffCallback : DiffUtil.ItemCallback<ShowAlarmItem>() {

    override fun areItemsTheSame(
        oldItem: ShowAlarmItem,
        newItem: ShowAlarmItem
    ): Boolean {
        return (oldItem.atCode == newItem.atCode) && (oldItem.showTime == newItem.showTime)
    }

    override fun areContentsTheSame(
        oldItem: ShowAlarmItem,
        newItem: ShowAlarmItem
    ): Boolean {
        return oldItem == newItem
    }
}