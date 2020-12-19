package com.kgeun.themeparkers.custom

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.util.DIALOG_BACKGROUND_DIM_VALUE
import kotlinx.android.synthetic.main.common_alert_dialog_layout.*


class TPAlertDialog(context: Context?) : Dialog(context!!, R.style.DialogTheme),
    View.OnClickListener {
    private var hasPositiveButton = false
    private var hasNegativeButton = false
    private var positiveListener: DialogInterface.OnClickListener? = null
    private var negativeListener: DialogInterface.OnClickListener? = null

//    fun setPositiveButton(
//        text: CharSequence?,
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasPositiveButton = true
//        positiveButton.visibility = View.VISIBLE
//        positiveButton.text = text
//        positiveListener = listener
//        alignButtonDesign()
//    }
//
//    fun setPositiveButton(
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasPositiveButton = true
//        positiveButton.visibility = View.VISIBLE
//        positiveListener = listener
//        alignButtonDesign()
//    }
//
//    fun setPositiveButton(
//        text: CharSequence?,
//        textColor: Int,
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasPositiveButton = true
//        positiveButton.visibility = View.VISIBLE
//        positiveButton.setTextColor(textColor)
//        positiveButton.text = text
//        positiveListener = listener
//        alignButtonDesign()
//    }
//
//    fun setPositiveButton(
//        stringResID: Int,
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasPositiveButton = true
//        positiveButton.visibility = View.VISIBLE
//        positiveButton.setText(stringResID)
//        positiveListener = listener
//        alignButtonDesign()
//    }
//
//    fun setNegativeButton(
//        text: CharSequence?,
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasNegativeButton = true
//        negativeButton.visibility = View.VISIBLE
//        negativeButton.text = text
//        negativeListener = listener
//        alignButtonDesign()
//    }
//
//    fun setNegativeButton(
//        stringResID: Int,
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasNegativeButton = true
//        negativeButton.visibility = View.VISIBLE
//        negativeButton.setText(stringResID)
//        negativeListener = listener
//        alignButtonDesign()
//    }
//
//    fun setNegativeButton(
//        listener: DialogInterface.OnClickListener?
//    ) {
//        hasNegativeButton = true
//        negativeButton.visibility = View.VISIBLE
//        negativeListener = listener
//        alignButtonDesign()
//    }

    fun setSpannableStringMessage(sp: SpannableStringBuilder?) {
        messageTextView.text = sp
    }

    fun setMessage(stringResID: Int) {
        messageTextView.setText(stringResID)
    }

    fun setMessage(message: String) {
        messageTextView.text = message
    }

    fun setMessage(message: Spanned) {
        messageTextView.text = message
    }

    override fun setTitle(stringResID: Int) {
        titleTextView.setText(stringResID)
        super.setTitle(stringResID)
    }

    fun setTitle(message: String) {
        titleTextView.text = message
    }

//    private fun alignButtonDesign() {
//        if (hasPositiveButton && hasNegativeButton) {
//            positiveButton.background = ContextCompat.getDrawable(
//                context,
//                R.drawable.common_popup_dialog_btn_selector_red_right
//            )
//            negativeButton.background = ContextCompat.getDrawable(
//                context,
//                R.drawable.common_popup_dialog_btn_selector_navy_left
//            )
//        } else if (hasPositiveButton) {
//            positiveButton.background =
//                ContextCompat.getDrawable(context, R.drawable.selector_btn_half_corner_orange)
//        } else if (hasNegativeButton) {
//            negativeButton.background =
//                ContextCompat.getDrawable(context, R.drawable.selector_btn_half_corner_navy)
//        }
//    }
//
//    fun reverseButtonColor() {
//        positiveButton.background = ContextCompat.getDrawable(
//            context,
//            R.drawable.common_popup_dialog_btn_selector_navy_right
//        )
//        negativeButton.background = ContextCompat.getDrawable(
//            context,
//            R.drawable.common_popup_dialog_btn_selector_red_left
//        )
//    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.positiveButton -> {
                if (positiveListener != null) {
                    positiveListener!!.onClick(this, 0)
                }
            }
            R.id.negativeButton -> {
                if (negativeListener != null) {
                    negativeListener!!.onClick(this, 0)
                } else {
                    dismiss()
                }
            }
        }
    }

    init {
        val lpWindow = WindowManager.LayoutParams()
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        lpWindow.dimAmount = DIALOG_BACKGROUND_DIM_VALUE
        window!!.attributes = lpWindow
        setContentView(R.layout.common_alert_dialog_layout)
        positiveButton.setOnClickListener(this)
        negativeButton.setOnClickListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val option = window?.decorView?.systemUiVisibility
            window?.decorView?.systemUiVisibility =
                option!! or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}