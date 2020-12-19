package com.kgeun.themeparkers.data

import com.kgeun.themeparkers.data.ArticleItem
import com.kgeun.themeparkers.data.YTVideoItem
import java.time.DayOfWeek

data class AtrcDetail (
    var description: String? = "",
    var descriptionKR: String? = "",
    var relatedVideo: List<YTVideoItem>?,
    var mapImageUrl: String?,
    var relatedArticleKR: List<ArticleItem>?,
    var mapMarkerTop: String?,
    var mapMarkerLeft: String?,
    var openTm: String?,
    var closeTm: String?,
    var showPeriod: String?,
    var playTime: String?,
    var availDescKR: String?,
    var availDesc: String?,
    var estiChartData: ArrayList<WaitTimeItem>?,
    var dayOfWeek: String?,
    var todayInfo: String?,
    var weatherInfo: String?,
    var isHoliday: Boolean?,
    var isWeekend: Boolean?
)