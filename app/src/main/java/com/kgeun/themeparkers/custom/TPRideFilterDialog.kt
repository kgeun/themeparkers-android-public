package com.kgeun.themeparkers.custom

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jaygoo.widget.RangeSeekBar
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.data.RideFilterItem
import com.kgeun.themeparkers.data.ThemeparkRepository
import com.kgeun.themeparkers.util.RIDE_ALL
import com.kgeun.themeparkers.util.RIDE_UNDER_30MIN
import com.kgeun.themeparkers.util.checkShowWaitTime
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.ride_filter_dialog_layout.*
import kotlinx.android.synthetic.main.ride_filter_dialog_layout.view.*
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject
import com.jaygoo.widget.OnRangeChangedListener as OnRangeChangedListener1


class TPRideFilterDialog(val tpCode: String) : BottomSheetDialogFragment() {

    var fragmentView: View? = null
    var filterItem: RideFilterItem? = null

    val atrcRepository: AttractionRepository by inject()
    val tpRepository: ThemeparkRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        super.onCreate(savedInstanceState)

        TPAnalytics.sendView("ViewRideFilterDialog")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.ride_filter_dialog_layout, container, false)
        initViews()
        setListener()

        return fragmentView
    }

    private fun initViews() {
        if (filterItem!!.height == -1) {
            fragmentView?.heightSeekbar!!.setProgress(152f)
            setHeightbar(fragmentView?.heightSeekbar!!, 152)
        } else {
            fragmentView?.heightSeekbar!!.setProgress(filterItem!!.height.toFloat())
            setHeightbar(fragmentView?.heightSeekbar!!, filterItem!!.height)
        }

        fragmentView?.attractionStatusSeekBar!!.setProgress(filterItem!!.statusMode.toFloat())
        setStepView(fragmentView?.attractionStatusSeekBar!!, filterItem!!.statusMode)

        fragmentView?.attractionStatusSeekBar?.tickMarkTextArray = arrayOf("30분 이하", "60분 이하", "운행 중", "전체")

        val tpCode = tpRepository.currentThemeparkLiveData.value?.tpCode

        if (!tpCode.isNullOrEmpty()) {
            if (checkShowWaitTime(tpCode)) {
                fragmentView?.waittimeFilterLayout?.visibility = View.VISIBLE
            } else {
                fragmentView?.waittimeFilterLayout?.visibility = View.GONE
            }
        }
    }

    private fun setListener() {
        fragmentView?.heightSeekbar?.setOnRangeChangedListener(object : com.jaygoo.widget.OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                setHeightbar(view!!, leftValue.toInt())
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) { }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) { }
        })

        fragmentView?.attractionStatusSeekBar?.setOnRangeChangedListener(object : com.jaygoo.widget.OnRangeChangedListener {
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean
            ) {
                setStepView(view!!, leftValue.toInt())
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) { }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) { }
        })

        fragmentView?.btnSave!!.setOnClickListener {
            filterItem?.let { item ->
                var currentValue: MutableMap<String, RideFilterItem>? = atrcRepository.rideFilterLiveData.value

                if (currentValue == null) {
                    currentValue = mutableMapOf()
                }

                currentValue.put(tpCode, item)

                atrcRepository.rideFilterLiveData.value = currentValue
            }
            dialog?.dismiss()
        }

        fragmentView?.btnClear!!.setOnClickListener {
            fragmentView?.heightSeekbar?.setProgress(152.toFloat())
            setHeightbar(fragmentView?.heightSeekbar!!, 152)

            fragmentView?.attractionStatusSeekBar?.setProgress(RIDE_ALL.toFloat())
            setStepView(fragmentView?.attractionStatusSeekBar!!, RIDE_ALL)
        }
    }

    private fun setHeightbar(seekbar: RangeSeekBar, height: Int) {
        if (height == 152) {
            seekbar.setIndicatorText("설정 안함")
            seekbar.setIndicatorTextStringFormat("%s")
            filterItem?.height = -1
        } else if (height == 151) {
            seekbar.setIndicatorText("151cm 이상")
            seekbar.setIndicatorTextStringFormat("%s")
            filterItem?.height = height
        } else {
            seekbar.setIndicatorText("")
            seekbar.setIndicatorTextDecimalFormat("0")
            seekbar.setIndicatorTextStringFormat("%scm")
            filterItem?.height = height
        }
    }

    private fun setStepView(seekbar: RangeSeekBar, step: Int) {
        val stepDrawble: ArrayList<Int> = arrayListOf()

        for ( i in RIDE_UNDER_30MIN..RIDE_ALL ) {
            if ( i <= step) {
                stepDrawble.add(R.drawable.step_icon_selected)
            } else {
                stepDrawble.add(R.drawable.step_icon)
            }
        }

        seekbar.setStepsDrawable(stepDrawble)
        filterItem?.statusMode = step
    }
}