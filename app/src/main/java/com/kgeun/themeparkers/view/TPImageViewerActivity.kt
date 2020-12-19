package com.kgeun.themeparkers.view

import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPBaseActivity
import com.kgeun.themeparkers.adapter.mapLeft
import com.kgeun.themeparkers.adapter.mapTop
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_attraction.view.*
import kotlinx.android.synthetic.main.activity_image_viewer.*

class TPImageViewerActivity : TPBaseActivity() {

    private var imageUrl: String? = null
    private var isMapMarker: Boolean = false

    var bLeftMargin: Int = 0
    var bTopMargin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewer)

        initData()
        initViews()

        Handler(Looper.getMainLooper()).postDelayed({
            showAnimation()
        }, 400)

        TPAnalytics.sendView("ViewImageViewer")
    }

    private fun initViews() {
        backBtn.setOnClickListener { finish() }

        imageView.setOnMatrixChangeListener { rectf: RectF ->
            if (isMapMarker) {
                val mapWidth = rectf.width()
                val mapHeight = rectf.height()

                val markerLeft = mapWidth * mapLeft
                val markerTop = mapHeight * mapTop

                ivMapMarker.x = rectf.left + markerLeft.toFloat()
                ivMapMarker.y = rectf.top + markerTop.toFloat()

                ivMapMarker.visibility = View.VISIBLE
            } else {
                ivMapMarker.visibility = View.GONE
            }
        }
    }

    private fun initData() {
        if (intent.hasExtra("imageUrl")) {
            imageUrl = intent.getStringExtra("imageUrl")
            isMapMarker = intent.getBooleanExtra("isMapMarker", false)

            if (!imageUrl.isNullOrEmpty()) {
                Glide
                    .with(this)
                    .load(imageUrl)
                    .into(imageView)
            }
        }
    }

    private fun showAnimation() {
        val mLoadAnimationThumbnail: Animation = AnimationUtils.loadAnimation(
            applicationContext,
            android.R.anim.fade_in
        )
        mLoadAnimationThumbnail.duration = 400
        mLoadAnimationThumbnail.fillAfter = true
        mLoadAnimationThumbnail.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                val mLoadAnimation: Animation = AnimationUtils.loadAnimation(
                    applicationContext,
                    R.anim.translate_fade_in_attraction
                )
                mLoadAnimation.duration = 500
                mLoadAnimation.fillAfter = true
            }
        })
        fullLayout.startAnimation(mLoadAnimationThumbnail)
        fullLayout.visibility = View.VISIBLE
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.translate_out)
    }
}

