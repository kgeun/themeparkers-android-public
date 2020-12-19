package com.kgeun.themeparkers.util

import android.content.Context
import android.os.PowerManager

object TPPushUtils {
    private val WAKEUP_TAG = TPPushUtils::class.java.simpleName
    private var mWakeLock: PowerManager.WakeLock? = null

    fun acquireWakeLock(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            WAKEUP_TAG
        )
        if (mWakeLock != null) mWakeLock!!.acquire()
    }

    fun releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock!!.release()
            mWakeLock = null
        }
    }
}