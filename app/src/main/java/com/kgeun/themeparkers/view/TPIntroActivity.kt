package com.kgeun.themeparkers.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.view.WindowManager.LayoutParams.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.flaviofaria.kenburnsview.KenBurnsView
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.TPBaseActivity
import com.kgeun.themeparkers.TPMainActivity
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.data.ThemeparkRepository
import com.kgeun.themeparkers.databinding.ActivityIntroBinding
import com.kgeun.themeparkers.viewmodels.AttractionViewModel
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_intro.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class TPIntroActivity: TPBaseActivity() {

    lateinit var binding: ActivityIntroBinding
    lateinit var activityView: View

    private val tpViewModel: ThemeparkViewModel by viewModel()
    private val atViewModel: AttractionViewModel by viewModel()

    private val atRepository: AttractionRepository by inject()
    private val tpRepository: ThemeparkRepository by inject()

    var isLoadingFinished = false
    var isViewTimeFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityIntroBinding>(
            this,
            R.layout.activity_intro
        )
        activityView = binding.root
        window.setFlags(FLAG_LAYOUT_NO_LIMITS, FLAG_LAYOUT_NO_LIMITS)

        if (intent.hasExtra("demo") && intent.getBooleanExtra("demo", false)) {
            initViews(demo = true)
            TPAnalytics.sendView("ViewIntroDemo")
        } else {
            initViews(demo = false)
            startDownload()
            TPAnalytics.sendView("ViewIntro")
        }
    }

    fun loadingFinish(b1: Boolean, b2: Boolean, b3: Boolean, b4: Boolean) {
        if (b1 && b2 && b3 && b4) {
            if (isViewTimeFinished) {
                startNextActivity()
            } else {
                isLoadingFinished = true
            }
        }
    }

    fun finishWithError() {
        Toast.makeText(this@TPIntroActivity, "정보 불러오기에 실패했습니다. 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
        finish()
    }

    fun startDownload() {
        var b1 = false
        var b2 = false
        var b3 = false
        var b4 = false

        GlobalScope.launch {
            atRepository.updateAttractions() { tpCode ->
                if (tpCode == "NEWS") {
                    b1 = true
                    loadingFinish(b1, b2, b3, b4)
                }
                if (tpCode == "KR_EVL") {
                    b2 = true
                    loadingFinish(b1, b2, b3, b4)
                }
                if (tpCode == "KR_LTW") {
                    b3 = true
                    loadingFinish(b1, b2, b3, b4)
                }
                if (tpCode == "ERROR") {
                    finishWithError()
                }
            }

            tpRepository.updateThemeparks() { stat ->
                if (stat == 0) {
                    b4 = true
                    loadingFinish(b1, b2, b3, b4)
                } else if (stat == -1) {
                    finishWithError()
                }
            }
        }
        GlobalScope.launch {
            tpRepository.updateOperTimeInfo()
        }
    }

    fun initViews(demo: Boolean) {
        val random = Random()
        val num = random.nextInt(8) // 0..5

        val titleImage = when (num) {
            0 -> {
                binding.titleAreaName = "디즈니랜드 호텔, 디즈니랜드 파리"
                R.drawable.disney_splash_2
            }
            1 -> {
                binding.titleAreaName = "매직 킹덤, 월트 디즈니 월드"
                R.drawable.disney_splash_1
            }
            2 -> {
                binding.titleAreaName = "도쿄 디즈니랜드"
                R.drawable.splash_3
            }
            3 -> {
                binding.titleAreaName = "유니버설 스튜디오 플로리다"
                R.drawable.splash_4
            }
            4 -> {
                binding.titleAreaName = "매직 캐슬, 롯데월드 매직아일랜드"
                R.drawable.splash_5
            }
            5 -> {
                binding.titleAreaName = "T 익스프레스, 에버랜드"
                R.drawable.splash_6
            }
            6 -> {
                binding.titleAreaName = "상하이 디즈니랜드"
                R.drawable.splash_7
            }
            else -> {
                binding.titleAreaName = "홍콩 디즈니랜드"
                R.drawable.splash_8
            }
        }

        activityView.splashImage.setTransitionListener(object : KenBurnsView.TransitionListener {
            override fun onTransitionStart(transition: com.flaviofaria.kenburnsview.Transition?) {
                activityView.splashImage.visibility = View.VISIBLE
                activityView.titleGreyLayer.visibility = View.VISIBLE
                val myFadeInAnimation: Animation = AnimationUtils.loadAnimation(this@TPIntroActivity, R.anim.fade_in)
                activityView.splashImage.startAnimation(myFadeInAnimation)
                activityView.titleGreyLayer.startAnimation(myFadeInAnimation)
            }

            override fun onTransitionEnd(transition: com.flaviofaria.kenburnsview.Transition?) {}
        })

        Glide
            .with(this)
            .load(titleImage)
            .into(activityView.splashImage)

        if (!demo) {
            atViewModel.isLoading.value = true

            Handler(Looper.getMainLooper()).postDelayed({
                if (isLoadingFinished) {
                    startNextActivity()
                } else {
                    isViewTimeFinished = true
                }
            }, 1000)
        }
    }

    fun startNextActivity() {
        activityView.splashImage.pause()

        val intent = Intent(binding.root.context, TPMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0,0)
    }
}