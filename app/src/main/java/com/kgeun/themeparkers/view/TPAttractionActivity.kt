package com.kgeun.themeparkers.view

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Transformation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.kgeun.themeparkers.AlarmService
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPBaseActivity
import com.kgeun.themeparkers.adapter.ShowAlarmAdapter
import com.kgeun.themeparkers.data.*
import com.kgeun.themeparkers.databinding.ActivityAttractionBinding
import com.kgeun.themeparkers.util.dp2px
import com.kgeun.themeparkers.util.getTodayString
import com.kgeun.themeparkers.viewmodels.AttractionViewModel
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_attraction.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class TPAttractionActivity : TPBaseActivity() {

    lateinit var binding: ActivityAttractionBinding
    lateinit var activityView: View

    private val atViewModel: AttractionViewModel by viewModel()
    private val tpViewModel: ThemeparkViewModel by viewModel()
    private val atRepository: AttractionRepository by inject()

    private var mHeaderShowPosition = 0
    private var mArrowHidePosition = 1000
    private var isHeaderShowing = false
    private var isArrowShowing = false

    lateinit var showTimeList: List<ShowTimeItem>
    var showTimeInit = false

    private var alarmIntent: Intent? = null

    lateinit var mShowAlarmAdapter: ShowAlarmAdapter

    private var kindCode = ""
    private var atCode = ""

    private var estiDescExtened = false

    companion object {
        val EXTRA_KEY_AT_CODE = "extra_key_at_code"
        val EXTRA_KEY_KIND_CODE = "extra_key_kind_code"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityAttractionBinding>(
            this,
            R.layout.activity_attraction
        )
        activityView = binding.root
        binding.activity = this
        binding.estiInfoOpened = false

        activityView.contentLayout.visibility = View.GONE
        activityView.atrcDetailThumbnailLayout.visibility = View.GONE

        setListener()
        initViews()

        Handler(Looper.getMainLooper()).postDelayed({
            showAnimation()
        }, 400)

        if (intent.hasExtra(EXTRA_KEY_KIND_CODE)) {
            kindCode = intent.getStringExtra(EXTRA_KEY_KIND_CODE) ?: ""
        }
        if (intent.hasExtra(EXTRA_KEY_AT_CODE)) {
            subscribeUi(intent.getStringExtra(EXTRA_KEY_AT_CODE)!!)
            atCode = intent.getStringExtra(EXTRA_KEY_AT_CODE)!!
        }

        TPAnalytics.sendView("ViewAttractionDetail")
        val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
        intent.getStringExtra(EXTRA_KEY_AT_CODE)?.let {
            hashMap["atCode"] = it
        }
        TPAnalytics.sendEvent("ViewAttractionDetail", hashMap)
    }

    private fun refreshAtrcDynamic() {
        GlobalScope.launch {
            if (binding.atItem != null) {
                atRepository.updateAttractionsDynamic(binding.atItem!!.tpCode) { stat ->
                    if (stat == "ERROR") {
                        Toast.makeText(this@TPAttractionActivity, "정보를 가져오는데 실패했습니다.\n잠시후에 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!isHeaderShowing) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = Color.BLACK
            }
        }

        refreshAtrcDynamic()
        binding.currentMin = DateTime().minuteOfDay
//        activityView.estiChart.invalidate()
//        setEstiChartData(binding.root.estiChart, binding.atDetail)
    }

    override fun onStart() {
        super.onStart()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            initStatusBarPadding()
//        }
    }

    private fun setListener() {
        activityView.btnFavoriteInContent.setOnClickListener {
            var fav = ""
            if (activityView.btnFavoriteInContent.isSelected) {
                GlobalScope.launch {
                    atRepository.deleteFavAtrc(binding.atItem!!.atCode)
                }
                Toast.makeText(this@TPAttractionActivity, "즐겨찾기에서 해제했습니다.", Toast.LENGTH_SHORT)
                    .show()
                fav = "0"
            } else {
                GlobalScope.launch {
                    atRepository.insertFavAtrc(binding.atItem!!.atCode)
                }
                Toast.makeText(this@TPAttractionActivity, "즐겨찾기에 등록했습니다.", Toast.LENGTH_SHORT)
                    .show()
                fav = "1"
            }

            TPAnalytics.sendClick("ClickAttractionFav")
            val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
            hashMap["atCode"] = atCode
            hashMap["fav"] = fav
            TPAnalytics.sendEvent("ClickAttractionFav", hashMap)
        }

        activityView.btnFavoriteInHeader.setOnClickListener {
            var fav = ""
            if (activityView.btnFavoriteInHeader.isSelected) {
                GlobalScope.launch {
                    atRepository.deleteFavAtrc(binding.atItem!!.atCode)
                }
                Toast.makeText(this@TPAttractionActivity, "즐겨찾기에서 해제했습니다.", Toast.LENGTH_SHORT)
                    .show()
                fav = "0"
            } else {
                GlobalScope.launch {
                    atRepository.insertFavAtrc(binding.atItem!!.atCode)
                }
                Toast.makeText(this@TPAttractionActivity, "즐겨찾기에 등록했습니다.", Toast.LENGTH_SHORT)
                    .show()
                fav = "1"
            }

            TPAnalytics.sendClick("ClickAttractionFavHeader")
            val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
            hashMap["atCode"] = atCode
            hashMap["fav"] = fav
            TPAnalytics.sendEvent("ClickAttractionFavHeader", hashMap)
        }

        activityView.btnBackHeader.setOnClickListener {
            goBack()
        }

        activityView.btnBackHeaderWhite.setOnClickListener {
            goBack()
        }
    }

    private fun initViews() {

        activityView.viewTreeObserver.addOnGlobalLayoutListener {
            mHeaderShowPosition =
                getRelativeTop(activityView.textAtrcNameSub) + activityView.textAtrcNameSub.height
            mArrowHidePosition = getRelativeTop(activityView.thumbnailGradient)
        }

        activityView.atrcHeaderLayout.visibility = View.GONE

        activityView.svAtrcContent.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->

            if (scrollY > mHeaderShowPosition) {
                showHeaderVisible(true)
            } else {
                showHeaderVisible(false)
            }

            if (scrollY > mArrowHidePosition) {
                showArrowVisible(false)
            } else {
                showArrowVisible(true)
            }
        }

        mShowAlarmAdapter = ShowAlarmAdapter()
        binding.rvAlarm.adapter = mShowAlarmAdapter

        initChart()

        val now = DateTime.now()

        if (now.hourOfDay in 0..4) {
            activityView.textChartError.text = "오늘의 예상시간 차트는\n오전 5시 이후부터 확인하실 수 있습니다."
        }
    }

    private fun showArrowVisible(visible: Boolean) {
        if (visible) {
            if (!isArrowShowing) {
                val animation: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    android.R.anim.fade_in
                )
                animation.duration = 200
                animation.fillAfter = true
                activityView.atrcHeaderBtnLayout.startAnimation(animation)
                activityView.atrcHeaderBtnLayout.visibility = View.VISIBLE
                isArrowShowing = !isArrowShowing
            }
        } else {
            if (isArrowShowing) {
                val animation: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    android.R.anim.fade_out
                )
                animation.duration = 100
                animation.fillAfter = true
                activityView.atrcHeaderBtnLayout.startAnimation(animation)
                activityView.atrcHeaderBtnLayout.visibility = View.GONE
                isArrowShowing = !isArrowShowing
            }
        }
    }

    private fun showHeaderVisible(visible: Boolean) {

        if (visible) {
            if (!isHeaderShowing) {
                showAtrcHeaderAnim {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        window.decorView.systemUiVisibility =
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        window.statusBarColor = Color.BLACK
                    }
                }
                isHeaderShowing = true
            }
        } else {
            if (isHeaderShowing) {
                hideAtrcHeaderAnim {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }

                isHeaderShowing = false
            }
        }
    }

    private fun showAtrcHeaderAnim(callback: () -> Unit) {
        val animation: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            android.R.anim.fade_in
        )
        animation.duration = 400
        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
                callback()
            }

            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {}
        })
        activityView.atrcHeaderLayout.startAnimation(animation)
        activityView.statusbarView.startAnimation(animation)
        activityView.atrcHeaderBtnLayout.visibility = View.GONE
        activityView.atrcHeaderLayout.visibility = View.VISIBLE
    }

    private fun hideAtrcHeaderAnim(callback: () -> Unit) {
        val animation: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            android.R.anim.fade_out
        )
        animation.duration = 200
//        animation.fillAfter = true
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                activityView.atrcHeaderLayout.visibility = View.GONE
                callback()
            }
        })
        activityView.atrcHeaderLayout.startAnimation(animation)
        activityView.statusbarView.startAnimation(animation)
        activityView.statusbarView.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun subscribeUi(atCode: String) {

        atViewModel.getOne(atCode).observe(this) {

            tpViewModel.operTime.value?.let { opertime ->
                if (it.tpCode == "KR_EVL") {
                    binding.tpOperTime = opertime[0]
                } else if (it.tpCode == "KR_LTW") {
                    binding.tpOperTime = opertime[1]
                }
            }

            binding.atItem = it
            initDescStyle()

            mShowAlarmAdapter.atItem = it
            showTimeList = it.showtimeList

            showTimeInit = true

            GlobalScope.launch {
                atRepository.deleteAndInsertShowAlarm(atViewModel.getShowAlarmsSync())
            }
        }

        atViewModel.checkIsFavoriteAtrc(atCode).observe(this) {
            activityView.btnFavoriteInContent.isSelected = it
            activityView.btnFavoriteInHeader.isSelected = it
        }

        atViewModel.atrcDetail.observe(this) {
            binding.atDetail = it
            binding.currentMin = DateTime().minuteOfDay
        }

        atViewModel.showAlarms.observe(this) { dbAlarmList ->
            if (dbAlarmList.isNotEmpty()) {
                if (!AlarmService.isRunning) {
                    alarmIntent = Intent(this@TPAttractionActivity, AlarmService::class.java)
                    startService(alarmIntent)
                }
            } else {
                if (alarmIntent != null) {
                    stopService(alarmIntent)
                    alarmIntent = null
                }
            }

            GlobalScope.launch {
                while (!showTimeInit) { }

                val list = mutableListOf<ShowAlarmItem>()

                showTimeList?.forEach { atrcShow ->
                    var checked = false

                    dbAlarmList.forEach dbLoop@{ showAlarmItem ->
                        if (atrcShow.showTime == showAlarmItem.showTime
                            && showAlarmItem.atCode == binding.atItem!!.atCode
                            && showAlarmItem.alarmDate == getTodayString()
                        ) {

                            checked = true
                            list.add(
                                ShowAlarmItem(
                                    getTodayString(),
                                    showAlarmItem.showTime,
                                    showAlarmItem.statCd,
                                    showAlarmItem.validYn,
                                    showAlarmItem.before30min,
                                    showAlarmItem.before15min,
                                    showAlarmItem.before5min,
                                    binding.atItem!!.tpCode,
                                    binding.atItem!!.tpName,
                                    binding.atItem!!.tpNameKR,
                                    binding.atItem!!.atCode,
                                    binding.atItem!!.name,
                                    binding.atItem!!.nameKR,
                                    binding.atItem!!.thumbnailUrl
                                )
                            )
                            return@dbLoop
                        }
                    }

                    if (!checked) {
                        list.add(
                            ShowAlarmItem(
                                getTodayString(),
                                atrcShow.showTime!!,
                                atrcShow.statCd,
                                atrcShow.validYn,
                                false,
                                false,
                                false,
                                binding.atItem!!.tpCode,
                                binding.atItem!!.tpName,
                                binding.atItem!!.tpNameKR,
                                binding.atItem!!.atCode,
                                binding.atItem!!.name,
                                binding.atItem!!.nameKR,
                                binding.atItem!!.thumbnailUrl
                            )
                        )
                    }
                }

                runOnUiThread{
                    atViewModel.showAlarmList.value = list
                }
            }
        }

        atViewModel.showAlarmList.observe(this) {
            mShowAlarmAdapter.submitList(it)
            mShowAlarmAdapter.notifyDataSetChanged()
        }

        GlobalScope.launch {
            atRepository.updateAtrcDetail(atCode) {
                Toast.makeText(this@TPAttractionActivity, "정보 불러오기에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        atViewModel.atrcDetail.value = null
    }


    private fun initDescStyle() {
        val desc = binding.root.textDescription
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (desc.layout != null && desc.layout.getEllipsisCount(desc.lineCount - 1) > 0) {
                    desc.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val indexLast = desc.layout.text.indexOf("…") - 6
                    val layoutText = desc.layout.text.substring(0, indexLast)
                    val showingText = "$layoutText… 더보기"

                    val sp = SpannableStringBuilder(showingText)

                    sp.setSpan(
                        ForegroundColorSpan(
                            ContextCompat.getColor(
                                this@TPAttractionActivity,
                                R.color.highlight_blue2
                            )
                        ),
                        showingText.length - 3,
                        showingText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    desc.text = sp
                }
            }
        }

        binding.root.textDescription.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun clickDesc(fullText: String) {
        val desc = binding.root.textDescription
        desc.ellipsize = null
        desc.maxLines = Integer.MAX_VALUE
        desc.text = fullText
        expand(desc)
        TPAnalytics.sendClick("ClickOpenDesc")
    }

    fun clickEstiDesc() {
        val desc = binding.root.estiDescLayout

        if (binding.estiInfoOpened != true) {
            expand(desc)
            binding.estiInfoOpened = true

            val estiDescTop = activityView.estiDescLayout.y
            val scrlY = activityView.svAtrcContent.scrollY

            if (scrlY < estiDescTop) {
                activityView.svAtrcContent.smoothScrollBy(
                    0,
                    (estiDescTop - scrlY + dp2px(250, this)).toInt()
                )
            }

            TPAnalytics.sendClick("ClickOpenEstiDesc")
        } else {
            collapse(desc)
            binding.estiInfoOpened = false
            TPAnalytics.sendClick("ClickCloseEstiDesc")
        }
    }

    fun clickAnalysis(atItem: Attraction, atrcDetail: AtrcDetail?) {
        val intent = Intent(this, TPChartActivity::class.java)
        intent.putExtra("atCode", atItem.atCode)
        intent.putExtra("name", atItem.nameKR)

        if (atrcDetail != null && !atrcDetail.openTm.isNullOrEmpty() && !atrcDetail.openTm.isNullOrEmpty()) {
            val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
            intent.putExtra(
                "openTm",
                formatter.parseLocalTime(atrcDetail.openTm).millisOfDay.toLong()
            )
            intent.putExtra(
                "closeTm",
                formatter.parseLocalTime(atrcDetail.closeTm).millisOfDay.toLong()
            )
        } else {
            intent.putExtra("openTm", binding.tpOperTime?.startTime?.times(1000))
            intent.putExtra("closeTm", binding.tpOperTime?.endTime?.times(1000))
        }

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )

        TPAnalytics.sendClick("ClickAnalysis")
        val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
        hashMap["atCode"] = atCode
        TPAnalytics.sendEvent("ClickAnalysis", hashMap)
    }

    private fun showAnimation() {
        val mLoadAnimationThumbnail: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            android.R.anim.fade_in
        )
        mLoadAnimationThumbnail.duration = 400
        mLoadAnimationThumbnail.fillAfter = true
        mLoadAnimationThumbnail.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                val mLoadAnimation: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.translate_fade_in_attraction
                )
                mLoadAnimation.duration = 500
                mLoadAnimation.fillAfter = true
                activityView.contentLayout.startAnimation(mLoadAnimation)
                activityView.contentLayout.visibility = View.VISIBLE
            }
        })
        activityView.atrcDetailThumbnailLayout.startAnimation(mLoadAnimationThumbnail)
        activityView.atrcDetailThumbnailLayout.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        goBack()
    }

    private fun goBack() {
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.translate_out)
    }

    private fun expand(v: View) {
        val currentHeight = v.measuredHeight
        val matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            (v.parent as View).width,
            View.MeasureSpec.EXACTLY
        )
        val wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(
            0,
            View.MeasureSpec.UNSPECIFIED
        )
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = v.measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.layoutParams.height = currentHeight
        v.visibility = View.VISIBLE
        val a: Animation = object : Animation() {
            override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation
            ) {
                if (interpolatedTime == 1f) {
                    v.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    v.requestLayout()
                } else {
                    val animHeight = (targetHeight * interpolatedTime).toInt()
                    if (animHeight > currentHeight) {
                        v.layoutParams.height = animHeight
                        v.requestLayout()
                    }
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Expansion speed of 1dp/ms
        a.duration = (targetHeight / v.context.resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    fun collapse(v: View) {
        val initialHeight = v.measuredHeight
        val a: Animation = object : Animation() {
            override fun applyTransformation(
                interpolatedTime: Float,
                t: Transformation?
            ) {
                if (interpolatedTime == 1f) {
                    v.visibility = View.GONE
                } else {
                    v.layoutParams.height =
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    v.requestLayout()
                }
            }

            override fun willChangeBounds(): Boolean {
                return true
            }
        }

        // Collapse speed of 1dp/ms
        a.duration = (initialHeight / v.context.resources.displayMetrics.density).toLong()
        v.startAnimation(a)
    }

    private fun getRelativeTop(myView: View): Int {
        return if (myView.parent === myView.rootView) myView.top else myView.top + getRelativeTop(
            myView.parent as View
        )
    }

    private fun initChart() {
        val chart = binding.root.estiChart

        chart.description.isEnabled = false

        chart.setDrawGridBackground(true)
        chart.setTouchEnabled(false)

        chart.isDragEnabled = false
        chart.setScaleEnabled(true)
        chart.setDrawBorders(false)

        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)

        chart.legend.isEnabled = false

        chart.axisLeft.axisLineWidth = 0f
        chart.axisLeft.axisLineColor = Color.TRANSPARENT
        chart.axisLeft.isEnabled = true
        chart.axisLeft.textSize = 11f
        chart.axisLeft.textColor = getColor(R.color.basicBlack0_5)
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.setDrawLabels(true)
        chart.axisLeft.setDrawAxisLine(true)
        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.gridColor = ContextCompat.getColor(chart.context, R.color.basicBlack0_3solid)
        chart.axisLeft.setDrawGridLinesBehindData(false)
        chart.axisLeft.isGranularityEnabled = true
        chart.axisLeft.granularity = 1f
        chart.axisLeft.axisMaximum = 5f
        chart.axisLeft.setLabelCount(6, true)
        chart.axisLeft.isEnabled = true
        chart.axisLeft.xOffset= -10f

        chart.axisRight.isEnabled = false

        chart.xAxis.isEnabled = true
        chart.xAxis.textSize = 11f
        chart.xAxis.textColor = getColor(R.color.basicBlack0_5)
        chart.xAxis.setDrawLabels(true)
        chart.xAxis.setDrawAxisLine(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawGridLinesBehindData(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.isGranularityEnabled = true
        chart.xAxis.granularity = 1f

        chart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value == 1f){
                    return "여유\n(~30분)"
                } else if (value == 2f){
                    return "보통\n(~60분)"
                } else if (value == 3f){
                    return "약간 혼잡\n(~90분)"
                } else if (value == 4f){
                    return "혼잡\n(~120분)"
                } else if (value == 5f){
                    return "매우 혼잡\n(120분~)"
                } else {
                    return "\n"
                }
            }
        }

        chart.rendererLeftYAxis = CustomYAxisRenderer(
            chart.viewPortHandler,
            chart.axisLeft,
            chart.getTransformer(YAxis.AxisDependency.LEFT)
        )

    }


    inner class CustomYAxisRenderer(
        viewPortHandler: ViewPortHandler?,
        yAxis: YAxis?,
        trans: Transformer?
    ) : YAxisRenderer(viewPortHandler, yAxis, trans) {

        override fun drawYLabels(
            c: Canvas?,
            fixedPosition: Float,
            positions: FloatArray?,
            offset: Float
        ) {
            val from = if (mYAxis.isDrawBottomYLabelEntryEnabled) 0 else 1
            val to =
                if (mYAxis.isDrawTopYLabelEntryEnabled) mYAxis.mEntryCount else mYAxis.mEntryCount - 1
            // draw
            // draw
            for (i in from until to) {
                val text = mYAxis.getFormattedLabel(i).split("\n".toRegex())
                c!!.drawText(
                    text[0],
                    fixedPosition - 80,
                    positions!![i * 2 + 1] + offset - 20,
                    mAxisLabelPaint
                )
                c!!.drawText(
                    text[1],
                    fixedPosition - 80,
                    positions!![i * 2 + 1] + offset + 30,
                    mAxisLabelPaint
                )
            }

        }

    }
}

