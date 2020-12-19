package com.kgeun.themeparkers.view

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kgeun.themeparkers.TPBaseActivity
import com.kgeun.themeparkers.TPMainActivity
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.ShowAlarmItem
import com.kgeun.themeparkers.databinding.ActivityAlarmBinding
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_alarm.*


class TPAlarmActivity : TPBaseActivity() {
    lateinit var binding : ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        turnScreenOnAndKeyguardOff()

        binding = DataBindingUtil.setContentView<ActivityAlarmBinding>(
            this,
            com.kgeun.themeparkers.R.layout.activity_alarm
        )

        setListener()
        initViews()
        TPAnalytics.sendView("ViewAlarm")
    }

    fun turnScreenOnAndKeyguardOff() {
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                keyguardManager.requestDismissKeyguard(this, null)
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.O -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                keyguardManager.requestDismissKeyguard(this, null)
            }
            else -> {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
                window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setListener() {
        btnClose.setOnClickListener {
            finish()
        }

        btnOpen.setOnClickListener {
            val intent = Intent(this@TPAlarmActivity, TPMainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            intent.putExtra("open_atrc", true)
            intent.putExtra(TPAttractionActivity.EXTRA_KEY_AT_CODE, binding.atItem?.atCode)
            intent.putExtra(TPAttractionActivity.EXTRA_KEY_KIND_CODE, binding.atItem?.kindCode)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun initViews() {
        try {
            val hashMap: MutableMap<String, String> = hashMapOf<String, String>()

            intent.getStringExtra("showAlarmItem")?.let {
                val saType = object : TypeToken<ShowAlarmItem>() {}.type
                val alarmItem = Gson().fromJson<ShowAlarmItem>(it, saType)
                binding.alarmItem = alarmItem
            }

            intent.getStringExtra("atItem")?.let {
                val saType = object : TypeToken<Attraction>() {}.type
                val atItem = Gson().fromJson<Attraction>(it, saType)
                binding.atItem = atItem
                hashMap["atCode"] = atItem.atCode
            }

            binding.beforeMin = intent.getIntExtra("beforeMin", 0)

            TPAnalytics.sendEvent("ViewAlarm", hashMap)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}