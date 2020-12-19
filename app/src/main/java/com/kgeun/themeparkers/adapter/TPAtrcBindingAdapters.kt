package com.kgeun.themeparkers.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.debop.kodatimes.asUtc
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.*
import com.kgeun.themeparkers.databinding.ListItemArticlesInAtrcBinding
import com.kgeun.themeparkers.databinding.ListItemVideosInAtrcBinding
import com.kgeun.themeparkers.util.dp2px
import com.kgeun.themeparkers.util.drawAtrcStatus
import com.kgeun.themeparkers.util.hideAllStatus
import com.kgeun.themeparkers.util.setWaitTimeVisibility
import com.kgeun.themeparkers.view.TPAttractionActivity
import com.kgeun.themeparkers.view.TPImageViewerActivity
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_attraction.view.*
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import kotlinx.android.synthetic.main.list_item_more_videos.view.*
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.unbescape.html.HtmlEscape
import java.util.*

@BindingAdapter(value = ["thumbnailUrl", "largeThumbnailUrl"], requireAll = true)
fun setAtrcDetailThumbnailUrl(view: ImageView, thumbnailUrl: String?, largeThumbnailUrl: String?) {

    if (thumbnailUrl == null) {
        return
    }

    val url = if (largeThumbnailUrl.isNullOrEmpty()) {
        thumbnailUrl
    } else {
        largeThumbnailUrl
    }

    view.background = null

    val myOptions = RequestOptions()
        .centerCrop()

    Glide
        .with(view.context)
        .load(url)
        .apply(myOptions)
        .transition(DrawableTransitionOptions.withCrossFade(1000))
        .into(view)
}

@BindingAdapter("atrcStatusInDetail")
fun setAtrcStatusInDetail(viewGroup: LinearLayout, atItem: Attraction?) {
    if (atItem == null) {
        return
    }

    hideAllStatus(viewGroup)
    drawAtrcStatus(atItem, viewGroup, "ATRC")
}


@BindingAdapter(value = ["videoListInAtrc", "keyword"], requireAll = true)
fun videoListInAtrc(viewGroup: LinearLayout, videoList: List<YTVideoItem>?, keyword: String?) {

    viewGroup.removeAllViews()

    videoList?.forEach {
        val binding = ListItemVideosInAtrcBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        binding.ytVideoItem = it
        viewGroup.addView(binding.root)
    }

    val moreVideos = LayoutInflater.from(viewGroup.context).inflate(
        R.layout.list_item_more_videos,
        viewGroup
    )

    moreVideos.txtMoreVideo.text = "${keyword} 영상 더 보기"

    moreVideos.setOnClickListener {
        moreVideos.context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=$keyword")
            )
        )

        TPAnalytics.sendClick("ClickMoreVideos")
    }
}


@BindingAdapter("articleListInAtrc")
fun articleListInAtrc(viewGroup: LinearLayout, articleList: List<ArticleItem>?) {

    viewGroup.removeAllViews()

    articleList?.forEachIndexed() { index, article ->
        val binding = ListItemArticlesInAtrcBinding.inflate(
            LayoutInflater.from(viewGroup.context), viewGroup, false
        )

        binding.root.setOnClickListener {
            val url = article.link
            val intentBuilder = CustomTabsIntent.Builder()

            intentBuilder.setToolbarColor(
                ContextCompat.getColor(
                    it.context,
                    android.R.color.white
                )
            )
            intentBuilder.setSecondaryToolbarColor(
                ContextCompat.getColor(
                    it.context,
                    android.R.color.white
                )
            )
            intentBuilder.setShowTitle(true)
            intentBuilder.enableUrlBarHiding()

            try {
                val customTabsIntent = intentBuilder.build()
                customTabsIntent.intent.setPackage("com.android.chrome")
                customTabsIntent.launchUrl(it.context, Uri.parse(url))
            } catch (e: java.lang.Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    it.context.startActivity(intent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            TPAnalytics.sendClick("ClickAtrcArticle")
        }

        article.title = article.title?.replace("\\<[^>]*>".toRegex(), "")
        article.description = article.description?.replace("\\<[^>]*>".toRegex(), "")

        if (!article.postdate.isNullOrEmpty()) {
            article.postdate =
                "${article.postdate?.substring(0..3)}.${article.postdate?.substring(4..5)}.${
                    article.postdate?.substring(
                        6..7
                    )
                }"
        }

        binding.articleItem = article
        viewGroup.addView(binding.root)

        if (index != articleList.lastIndex) {
            val divider = View(viewGroup.context)
            divider.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp2px(1, viewGroup.context)
                )
            divider.setBackgroundResource(R.color.basicBlack0_05)

            viewGroup.addView(divider)
        }
    }
}

@BindingAdapter("articleKeyword")
fun moreArticles(view: View, keyword: String?) {

    val url = "https://m.search.naver.com/search.naver?where=m_view&where=m&query=$keyword"

    view.setOnClickListener {
        val intentBuilder = CustomTabsIntent.Builder()

        intentBuilder.setToolbarColor(ContextCompat.getColor(view.context, android.R.color.white))
        intentBuilder.setSecondaryToolbarColor(
            ContextCompat.getColor(
                view.context,
                android.R.color.white
            )
        )
        intentBuilder.setShowTitle(true)
        intentBuilder.enableUrlBarHiding()

        try {
            val customTabsIntent = intentBuilder.build()
            customTabsIntent.intent.setPackage("com.android.chrome")
            customTabsIntent.launchUrl(view.context, Uri.parse(url))
        } catch (e: java.lang.Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                view.context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}

@BindingAdapter("loadVideoUrl")
fun loadVideoUrl(view: ImageView, url: String?) {

    val myOptions = RequestOptions()
        .fitCenter()
        .transform(RoundedCorners(dp2px(12, view.context)))

//    Glide
//        .with(view.context)
//        .load(url)
//        .apply(myOptions)
//        .placeholder(R.drawable.image_placeholder_wide)
//        .transition(DrawableTransitionOptions.withCrossFade(200))
//        .preload()

    Glide
        .with(view.context)
        .load(url)
        .placeholder(R.drawable.image_placeholder_wide)
        .apply(myOptions)
//        .transition(DrawableTransitionOptions.withCrossFade(1000))
        .into(view)
}

@BindingAdapter("encodedText")
fun decodeText(view: TextView, text: String?) {
    view.text = HtmlEscape.unescapeHtml(text)
}

@BindingAdapter("youtubeVideoId")
fun setVideoLink(viewGroup: ViewGroup, videoId: String?) {
    if (videoId != null) {
        viewGroup.setOnClickListener {
            viewGroup.context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=$videoId")
                )
            )

            TPAnalytics.sendClick("ClickYoutubeArticle")
        }
    }
}

@BindingAdapter(value = ["operTimeStatusInDetail", "operTimeStatusInDetail2"], requireAll = true)
fun setOperTimeStatus(viewGroup: ViewGroup, atItem: Attraction?, atDetail: AtrcDetail?) {

    if (atItem == null || atDetail == null) {
        return
    }

    if (!(atDetail.closeTm.isNullOrEmpty() && atDetail.openTm.isNullOrEmpty())) {
        viewGroup.visibility = View.VISIBLE

        val now = DateTime.now().secondOfDay().get()
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        val openTime = formatter.parseDateTime(atDetail.openTm).secondOfDay().get()
        val closeTime = formatter.parseDateTime(atDetail.closeTm).secondOfDay().get()

        viewGroup.operTimeTextInDetail.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)

        if (now in (openTime + 1) until closeTime) {
            viewGroup.operTimeTextInDetail.setTextColor(
                ContextCompat.getColor(
                    viewGroup.context,
                    R.color.highlight_green
                )
            )
            viewGroup.textOperTimeStatus.setTextColor(
                ContextCompat.getColor(
                    viewGroup.context,
                    R.color.highlight_green
                )
            )
            viewGroup.operTimeTextInDetail.text = "현재 운영중"
        } else {
            viewGroup.operTimeTextInDetail.setTextColor(
                ContextCompat.getColor(
                    viewGroup.context,
                    R.color.highlight1
                )
            )
            viewGroup.textOperTimeStatus.setTextColor(
                ContextCompat.getColor(
                    viewGroup.context,
                    R.color.highlight1
                )
            )
            viewGroup.operTimeTextInDetail.text = "운영시간이 아닙니다."
        }
    } else {
        if (atItem.status.isNullOrEmpty() && atItem.waitTime == null) {
            viewGroup.operTimeTextInDetail.text = "파크 운영시간과 동일"
            viewGroup.textOperTimeStatus.visibility = View.GONE
            viewGroup.operTimeTextInDetail.setTextColor(
                ContextCompat.getColor(
                    viewGroup.context,
                    R.color.basicBlack
                )
            )
            viewGroup.operTimeTextInDetail.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15f)
            viewGroup.operTimeTextInDetail.setTypeface(
                viewGroup.operTimeTextInDetail.typeface,
                Typeface.BOLD
            )
        } else {
            viewGroup.visibility = View.GONE
        }
    }
}

@BindingAdapter("setWaitVisible")
fun setWaitVisible(view: ViewGroup, atItem: Attraction?) {
    setWaitTimeVisibility(view, atItem)
}

@BindingAdapter("areaName")
fun setAreaName(textView: TextView, atItem: Attraction?) {
    if (atItem != null) {
        var areaName = ""

        if (!atItem.areaKR.isNullOrEmpty()) {
            areaName += "${atItem.areaKR}, "
        }
        if (atItem.tpNameKR != null) {
            areaName += atItem.tpNameKR
        }

        textView.text = areaName
    } else {
        textView.text = ""
    }
}

var mapLeft: Double = 0.0
var mapTop: Double = 0.0

@BindingAdapter("mapImage")
fun setMapImageUrl(viewGroup: ViewGroup, atDetail: AtrcDetail?) {

    Glide
        .with(viewGroup.context)
        .load(atDetail?.mapImageUrl)
        .transition(DrawableTransitionOptions.withCrossFade(1000))
        .listener(object : RequestListener<Drawable> {
            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {

                if (atDetail?.mapMarkerLeft.isNullOrEmpty() || atDetail?.mapMarkerTop.isNullOrEmpty()) {
                    viewGroup.ivMapMarker.visibility = View.GONE
                    return false
                }

                val mapWidth = resource!!.intrinsicWidth
                val mapHeight = resource.intrinsicHeight

                try {
                    mapLeft =
                        (Integer.parseInt(atDetail?.mapMarkerLeft!!.filter { it.isDigit() }) / 835.0)
                    mapTop =
                        (Integer.parseInt(atDetail?.mapMarkerTop!!.filter { it.isDigit() }) / 626.0)

                    val markerLeft = mapWidth * mapLeft
                    val markerTop = mapHeight * mapTop

                    val lp = viewGroup.ivMapMarker.layoutParams as FrameLayout.LayoutParams

                    lp.leftMargin = markerLeft.toInt()
                    lp.topMargin = markerTop.toInt()

                    viewGroup.ivMapMarker.layoutParams = lp
                    viewGroup.ivMapMarker.visibility = View.VISIBLE
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
                return false
            }

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                viewGroup.visibility = View.GONE
                (viewGroup.parent as ViewGroup).mapDivider.visibility = View.GONE
                return false
            }
        })
        .into(viewGroup.imageMap)

    viewGroup.imageMap.setOnClickListener {
        val intent = Intent(viewGroup.context, TPImageViewerActivity::class.java)
        intent.putExtra("imageUrl", atDetail?.mapImageUrl)
        if (atDetail?.mapMarkerLeft.isNullOrEmpty() || atDetail?.mapMarkerTop.isNullOrEmpty()) {
            intent.putExtra("isMapMarker", false)
        } else {
            intent.putExtra("isMapMarker", true)
        }
        viewGroup.context.startActivity(intent)
        (viewGroup.context as Activity).overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )

        TPAnalytics.sendClick("ClickAtrcMap")
    }

}

@BindingAdapter(value = ["setEstiChartData", "setChartColorByMin", "atrcActivity", "tpOperTime", "setAtItem"], requireAll = true)
fun setEstiChartData(chart: BarChart, atDetail: AtrcDetail?, currentMin: Int?, activity: TPAttractionActivity?, tpOperTime: OperTime?, atItem: Attraction?) {

    if (atDetail?.estiChartData == null || currentMin == null || activity == null || atItem == null || atItem.kindCode != "RIDE") {
        return
    }

    val historyValue = ArrayList<BarEntry>()
    val tpViewModel: ThemeparkViewModel = getKoinInstance()
    val chartData = atDetail.estiChartData

    val historyDay = DateTime.now().withTimeAtStartOfDay().millis

    var operTimeStart = 0L
    var operTimeEnd = 0L

    if (!atDetail.openTm.isNullOrEmpty() && !atDetail.closeTm.isNullOrEmpty()) {
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
        val open = formatter.parseLocalTime(atDetail.openTm).millisOfDay()
        val close = formatter.parseLocalTime(atDetail.closeTm).millisOfDay()

        operTimeStart = historyDay + open.get()
        operTimeEnd = historyDay + close.get()
    } else {
        operTimeStart = historyDay + (tpOperTime!!.startTime * 1000).toLong()
        operTimeEnd = historyDay + (tpOperTime.endTime * 1000).toLong()
    }

    val timeList: ArrayList<Int> = arrayListOf()
    val colorList: MutableList<Int> = mutableListOf()
    var index = 0f

    chartData?.forEachIndexed timeLoop@{ eIdx, it ->
        val time = DateTime(it.time)
        val min = time.minuteOfDay

        if (it.time!! < operTimeStart || it.time!! > operTimeEnd - 600000) {
            return@timeLoop
        }

        if ((min % 60) > 0) {
            return@timeLoop
        }

        historyValue.add(BarEntry(index, (it.waitLevel ?: 0).toFloat()))

        when {
            currentMin >= min + 60 -> {
                colorList.add(chart.context.getColor(R.color.basicBlack0_5))
            }
            currentMin in min..min+60 -> {
                colorList.add(chart.context.getColor(R.color.highlight1_sub))
            }
            else -> {
                colorList.add(chart.context.getColor(R.color.highlight_blue1))
            }
        }

        timeList.add(min)
        index++
    }

    if (timeList.size == 0) {
        chart.visibility = View.GONE
        activity.binding.showChart = false
    } else {
        chart.visibility = View.VISIBLE
        activity.binding.showChart = true
    }

    val set1 = BarDataSet(historyValue, "오눌 예상 대기시간")

    set1.colors = colorList
    set1.setDrawValues(false)
    set1.axisDependency = YAxis.AxisDependency.LEFT

    val data = BarData(set1)

    chart.xAxis.labelCount = historyValue.size

    chart.xAxis.valueFormatter = object: ValueFormatter() {
        override fun getFormattedValue(value: Float): String {

            val time = LocalTime((timeList[value.toInt()] * 60 * 1000).toLong(), DateTimeZone.UTC)
            val str = time.toString("HH:mm")

            if (str.endsWith(":00", ignoreCase = true)) {
                return str.substring(0, 2) + "시"
            } else {
                return ""
            }
        }
    }

    chart.data = data
}


@BindingAdapter("estiInDetail")
fun estiInDetail(viewGroup: ViewGroup, atItem: Attraction?) {
    if (atItem == null) {
        viewGroup.visibility = View.GONE
        return
    }

    viewGroup.visibility = View.GONE

//    if (atItem.kindCode == "RIDE") {
//        when (atItem.status) {
//            "OVER" -> {
//                viewGroup.textAtrcOverEsti.visibility = View.VISIBLE
//            }
//            "CLOS" -> {
//                viewGroup.textAtrcCloseEsti.visibility = View.VISIBLE
//            }
//            "STND" -> {
//                viewGroup.textStandbyEsti.visibility = View.VISIBLE
//            }
//            "RAIN" -> {
//                viewGroup.llAtrcRainEsti.visibility = View.VISIBLE
//                viewGroup.llAtrcRainEsti.textRainEsti.text = "우천대기 (예상)"
//            }
//            else -> {
//                if (atItem.waitLevel != null) {
//                    viewGroup.llAtrcWaitTimeEsti.visibility = View.VISIBLE
//
//                    if (atItem.waitLevel == 1) {
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.text = "여유 (0 ~ 30분)"
//                        viewGroup.llAtrcWaitTimeEsti.ivTimeIconEsti.setColorFilter(viewGroup.context.getColor(R.color.highlight_green))
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.setTextColor(
//                            ContextCompat.getColor(
//                                viewGroup.context,
//                                R.color.highlight_green
//                            )
//                        )
//                    } else if (atItem.waitLevel == 2) {
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.text = "보통 (30 ~ 60분)"
//                        viewGroup.llAtrcWaitTimeEsti.ivTimeIconEsti.setColorFilter(viewGroup.context.getColor(R.color.highlight_yellow))
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.setTextColor(
//                            ContextCompat.getColor(
//                                viewGroup.context,
//                                R.color.highlight_yellow
//                            )
//                        )
//                    } else if (atItem.waitLevel == 3) {
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.text = "약간 혼잡 (60 ~ 90분)"
//                        viewGroup.llAtrcWaitTimeEsti.ivTimeIconEsti.setColorFilter(viewGroup.context.getColor(R.color.highlight1_sub))
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.setTextColor(
//                            ContextCompat.getColor(
//                                viewGroup.context,
//                                R.color.highlight1
//                            )
//                        )
//                    } else if (atItem.waitLevel == 4 || atItem.waitLevel == 5) {
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.text = if (atItem.waitLevel == 4) {
//                            "혼잡 (90 ~ 120분)"
//                        } else {
//                            "매우 혼잡 (120분 이상)"
//                        }
//
//                        viewGroup.llAtrcWaitTimeEsti.ivTimeIconEsti.setColorFilter(viewGroup.context.getColor(R.color.highlight_red1))
//                        viewGroup.llAtrcWaitTimeEsti.textWaitMinEsti.setTextColor(
//                            ContextCompat.getColor(
//                                viewGroup.context,
//                                R.color.highlight_red1
//                            )
//                        )
//                    }
//                } else {
//                    viewGroup.visibility = View.GONE
//                }
//            }
//        }
//    } else {
//        viewGroup.visibility = View.GONE
//    }
}

@BindingAdapter("showEstiInfo")
fun showEstiInfo(view: ViewGroup, atItem: Attraction?) {
    if (atItem == null) {
        return
    }

    if (atItem.kindCode != "RIDE" || atItem.popularity == 0) {
        view.visibility = View.GONE
    }
}

@BindingAdapter("showPopularityText")
fun showPopularityText(viewGroup: ViewGroup, popularity: Int?) {
    if (popularity == null) {
        viewGroup.txtPopularity.text = "정보없음"
        viewGroup.ivPopularity.visibility = View.GONE
        return
    }

    if (popularity >= 130) {
        viewGroup.txtPopularity.text = "특급"
        viewGroup.ivPopularity.visibility = View.VISIBLE
        viewGroup.ivPopularity.setColorFilter(viewGroup.context.getColor(R.color.highlight_red1))
    } else if (popularity >= 100) {
        viewGroup.txtPopularity.text = "최상"
        viewGroup.ivPopularity.visibility = View.VISIBLE
        viewGroup.ivPopularity.setColorFilter(viewGroup.context.getColor(R.color.highlight_red1))
    } else if (popularity >= 70) {
        viewGroup.txtPopularity.text = "상"
        viewGroup.ivPopularity.visibility = View.VISIBLE
        viewGroup.ivPopularity.setColorFilter(viewGroup.context.getColor(R.color.highlight1))
    } else if (popularity >= 40) {
        viewGroup.txtPopularity.text = "중"
        viewGroup.ivPopularity.visibility = View.VISIBLE
        viewGroup.ivPopularity.setColorFilter(viewGroup.context.getColor(R.color.highlight_green))
    } else if (popularity > 1) {
        viewGroup.txtPopularity.text = "하"
        viewGroup.ivPopularity.visibility = View.VISIBLE
        viewGroup.ivPopularity.setColorFilter(viewGroup.context.getColor(R.color.highlight_blue2))
    } else {
        viewGroup.txtPopularity.text = "정보없음"
        viewGroup.ivPopularity.visibility = View.GONE
    }
}

@BindingAdapter("showDayOfWk")
fun showDayOfWk(viewGroup: ViewGroup, atDetail: AtrcDetail?) {

    if (atDetail == null) {
        viewGroup.txtDayofWk.text = "정보없음"
        viewGroup.ivDayOfWk.visibility = View.GONE
        return
    }

    viewGroup.txtDayofWk.text = atDetail.dayOfWeek
    viewGroup.ivDayOfWk.visibility = View.VISIBLE

    if (atDetail.isWeekend == true) {
        viewGroup.ivDayOfWk.setColorFilter(viewGroup.context.getColor(R.color.highlight_red1))
    } else {
        viewGroup.ivDayOfWk.setColorFilter(viewGroup.context.getColor(R.color.highlight_blue2))
    }
}

@BindingAdapter("showHoliday")
fun showHoliday(viewGroup: ViewGroup, atDetail: AtrcDetail?) {

    if (atDetail == null) {
        viewGroup.txtHoliday.text = "정보없음"
        viewGroup.ivHoliday.visibility = View.GONE
        return
    }

    viewGroup.txtHoliday.text = atDetail.todayInfo
    viewGroup.ivHoliday.visibility = View.VISIBLE

    if (atDetail.isHoliday == true) {
        viewGroup.ivHoliday.setColorFilter(viewGroup.context.getColor(R.color.highlight_red1))
    } else {
        viewGroup.ivHoliday.setColorFilter(viewGroup.context.getColor(R.color.highlight_blue2))
    }
}

@BindingAdapter("setWeatherText")
fun setWeatherText(view: TextView, atDetail: AtrcDetail?) {
    if (atDetail == null) {
        view.text = "정보없음"
        return
    }

    view.text = atDetail.weatherInfo
}

@BindingAdapter("estiInfoOpened")
fun estiInfoOpened(view: TextView, opened: Boolean?) {
    if (opened == null || !opened) {
        view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info2, 0, R.drawable.ic_down_arrow2, 0)
    } else {
        view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info2, 0, R.drawable.ic_up_arrow2, 0)
    }
}