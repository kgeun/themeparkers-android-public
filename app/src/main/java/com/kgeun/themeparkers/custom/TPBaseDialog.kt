package com.kgeun.themeparkers.custom

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.AttractionRepository
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.app_info_dialog_layout.*
import kotlinx.android.synthetic.main.base_dialog_layout.view.*
import org.koin.android.ext.android.inject

class TPBaseDialog : BottomSheetDialogFragment() {

    var fragmentView: View? = null
    val atrcRepository: AttractionRepository by inject()

    private var hasPositiveBtn = false
    private var hasNegativeBtn = false

    private var positiveListener: (() -> Unit)? = null
    private var negativeListener: (() -> Unit)? = null
    private var posBtnText: String? = null
    private var posBtnTextResId: Int? = null
    private var negBtnText: String? = null
    private var negBtnTextResId: Int? = null
    private var titleTextStr: String? = null
    private var titleTextResId: Int? = null
    private var descText: String? = null
    private var descTextResId: Int? = null

    var onCancelListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
        super.onCreate(savedInstanceState)
        TPAnalytics.sendView("ViewBaseDialog")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.base_dialog_layout, container, false)

        if (positiveListener != null) {
            fragmentView?.positiveBtn?.visibility = View.VISIBLE
            fragmentView?.positiveBtn?.text =
                when {
                    posBtnText != null -> posBtnText
                    posBtnTextResId != null -> activity?.getString(posBtnTextResId!!)
                    else -> ""
                }
            fragmentView?.positiveBtn?.setOnClickListener {
                positiveListener!!()
            }
        }
        if (negativeListener != null) {
            fragmentView?.negativeBtn?.visibility = View.VISIBLE
            fragmentView?.negativeBtn?.text =
                when {
                    negBtnText != null -> negBtnText
                    negBtnTextResId != null -> activity?.getString(negBtnTextResId!!)
                    else -> ""
                }
            fragmentView?.negativeBtn?.setOnClickListener {
                negativeListener!!()
            }
        }
        if (titleTextStr != null) {
            fragmentView?.titleText?.visibility = View.VISIBLE
            fragmentView?.titleText?.text =
                when {
                    titleTextStr != null -> titleTextStr
                    titleTextResId != null -> activity?.getString(titleTextResId!!)
                    else -> ""
                }
        }
        fragmentView?.messageText?.text =
            when {
                descText != null -> descText
                descTextResId != null -> activity?.getString(descTextResId!!)
                else -> ""
            }

        return fragmentView
    }

    fun setPositiveButton(text: CharSequence, listener: () -> Unit) {
        hasPositiveBtn = true
        posBtnText = text.toString()
        positiveListener = listener
    }

    fun setPositiveButton(stringResID: Int, listener: () -> Unit) {
        hasPositiveBtn = true
        posBtnTextResId = stringResID
        positiveListener = listener
    }

    fun setNegativeButton(text: CharSequence, listener: () -> Unit) {
        hasNegativeBtn = true
        negBtnText = text.toString()
        negativeListener= listener
    }

    fun setNegativeButton(stringResID: Int, listener: () -> Unit) {
        hasNegativeBtn = true
        negBtnTextResId = stringResID
        negativeListener= listener
    }

    fun setTitle(text: CharSequence?) {
        titleTextStr = text?.let { it.toString() }
    }

    fun setTitle(stringResID: Int) {
        titleTextResId = stringResID
    }

    fun setMessage(text: CharSequence) {
        descText = text?.let {it.toString()}
    }

    fun setMessage(stringResID: Int) {
        descTextResId = stringResID
    }

    override fun onCancel(dialog: DialogInterface) {
        onCancelListener?.let { it() }
        super.onCancel(dialog)
    }
}