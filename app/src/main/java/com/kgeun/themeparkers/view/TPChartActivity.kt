package com.kgeun.themeparkers.view

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPBaseActivity
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.databinding.ActivityChartBinding
import com.kgeun.themeparkers.viewmodels.AttractionViewModel
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_chart.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class TPChartActivity : TPBaseActivity() {
    lateinit var binding : ActivityChartBinding
    lateinit var atCode: String
    var openTm: Long? = null
    var closeTm: Long? = null

    private val atViewModel: AttractionViewModel by viewModel()
    private val atRepository: AttractionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityChartBinding>(
            this,
            com.kgeun.themeparkers.R.layout.activity_chart
        )

        binding.activity = this

        atViewModel.statItem.value = null

        if (intent.hasExtra("atCode")) {
            atCode = intent.getStringExtra("atCode")!!
        }
        if (intent.hasExtra("name")) {
            binding.name = intent.getStringExtra("name")!!
        }

        if (intent.hasExtra("openTm")) {
            openTm = intent.getLongExtra("openTm", 0)
        }
        if (intent.hasExtra("closeTm")) {
            closeTm = intent.getLongExtra("closeTm", 0)
        }

        setListener()
        initViews()
        subscribeUi()

        Handler(Looper.getMainLooper()).postDelayed({
            showAnimation()
        }, 400)

        TPAnalytics.sendView("ViewChart")
    }

    fun setListener() {
        binding.root.historyTypeSelect.onPositionChangedListener =
            SegmentedButtonGroup.OnPositionChangedListener { position ->
                    binding.statItem = null

                    if (position == 0) {
                        GlobalScope.launch {
                            atRepository.updateAttractionsStatistic(atCode)
                        }
                        TPAnalytics.sendClick("ClickChartWeekago")
                    } else if (position == 1) {
                        GlobalScope.launch {
                            atRepository.updateAttractionsStatistic(atCode, "yesterday")
                        }
                        TPAnalytics.sendClick("ClickChartYesterday")
                    }
                }
    }

    fun initViews() {
        val chart = binding.root.atrcChart
        chart.description.isEnabled = false

        chart.setDrawGridBackground(true)
        chart.setTouchEnabled(false)

        chart.isDragEnabled = false
        chart.setScaleEnabled(true)
        chart.setDrawBorders(false)

        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)

        chart.setExtraOffsets(0f, 0f, 0f, 30f)

        chart.legend.isEnabled = true
        chart.legend.textSize = 13f
        chart.legend.textColor = getColor(R.color.basicBlack0_7)
        chart.legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        chart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        chart.legend.xEntrySpace = 20f

        chart.axisLeft.isEnabled = true
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

        chart.axisRight.isEnabled = false

        chart.xAxis.isEnabled = true
        chart.xAxis.textSize = 11f
        chart.xAxis.textColor = getColor(R.color.basicBlack0_5)
        chart.xAxis.setDrawLabels(true)
        chart.xAxis.setDrawAxisLine(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.setDrawGridLinesBehindData(false)
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.isGranularityEnabled = true

        chart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value == 0f) {
                    return ""
                } else {
                    return "${value.toInt()}분"
                }
            }
        }
    }

    fun subscribeUi() {
        GlobalScope.launch {
            atRepository.updateAttractionsStatistic(atCode)
        }

        atViewModel.statItem.observe(this) {
            binding.statItem = it
        }
    }

    fun update() {
        val animSet = AnimationSet(true)
        animSet.interpolator = DecelerateInterpolator()
        animSet.fillAfter = true
        animSet.isFillEnabled = true

        val animRotate = RotateAnimation(
            0.0f, -90.0f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )

        animRotate.duration = 1500
        animRotate.fillAfter = true
        animSet.addAnimation(animRotate)

        binding.root.btnRefresh.startAnimation(animSet)

        GlobalScope.launch {
            atRepository.updateAttractionsStatistic(atCode, binding.statItem?.type) {
                binding.root.btnRefresh.clearAnimation()
            }
        }
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
            }
        })
        binding.root.startAnimation(mLoadAnimationThumbnail)
        binding.root.visibility = View.VISIBLE
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.translate_out)
    }
}