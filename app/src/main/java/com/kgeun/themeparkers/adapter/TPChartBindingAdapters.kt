package com.kgeun.themeparkers.adapter

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.OperTime
import com.kgeun.themeparkers.data.StatisticItem
import com.kgeun.themeparkers.view.TPChartActivity
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*



@BindingAdapter(value = ["setData", "setActivity"], requireAll = true)
fun setChartData(chart: LineChart, chartData: StatisticItem?, activity: TPChartActivity) {

    if (chartData == null) {
        return
    }

    val historyValue = ArrayList<Entry>()
    val tpViewModel: ThemeparkViewModel = getKoinInstance()

    val operTime = tpViewModel.operTime.value

    val historyDay = if (chartData.type == "yesterday") {
        DateTime.now().minusDays(1).withTimeAtStartOfDay().millis
    } else {
        DateTime.now().minusDays(7).withTimeAtStartOfDay().millis
    }

    val today = DateTime.now().withTimeAtStartOfDay().millis

    var operTimeStart = 0L
    var operTimeEnd = 0L

//    if (!activity.openTm.isNullOrEmpty() && !activity.closeTm.isNullOrEmpty()) {
//
//        operTimeStart = historyDay + open.get()
//        operTimeEnd = historyDay + close.get()
//    } else {
        operTimeStart = historyDay + (activity.openTm!!)
        operTimeEnd = historyDay + (activity.closeTm!!)
//    }

    val timeList: ArrayList<Long> = arrayListOf()
    var index = 0f

    chartData.history?.forEach timeLoop@{
        if (it.time!! < operTimeStart || it.time!! > operTimeEnd) {
            return@timeLoop
        }

        val time = DateTime(it.time)
        val min = time.minuteOfDay().get()

        if (it.waitTime!!.toFloat() > 900f) {
            historyValue.add(Entry(min.toFloat(), 0f))
        } else {
            historyValue.add(Entry(min.toFloat(), it.waitTime!!.toFloat()))
        }

        timeList.add(it.time)
    }

    val set1 = if (chartData.type == "yesterday") {
        LineDataSet(historyValue, "어제 대기시간")
    } else {
        LineDataSet(historyValue, "일주일 전 대기시간")
    }

    set1.lineWidth = 1.75f
    set1.color = ContextCompat.getColor(chart.context, R.color.highlight1)
    set1.setDrawCircles(false)
    set1.setDrawValues(false)
    set1.setDrawFilled(true)
    set1.fillDrawable = ContextCompat.getDrawable(chart.context, R.drawable.gradient_highlight);
    set1.axisDependency = AxisDependency.LEFT

    index = 0f

//    if (!activity.openTm.isNullOrEmpty() && !activity.closeTm.isNullOrEmpty()) {
//        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
//        val open = formatter.parseLocalTime(activity.openTm).millisOfDay()
//        val close = formatter.parseLocalTime(activity.closeTm).millisOfDay()
//
//        operTimeStart = today + open.get()
//        operTimeEnd = today + close.get()
//    } else {
//        operTimeStart = today + (operTime!!.startTime * 1000).toLong()
//        operTimeEnd = today + (operTime.endTime * 1000).toLong()
//    }
    operTimeStart = today + (activity.openTm!!)
    operTimeEnd = today + (activity.closeTm!!)

    val todayValue = ArrayList<Entry>()
    val timeList2: ArrayList<Long> = arrayListOf()

    chartData.today?.forEach timeLoop@{
        if (it.time!! < operTimeStart || it.time!! > operTimeEnd) {
            return@timeLoop
        }

        val time = DateTime(it.time)
        val min = time.minuteOfDay().get()

        if (it.waitTime!!.toFloat() > 900f) {
            todayValue.add(Entry(min.toFloat(), 0f))
        } else {
            todayValue.add(Entry(min.toFloat(), it.waitTime!!.toFloat()))
        }

        timeList2.add(it.time)
    }

    val set2 = LineDataSet(todayValue, "오늘 대기시간")

    set2.lineWidth = 1.75f
    set2.color = ContextCompat.getColor(chart.context, R.color.highlight_blue1)
    set2.setDrawCircles(false)
    set2.setDrawValues(false)
    set2.setDrawFilled(true)
    set2.fillDrawable = ContextCompat.getDrawable(chart.context, R.drawable.gradient_highlight_blue1);
    set2.axisDependency = AxisDependency.LEFT

    val data =  LineData(set1, set2)

    val startStr = DateTime(operTimeStart).toString("HH:mm")
    val endStr = DateTime(operTimeEnd).toString("HH:mm")

    chart.xAxis.labelCount = timeList.size

    chart.xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            val time = LocalTime((value.toInt() * 60 * 1000).toLong(), DateTimeZone.UTC)
            val str = time.toString("HH:mm")

            if (str.endsWith(":00", ignoreCase = true)) {
                return str.substring(0,2) + "시"
            } else {
               return ""
            }
        }
    }

    chart.data = data
    chart.animateX(2000)
}


@BindingAdapter("timeText")
fun settimeText(view: TextView, time: Long) {

    if (time == 0L) {
        return
    }

    val time = DateTime(time)
    val str = time.toString("YYYY년 MM월 dd일 HH:mm:ss")
    view.text = str
    view.visibility = View.VISIBLE
}