package com.kgeun.themeparkers.adapter

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPMainActivity
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.OperTime
import com.kgeun.themeparkers.data.Themepark
import com.kgeun.themeparkers.databinding.ListItemTopBtnBinding
import com.kgeun.themeparkers.util.*
import com.kgeun.themeparkers.viewmodels.AttractionViewModel
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_attraction.view.*
import kotlinx.android.synthetic.main.list_item_opertime.view.*
import kotlinx.android.synthetic.main.list_item_top_btn.view.*
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject


inline fun <reified T> getKoinInstance(): T {
    return object : KoinComponent {
        val value: T by inject()
    }.value
}

@BindingAdapter(value = ["list", "setActivity"], requireAll = true)
fun showThemeparkList(layout: LinearLayout, tmpks: List<Themepark>?, activity: TPMainActivity?) {
    if (tmpks == null || activity == null) {
        return
    }

    layout.removeAllViews()

    val tpViewModel: ThemeparkViewModel = getKoinInstance()
    val atViewModel: AttractionViewModel = getKoinInstance()

    val tpBinding =
        ListItemTopBtnBinding.inflate(LayoutInflater.from(layout.context), layout, false).apply {
            val home = Themepark(0, "News", "뉴스", "NEWS", "")

            tpItem = home

            tpViewModel.currentThemepark.value = tpItem
            selected = true

            root.setOnClickListener {
                tpViewModel.currentThemepark.value = home
            }
        }
    layout.addView(tpBinding.root)
    tpBinding.root.isClickable = false

    tmpks?.forEachIndexed() { index, tp ->
        val tpBinding =
            ListItemTopBtnBinding.inflate(LayoutInflater.from(layout.context), layout, false).apply {
                tpItem = tp

                root.setOnClickListener {
                    tpViewModel.currentThemepark.value = tp
                }
            }
        tpBinding.root.isClickable = false
        layout.addView(tpBinding.root)
    }
}

@BindingAdapter("setCurrentThemepark")
fun setCurrentThmepark(layout: LinearLayout, tp: Themepark?) {
    if (tp == null) {
        return
    }

    for (i in 0 until layout.childCount) {
        val view = layout.getChildAt(i)
        val btnBinding = DataBindingUtil.getBinding<ListItemTopBtnBinding>(view)

        btnBinding?.selected = btnBinding?.tpItem?.tpCode == tp.tpCode
    }
}

@BindingAdapter("selected")
fun setTPTitleSelected(layout: ConstraintLayout, selected: Boolean) {
    if (selected) {
        layout.textTPName.setTextColor(ContextCompat.getColor(layout.context, R.color.basicBlack))
        layout.imageTPSelector.visibility = View.VISIBLE
    } else {
        layout.textTPName.setTextColor(ContextCompat.getColor(layout.context, R.color.disabled1))
        layout.imageTPSelector.visibility = View.GONE
    }
}


@BindingAdapter("loadUrl")
fun setImageUrl(view: ImageView, url: String?) {

    val myOptions = RequestOptions()
        .centerCrop()
        .override(dp2px(80, view.context), dp2px(130, view.context))

    Glide
        .with(view.context)
        .asBitmap()
        .load(url)
        .placeholder(R.drawable.image_placeholder)
        .apply(myOptions)
        .transition(withCrossFade(200))
        .into(view)
}

@BindingAdapter("isLockTmpks")
fun isLockTmpks(viewGroup: ViewGroup, loading: Boolean?) {
    if (loading == null) {
        viewGroup.children.forEach {
            it.isClickable = false
            it.isEnabled = false
        }
        return
    }

    viewGroup.children.forEach {
        it.isClickable = !loading
        it.isEnabled = !loading
    }

//    viewGroup.isClickable =
}


@BindingAdapter("operStatus")
fun setOperStatus(view: TextView, operTimeItem: OperTime) {
    val now = DateTime.now().secondOfDay().get()

    val parent = view.parent as ViewGroup
    val grandParent = parent.parent as ViewGroup
    if ( operTimeItem.startTime < now &&
                now < operTimeItem.endTime ) {

        grandParent.setBackgroundResource(R.drawable.bg_translucent_highlight_green)
        parent.textOperTimeStatus.setTextColor(
            ContextCompat.getColor(
                view.context,
                R.color.highlight_green
            )
        )
        view.setTextColor(ContextCompat.getColor(view.context, R.color.highlight_green))
        view.text = "현재 운영중"
    } else {
        grandParent.setBackgroundResource(R.drawable.bg_translucent_highlight1)
        parent.textOperTimeStatus.setTextColor(
            ContextCompat.getColor(
                view.context,
                R.color.highlight1
            )
        )
        view.setTextColor(ContextCompat.getColor(view.context, R.color.highlight1))
        view.text = "운영시간이 끝났습니다."
    }
}

@BindingAdapter("calenderUrl")
fun setCalenderUrl(viewGroup: ViewGroup, url: String) {
    viewGroup.setOnClickListener {
        val intentBuilder = CustomTabsIntent.Builder()

        intentBuilder.setToolbarColor(
            ContextCompat.getColor(
                viewGroup.context,
                android.R.color.white
            )
        )
        intentBuilder.setSecondaryToolbarColor(
            ContextCompat.getColor(
                viewGroup.context,
                android.R.color.white
            )
        )
        intentBuilder.setShowTitle(true)
        intentBuilder.enableUrlBarHiding()

        try {
            val customTabsIntent = intentBuilder.build()
            customTabsIntent.intent.setPackage("com.android.chrome")
            customTabsIntent.launchUrl(viewGroup.context, Uri.parse(url))
        } catch (e: java.lang.Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                viewGroup.context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}

@BindingAdapter("atrcStatus")
fun setAtrcStatus(viewGroup: ConstraintLayout, atItem: Attraction) {

    // 상태 설정
    hideAllStatus(viewGroup)

    if (checkShowMainWaitTime(atItem.tpCode) && checkShowWaitTime(atItem.tpCode)) {
        drawAtrcStatus(atItem, viewGroup, "MAIN")
    } else {
        drawAtrcEsti(atItem, viewGroup, "MAIN")
    }

    // 이름 노출 설정
    if (TextUtils.isEmpty(atItem.areaKR)) {
        viewGroup.textAtrcArea.setCompoundDrawables(null, null, null, null)
        viewGroup.textAtrcArea.text = atItem.name
        val lp = viewGroup.textAtrcArea.layoutParams as ViewGroup.MarginLayoutParams
        lp.leftMargin = 0
        viewGroup.textAtrcArea.layoutParams = lp
        viewGroup.textAtrcArea.setTextColor(
            ContextCompat.getColor(
                viewGroup.context,
                R.color.disabled1
            )
        )
    } else {
        viewGroup.textAtrcArea.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.icon_location_pin_small,
            0,
            0,
            0
        )
        viewGroup.textAtrcArea.compoundDrawablePadding = dp2px(5, viewGroup.context)
        val lp = viewGroup.textAtrcArea.layoutParams as ViewGroup.MarginLayoutParams
        lp.leftMargin = dp2px(2, viewGroup.context)
        viewGroup.textAtrcArea.layoutParams = lp
        viewGroup.textAtrcArea.text = atItem.areaKR
        viewGroup.textAtrcArea.setTextColor(
            ContextCompat.getColor(
                viewGroup.context,
                R.color.disabled2
            )
        )
    }

    // 쇼 시간 노출 설정
    var timeText = ""

    if (atItem.kindCode == "SHOW") {
        timeText = atItem.showTimeParsedString
    }

    if (timeText.isNotEmpty()) {
        viewGroup.textTimeInfo.visibility = View.VISIBLE
        viewGroup.textTimeInfo.text = timeText.trim()
    } else {
        viewGroup.textTimeInfo.visibility = View.GONE
    }
}

@BindingAdapter("setArticleClick")
fun setArticleClick(viewGroup: ViewGroup, url: String?) {
    viewGroup.setOnClickListener {
        val intentBuilder = CustomTabsIntent.Builder()

        intentBuilder.setToolbarColor(
            ContextCompat.getColor(
                viewGroup.context,
                android.R.color.white
            )
        )
        intentBuilder.setSecondaryToolbarColor(
            ContextCompat.getColor(
                viewGroup.context,
                android.R.color.white
            )
        )
        intentBuilder.setShowTitle(true)
        intentBuilder.enableUrlBarHiding()

        try {
            val customTabsIntent = intentBuilder.build()
            customTabsIntent.intent.setPackage("com.android.chrome")
            customTabsIntent.launchUrl(viewGroup.context, Uri.parse(url))
        } catch (e: java.lang.Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                viewGroup.context.startActivity(intent)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        TPAnalytics.sendClick("ClickHomeArticle")
    }
}