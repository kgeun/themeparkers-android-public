package com.kgeun.themeparkers.custom

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jaygoo.widget.RangeSeekBar
import com.kgeun.themeparkers.BuildConfig
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPMainActivity
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.util.APP_DOWN_URL
import com.kgeun.themeparkers.util.EXTRA_FORCE_FINISH
import com.kgeun.themeparkers.view.TPIntroActivity
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.app_info_dialog_layout.view.*
import org.koin.android.ext.android.inject
import org.koin.core.KoinComponent
import org.koin.core.inject
import com.jaygoo.widget.OnRangeChangedListener as OnRangeChangedListener1


class TPAppInfoDialog() : BottomSheetDialogFragment() {

    var fragmentView: View? = null
    val atrcRepository: AttractionRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        super.onCreate(savedInstanceState)

        TPAnalytics.sendView("ViewAppInfoDialog")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.app_info_dialog_layout, container, false)
        initViews()
        setListener()

        return fragmentView
    }

    private fun initViews() {
        var title = if (BuildConfig.FLAVOR == "qa") {
            getString(R.string.qa_app_name)
        } else {
            getString(R.string.app_name)
        }
        title += " ${BuildConfig.VERSION_NAME}"
        fragmentView?.appTitleText?.text = title

        val remoteConfig = FirebaseRemoteConfig.getInstance()

        try {
            var currentVersion = remoteConfig.getString("current_version")

            var currentVersionNum = currentVersion.trim { it <= ' ' }
            currentVersionNum = currentVersionNum.replace(".", "")

            var curVersion = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName.trim { it <= ' ' }
            curVersion = curVersion.replace(".", "")

            if (Integer.parseInt(curVersion) < Integer.parseInt(currentVersionNum)) {
                fragmentView?.btnUpdate?.visibility = View.VISIBLE
                fragmentView?.btnUpdate?.text = "최신버전 업데이트 ($currentVersion)"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setListener() {
        fragmentView?.btnUpdate?.setOnClickListener {
            val finishIntent = Intent(requireActivity(), TPMainActivity::class.java)
            finishIntent.putExtra(EXTRA_FORCE_FINISH, true)
            finishIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(finishIntent)
            activity?.finish()

            val it = Intent(Intent.ACTION_VIEW)
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            it.data = Uri.parse(APP_DOWN_URL)
            startActivity(it)
            dismiss()
            TPAnalytics.sendClick("ClickUpdateInAppInfo")
        }
        fragmentView?.btnClose?.setOnClickListener {
            dialog?.dismiss()
        }
        fragmentView?.btnIntro?.setOnClickListener {
            val intent = Intent(requireContext(), TPIntroActivity::class.java)
            intent.putExtra("demo", true)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            TPAnalytics.sendClick("ClickIntroDemo")
        }
        fragmentView?.devEmailText?.setOnClickListener {
            val email = Intent(Intent.ACTION_SEND)
            email.setType("plain/Text");
            email.putExtra(Intent.EXTRA_EMAIL, "poisonlab72@gmail.com");
            email.putExtra(Intent.EXTRA_SUBJECT, "<" + getString(R.string.app_name) + " 버그신고 및 건의사항>");
            email.putExtra(Intent.EXTRA_TEXT, "앱 버전 (AppVersion): ${BuildConfig.VERSION_NAME}\n기기명 (Device): ${Build.MODEL}\n안드로이드 OS (Android OS):${Build.VERSION.SDK_INT} ${Build.VERSION.CODENAME}\n내용 (Content):\n");
            email.setType("message/rfc822");
            startActivity(email);
            TPAnalytics.sendClick("ClickEmailToDev")
        }
    }
}