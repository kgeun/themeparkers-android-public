package com.kgeun.themeparkers

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kgeun.themeparkers.custom.TPAppInfoDialog
import com.kgeun.themeparkers.custom.TPBaseDialog
import com.kgeun.themeparkers.custom.TPLoadingDialog
import com.kgeun.themeparkers.util.APP_DOWN_URL
import com.kgeun.themeparkers.util.EXTRA_FORCE_FINISH
import com.nmp.studygeto.analytics.TPAnalytics
import org.joda.time.DateTime
import org.json.JSONObject

open class TPBaseActivity() : AppCompatActivity() {

    protected lateinit var mLoadingDialog: TPLoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLoadingDialog = TPLoadingDialog(this)

        val view = window.decorView

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 23 버전 이상일 때 상태바 하얀 색상에 회색 아이콘 색상을 설정
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 21 버전 이상일 때
            window.statusBarColor = Color.BLACK
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (javaClass.simpleName != "TPIntroActivity") {
            checkUpdateRemoteConfig()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initStatusBarPadding()
        }
    }

    private fun initStatusBarPadding() {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        var statusbarHeight = resources.getDimensionPixelSize(R.dimen.statusbar_height)
        if (resourceId > 0) {
            statusbarHeight = resources.getDimensionPixelSize(resourceId)
        }

        val statusbarView : View?  = findViewById<View>(R.id.statusbarView)
        statusbarView?.visibility = View.VISIBLE
        statusbarView?.layoutParams?.height = statusbarHeight

        val statusbarView2 : View?  = findViewById<View>(R.id.statusbarView2)
        statusbarView2?.visibility = View.VISIBLE
        statusbarView2?.layoutParams?.height = statusbarHeight
    }

    fun Int.dp2px(context: Context) :Int {
        val scale = context.resources.displayMetrics.density
        return (this * scale).toInt()
    }

    fun showLoadingDialog() {
        if (mLoadingDialog != null && !mLoadingDialog.isShowing) {
            try {
                mLoadingDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing) {
            try {
                mLoadingDialog.dismiss()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkUpdateRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetchAndActivate()

        try {
            var forceUpdateVersion = remoteConfig.getString("force_update_version")

            forceUpdateVersion = forceUpdateVersion.trim { it <= ' ' }
            forceUpdateVersion = forceUpdateVersion.replace(".", "")

            var curVersion = packageManager.getPackageInfo(packageName, 0).versionName.trim { it <= ' ' }
            curVersion = curVersion.replace(".", "")

            if (Integer.parseInt(curVersion) < Integer.parseInt(forceUpdateVersion)) {
                // 필수 업데이트 진행
                val alert = TPBaseDialog()
                alert.isCancelable = false
                alert.setMessage(R.string.update_desc)
                alert.setTitle("업데이트 안내")
                alert.setPositiveButton(R.string.update_ok) {
                    val finishIntent = Intent(this, TPMainActivity::class.java)
                    finishIntent.putExtra(EXTRA_FORCE_FINISH, true)
                    finishIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(finishIntent)
                    finish()

                    val it = Intent(Intent.ACTION_VIEW)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.data = Uri.parse(APP_DOWN_URL)
                    startActivity(it)
                    alert.dismiss()

                    TPAnalytics.sendClick("ClickForceUpdate")
                }
                alert.setNegativeButton(R.string.update_cancel) {
                    alert.dismiss()

                    when (javaClass.simpleName) {
                        "TPMainActivity" -> {
                            finish()
                        }
                        else -> {
                            val finishIntent = Intent(this, TPMainActivity::class.java)
                            finishIntent.putExtra(EXTRA_FORCE_FINISH, true)
                            finishIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(finishIntent)
                            finish()
                            overridePendingTransition(0,0)
                        }
                    }

                    TPAnalytics.sendClick("CancelForceUpdate")
                }
                alert.show(supportFragmentManager, "ForceUpdateDialog")

                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            var curVersion = packageManager.getPackageInfo(packageName, 0).versionName.trim { it <= ' ' }
            curVersion = curVersion.replace(".", "")

            // 선택 업데이트 여부 확인
            var optionUpdateInfoString = remoteConfig.getString("option_update_version")

            val optionUpdateInfo = JSONObject(optionUpdateInfoString)
            val optionUpdateText = optionUpdateInfo.getString("text")
            var optionUpdateVersion = optionUpdateInfo.getString("version").trim { it <= ' ' }

            optionUpdateVersion = optionUpdateVersion.replace(".", "")

            if (Integer.parseInt(curVersion) < Integer.parseInt(optionUpdateVersion)
                && DateTime.now().dayOfYear != TPPrefCtl.getDayOfYear()
            ) {

                val alert = TPBaseDialog()

                alert.isCancelable = false
                alert.setTitle("업데이트 안내")
                alert.setMessage(optionUpdateText)
                alert.setPositiveButton(R.string.update_ok) {
                    val finishIntent = Intent(this, TPMainActivity::class.java)
                    finishIntent.putExtra(EXTRA_FORCE_FINISH, true)
                    finishIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(finishIntent)
                    finish()

                    val it = Intent(Intent.ACTION_VIEW)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.data = Uri.parse(APP_DOWN_URL)
                    startActivity(it)
                    alert.dismiss()

                    TPAnalytics.sendClick("ClickOptionUpdate")
                    alert.dismiss()
                }
                alert.setNegativeButton(R.string.update_no_more_today) {
                    TPPrefCtl.setDayOfYear(DateTime.now().dayOfYear().get())
                    TPAnalytics.sendClick("ClickNoMoreUpdateToday")
                    alert.dismiss()
                }
                alert.onCancelListener = {
                    TPPrefCtl.setDayOfYear(DateTime.now().dayOfYear().get())
                    TPAnalytics.sendClick("CancelNoMoreUpdateToday")
                    alert.dismiss()
                }
                alert.show(supportFragmentManager, "OptionUpdateDialog")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}