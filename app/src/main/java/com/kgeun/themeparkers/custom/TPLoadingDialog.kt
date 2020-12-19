package com.kgeun.themeparkers.custom

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import com.kgeun.themeparkers.R

class TPLoadingDialog(private val mContext: Context) : AppCompatDialog(mContext, R.style.LoadingDialogTheme) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setContentView(R.layout.loading_dialog)

//        window?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(mContext, R.color.whiteTransperent)))

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val option = window?.decorView?.systemUiVisibility
//            window?.decorView?.systemUiVisibility =
//                option!! or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//        }
    }

    override fun show() {
        try {
            super.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

//    override fun show() {
//        fun showAndDim() {
//            super.show()
//
//            window?.attributes?.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
//            window?.attributes?.dimAmount = 0.03f
//        }
//
//        if (mContext is Activity) {
//            try {
//                if (!mContext.isFinishing) {
//                    showAndDim()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//            showAndDim()
//        }
//    }
}