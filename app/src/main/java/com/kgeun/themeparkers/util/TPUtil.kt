package com.kgeun.themeparkers.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPPrefCtl
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.RideFilterItem
import com.kgeun.themeparkers.data.ShowAlarmItem
import com.kgeun.themeparkers.data.ShowTimeItem
import com.kgeun.themeparkers.network.KR_EVLDynamicResponse
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import java.lang.Exception
import java.time.LocalDateTime

fun dp2px(dp: Int, context: Context) : Int {
    val scale = context.resources.displayMetrics.density
    return (dp * scale).toInt()
}

fun getToday(): String = LocalDate.now().toString("yyyyMMdd")

fun rideFilter(list: List<Attraction>, filter: RideFilterItem) : List<Attraction> {
    return list.filter {
        fun checkRideStatus(): Boolean {
            if (filter.statusMode == RIDE_ALL) {
                return true
            }
            return if (it.status == "OPEN") {
                when (filter.statusMode) {
                    RIDE_OPERATING -> {
                        true
                    }
                    RIDE_UNDER_1HR -> {
                        it.waitTime!! <= 60
                    }
                    else -> {
                        it.waitTime!! <= 30
                    }
                }
            } else {
                false
            }
        }

        if (filter.height == -1 && filter.statusMode == RIDE_ALL) {
            true
        } else {
            try {
                if (filter.height > -1) {
                    if (!it.heightFrom.isNullOrEmpty() && filter.height < Integer.parseInt(it.heightFrom!!)) {
                        false
                    } else if (!it.heightTo.isNullOrEmpty() && filter.height > Integer.parseInt(it.heightTo!!)) {
                        false
                    } else {
                        checkRideStatus()
                    }
                } else {
                    checkRideStatus()
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}

fun getTodayString(): String {
    val dtfOut = DateTimeFormat.forPattern("dd/MM/yyyy")
    return dtfOut.print(DateTime.now().millis)
}


fun hashAtCodeAndTime(atCode: String, time: Int): Int {
    val byteArray = atCode.toByteArray()
    val sum = byteArray.last().toInt() + byteArray[byteArray.size - 2].toInt()
    return time + sum
}


fun drawAtrcStatus(atItem: Attraction, viewGroup: ViewGroup, from: String) {
    if (atItem.kindCode == "RIDE") {
        when (atItem.status?.toUpperCase()) {
            "OPEN" -> {
                if (atItem.waitTime != null) {
                    viewGroup.llAtrcWaitTime.visibility = View.VISIBLE
                    viewGroup.llAtrcWaitTime.textWaitMin.text = "${atItem.waitTime}분"

                    if (atItem.waitTime!! <= 30) {
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setImageResource(R.drawable.icon_time_green)
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight_green
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight_green)
                        }
                    } else if (atItem.waitTime!! in 31..60) {
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setImageResource(R.drawable.icon_time_yellow)
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight_yellow
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight_yellow)
                        }
                    } else if (atItem.waitTime!! > 60) {
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setImageResource(R.drawable.icon_time_red)
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight_red1
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight_red1)
                        }
                    }
                } else {
                    viewGroup.textAtrcUnknown.visibility = View.VISIBLE
                    viewGroup.textAtrcUnknown.text = atItem.waitText
                }
            }
            "CLOS" -> {
                viewGroup.textAtrcClose.visibility = View.VISIBLE
            }
            "WNTR" -> {
                viewGroup.textAtrcWinterClose.visibility = View.VISIBLE
            }
            "RAIN" -> {
                viewGroup.llAtrcRain.visibility = View.VISIBLE
            }
            "WIND" -> {
                viewGroup.llAtrcWind.visibility = View.VISIBLE
            }
            "SNOW" -> {
                viewGroup.llAtrcSnow.visibility = View.VISIBLE
            }
            "OVER" -> {
                viewGroup.textAtrcOver.visibility = View.VISIBLE
            }
            "STND" -> {
                viewGroup.textStandby.visibility = View.VISIBLE
            }
            "PEND" -> {
                viewGroup.textAtrcUnknown.visibility = View.VISIBLE
                viewGroup.textAtrcUnknown.text = "일시중지"
            }
            "MAINT" -> {
                viewGroup.llAtrcAlert.visibility = View.VISIBLE
            }
            "TEMPERATURE" -> {
                viewGroup.llAtrcCold.visibility = View.VISIBLE
            }
            else -> {
                viewGroup.textAtrcUnknown.visibility = View.VISIBLE
                viewGroup.textAtrcUnknown.text = atItem.status
            }
        }
    }
}


fun drawAtrcEsti(atItem: Attraction, viewGroup: ViewGroup, from: String) {
    if (atItem.kindCode == "RIDE") {
        when (atItem.status) {
//            "CLOS" -> {
//                viewGroup.textAtrcClose.visibility = View.VISIBLE
//            }
            "OVER" -> {
                viewGroup.textAtrcOver.visibility = View.VISIBLE
            }
            "CLOS" -> {
                viewGroup.textAtrcClose.visibility = View.VISIBLE
            }
            "STND" -> {
                viewGroup.textStandby.visibility = View.VISIBLE
            }
            "RAIN" -> {
                viewGroup.llAtrcRain.visibility = View.VISIBLE
                viewGroup.llAtrcRain.textRain.text = "우천대기 (예상)"
            }
            else -> {
                if (atItem.waitLevel != null) {
                    viewGroup.llAtrcWaitTime.visibility = View.VISIBLE

                    if (atItem.waitLevel == 1) {
                        viewGroup.llAtrcWaitTime.textWaitMin.text = "여유"
//                        viewGroup.llAtrcWaitTime.ivTimeIcon.setImageResource(R.drawable.icon_time_green)
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setColorFilter(viewGroup.context.getColor(R.color.highlight_green))
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight_green
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight_green)
                        }
                    } else if (atItem.waitLevel == 2) {
                        viewGroup.llAtrcWaitTime.textWaitMin.text = "보통"
//                        viewGroup.llAtrcWaitTime.ivTimeIcon.setImageResource(R.drawable.icon_time_yellow)
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setColorFilter(viewGroup.context.getColor(R.color.highlight_yellow))
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight_yellow
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight_yellow)
                        }
                    } else if (atItem.waitLevel == 3) {
                        viewGroup.llAtrcWaitTime.textWaitMin.text = "약간 혼잡"
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setColorFilter(viewGroup.context.getColor(R.color.highlight1_sub))
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight1
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight1)
                        }
                    } else if (atItem.waitLevel == 4 || atItem.waitLevel == 5) {
                        viewGroup.llAtrcWaitTime.textWaitMin.text = if (atItem.waitLevel == 4) {
                            "혼잡"
                        } else {
                            "매우 혼잡"
                        }

//                        viewGroup.llAtrcWaitTime.ivTimeIcon.setImageResource(R.drawable.icon_time_red)
                        viewGroup.llAtrcWaitTime.ivTimeIcon.setColorFilter(viewGroup.context.getColor(R.color.highlight_red1))
                        viewGroup.llAtrcWaitTime.textWaitMin.setTextColor(
                            ContextCompat.getColor(
                                viewGroup.context,
                                R.color.highlight_red1
                            )
                        )
                        if (from == "MAIN") {
                            viewGroup.llAtrcWaitTime.setBackgroundResource(R.drawable.bg_translucent_highlight_red1)
                        }
                    }
                } else {
                    viewGroup.textAtrcUnknown.visibility = View.VISIBLE
                    viewGroup.textAtrcUnknown.text = atItem.waitText
                }
            }
        }
    }
}

//checkWaitTimeVisibilityLambda (
//esti = {
//    drawAtrcEsti(atItem, viewGroup, "MAIN")
//},
//success = {
//    drawAtrcStatus(atItem, viewGroup, "MAIN")
//}
//)

fun setWaitTimeVisibility(view: View, atItem: Attraction?) {
    if (atItem == null)
        return

    if (checkShowWaitTime(atItem.tpCode)) {
        if (atItem.kindCode == "RIDE") {
            if (atItem.status.isNullOrEmpty()) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
            }
        } else {
            view.visibility = View.GONE
        }
    } else {
        view.visibility = View.GONE
    }
}

fun checkShowWaitTime(tpCode: String): Boolean {
    val remoteConfig = FirebaseRemoteConfig.getInstance()

    return (tpCode == "KR_EVL" && remoteConfig.getBoolean("kr_evl_show_waittime")) || (tpCode == "KR_LTW" && remoteConfig.getBoolean("kr_ltw_show_waittime"))
}

fun checkShowMainWaitTime(tpCode: String): Boolean {
    val remoteConfig = FirebaseRemoteConfig.getInstance()

    return (tpCode == "KR_EVL" && (remoteConfig.getString("kr_evl_main_type")) == "waittime") || (tpCode == "KR_LTW" && (remoteConfig.getString("kr_ltw_main_type")) == "waittime")
}

fun getShowModeQuery(tpCode: String): Int {
    return if (checkShowWaitTime(tpCode)) {
        TYPE_FULL
    } else {
        TYPE_WAITTIME_ONLY_ESTI
    }
}


fun hideAllStatus(viewGroup: ViewGroup) {
    viewGroup.textAtrcOver.visibility = View.GONE
    viewGroup.textAtrcClose.visibility = View.GONE
    viewGroup.textStandby.visibility = View.GONE
    viewGroup.llAtrcAlert.visibility = View.GONE
    viewGroup.llAtrcRain.visibility = View.GONE
    viewGroup.llAtrcWind.visibility = View.GONE
    viewGroup.llAtrcWaitTime.visibility = View.GONE
    viewGroup.textAtrcUnknown.visibility = View.GONE
    viewGroup.textAtrcWinterClose.visibility = View.GONE
    viewGroup.llAtrcSnow.visibility = View.GONE
    viewGroup.llAtrcCold.visibility = View.GONE
    viewGroup.llAtrcWaitTime.ivTimeIcon.clearColorFilter()
}

fun registerNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val CHANNEL_ID = "${context.packageName}_CHANNEL"
        val CHANNEL_NAME = "공연시간 알람 설정"

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel: NotificationChannel? = mNotificationManager.getNotificationChannel(
            CHANNEL_ID
        )
        if (notificationChannel == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lightColor = Color.RED
            channel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            mNotificationManager.createNotificationChannel(channel)
        }
    }
}