package com.kgeun.themeparkers.adapter

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.ArticleItem
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.YTVideoItem
import com.kgeun.themeparkers.data.AtrcDetail
import com.kgeun.themeparkers.util.dp2px
import com.kgeun.themeparkers.util.drawAtrcStatus
import com.kgeun.themeparkers.util.setWaitTimeVisibility
import com.kgeun.themeparkers.view.TPImageViewerActivity
import kotlinx.android.synthetic.main.activity_attraction.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.unbescape.html.HtmlEscape


@BindingAdapter("atclThumbnail")
fun setAtclThumbnail(view: ImageView, thumbnailUrl: String) {
    val myOptions = RequestOptions()
        .centerCrop()
        .override(view.width, (view.height * 1.5).toInt())

    Glide
        .with(view.context)
        .load(thumbnailUrl)
        .apply(myOptions)
        .transition(withCrossFade(400))
        .into(view)
}

@BindingAdapter("atclAuthorThumbnail")
fun setAtclAuthorThumbnail(view: ImageView, thumbnailUrl: String) {
    val myOptions = RequestOptions()
        .centerCrop()

    Glide
        .with(view.context)
        .load(thumbnailUrl)
        .apply(myOptions)
        .circleCrop()
        .transition(DrawableTransitionOptions.withCrossFade(1000))
        .into(view)
}