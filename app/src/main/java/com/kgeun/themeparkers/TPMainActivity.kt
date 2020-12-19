package com.kgeun.themeparkers

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import androidx.viewpager2.widget.ViewPager2
import com.kgeun.themeparkers.adapter.HomePagerRecyclerAdapter
import com.kgeun.themeparkers.custom.TPAppInfoDialog
import com.kgeun.themeparkers.custom.TPRideFilterDialog
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.data.OperTime
import com.kgeun.themeparkers.data.Themepark
import com.kgeun.themeparkers.data.ThemeparkRepository
import com.kgeun.themeparkers.databinding.ActivityMainBinding
import com.kgeun.themeparkers.databinding.ListItemTopBtnBinding
import com.kgeun.themeparkers.network.ArticleResult
import com.kgeun.themeparkers.util.EXTRA_FORCE_FINISH
import com.kgeun.themeparkers.view.TPAttractionActivity
import com.kgeun.themeparkers.viewmodels.AttractionViewModel
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class TPMainActivity : TPBaseActivity() {
    lateinit var binding :ActivityMainBinding
    lateinit var activityView :View

    private val tpViewModel: ThemeparkViewModel by viewModel()
    private val atViewModel: AttractionViewModel by viewModel()

    private val atRepository: AttractionRepository by inject()
    private val tpRepository: ThemeparkRepository by inject()

    lateinit var pagerAdapter: HomePagerRecyclerAdapter

    private var atInit = true
    private var alarmIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(
            R.anim.fade_in,
            R.anim.fade_out
        )

        if (intent != null) {
            if (intent.hasExtra(EXTRA_FORCE_FINISH)) {
                if (intent.getBooleanExtra(EXTRA_FORCE_FINISH, false)) {
                    finish()
                    return
                }
            }
        }

        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        activityView = binding.root
        binding.activity = this
        pagerAdapter = HomePagerRecyclerAdapter(activityView as ViewGroup)

        if (intent.hasExtra("open_atrc") && intent.getBooleanExtra("open_atrc", false)) {
            val openIntent = Intent(binding.root.context, TPAttractionActivity::class.java)

            openIntent.putExtra(
                TPAttractionActivity.EXTRA_KEY_AT_CODE, intent.getStringExtra(
                    TPAttractionActivity.EXTRA_KEY_AT_CODE
                )
            )
            openIntent.putExtra(
                TPAttractionActivity.EXTRA_KEY_KIND_CODE, intent.getStringExtra(
                    TPAttractionActivity.EXTRA_KEY_KIND_CODE
                )
            )
            openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(openIntent)
            overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            )
        }

        activityView.visibility = View.GONE
        activityView.mainPager.visibility = View.GONE
        binding.nowLoading = true
        atViewModel.isLoading.value = true
        tpViewModel.currentThemepark.value = Themepark(0, "News", "뉴스", "NEWS", "")

        initApp()
        initViews()

        TPAnalytics.sendView("ViewMain")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("articleRes", atViewModel.articleRes.value)
        outState.putSerializable("operTime", ArrayList(tpViewModel.operTime.value?.toMutableList() as MutableCollection<OperTime>))
        
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        atViewModel.articleRes.postValue(savedInstanceState.getSerializable("articleRes") as ArticleResult)
        tpViewModel.operTime.postValue(savedInstanceState.getSerializable("operTime") as List<OperTime>)
    }

    fun showAnimation() {

        val mLoadAnimationThumbnail: Animation = AnimationUtils.loadAnimation(
            binding.root.context,
            android.R.anim.fade_in
        )
        mLoadAnimationThumbnail.duration = 400
        mLoadAnimationThumbnail.fillAfter = true
        mLoadAnimationThumbnail.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                atInit = false
                val mLoadAnimation: Animation = AnimationUtils.loadAnimation(
                    binding.root.context,
                    R.anim.translate_fade_in_attraction
                )
                mLoadAnimation.duration = 1000
                binding.mainPager.startAnimation(mLoadAnimation)
                activityView.mainPager.visibility = View.VISIBLE
                atViewModel.isLoading.value = false
            }
        })
        if (!mLoadAnimationThumbnail.hasStarted()) {
            activityView.startAnimation(mLoadAnimationThumbnail)
        }
        activityView.visibility = View.VISIBLE
    }

    private fun initApp() {
        GlobalScope.launch {
            atRepository.removeExpiredAlarms {
                stopService(Intent(this@TPMainActivity, AlarmService::class.java))
            }
        }
    }

    private fun subscribeUi() {
        tpViewModel.themeparks.observe(this) {
            binding.themeparks = it
        }

        atViewModel.rideFilter.observe(this) {
            pagerAdapter.rideFilters = it
        }

        tpViewModel.currentThemepark.observe(this) {
            binding.currentThemepark = it
            activityView.postDelayed(
                Runnable { mainPager.setCurrentItem(it.seq.toInt(), true) },
                10
            )
        }

        atViewModel.currentTPAtrcList.observe(this) {
            pagerAdapter.atrcs = it
        }

        atViewModel.currentTPFavAtrcList.observe(this) {
            pagerAdapter.favAtrcs = it
        }

        atViewModel.showAlarms.observe(this) {
            pagerAdapter.alarms = it

            if (it.isNotEmpty()) {
                if (!AlarmService.isRunning) {
                    alarmIntent = Intent(this@TPMainActivity, AlarmService::class.java)
                    startService(alarmIntent)
                }
            } else {
                if (AlarmService.isRunning) {
                    if (alarmIntent == null) {
                        alarmIntent = Intent(this@TPMainActivity, AlarmService::class.java)
                    }
                    stopService(alarmIntent)
                    alarmIntent = null
                }
            }
        }

        atViewModel.articleRes.observe(this) {
            if (it.isFirst == true) {
                pagerAdapter.articles = it.articles.toMutableList()
            } else {
                pagerAdapter.addArticles(it.articles)
            }
        }

        atViewModel.isLoading.observe(this) {
            if (atInit) {
                binding.nowLoading = true
            } else {
                binding.nowLoading = it
            }
        }

        tpViewModel.operTime.observe(this) {
            it[0].calenderUrl = binding.themeparks?.singleOrNull { it.tpCode == "KR_EVL" }?.calenderUrl ?: ""
            it[1].calenderUrl = binding.themeparks?.singleOrNull { it.tpCode == "KR_LTW" }?.calenderUrl ?: ""

            pagerAdapter.operTimes = it.toMutableList()
        }

        atViewModel.rideFilter.observe(this) {
            pagerAdapter.rideFilters = it
        }
    }

    private fun initViews() {

        activityView.mainPager.adapter = pagerAdapter
        activityView.mainPager.isUserInputEnabled = true

        subscribeUi()

        Handler(Looper.getMainLooper()).postDelayed({
            showAnimation()
        }, 400)

        activityView.svTpTitle.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val tpList = binding.root.svTpTitle.layoutThemeparkList

                if (tpList.childCount == tpViewModel.themeparks.value?.size) {
                    for (i in 0 until tpList.childCount) {
                        val view = tpList.getChildAt(i)
                        val btnBinding = DataBindingUtil.getBinding<ListItemTopBtnBinding>(view)

                        if (btnBinding?.tpItem?.tpCode == tpViewModel.currentThemepark.value?.tpCode) {
                            tpTitlescrollTo(view.left - 10.dp2px(activityView.context))
                        }
                    }
                }
            }
        })

        activityView.mainPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                tpViewModel.currentThemepark.value = when (position) {
                    1 -> {
                        tpViewModel.themeparks.value?.get(0)
                    }
                    2 -> {
                        tpViewModel.themeparks.value?.get(1)
                    }
                    else -> {
                        Themepark(0, "News", "뉴스", "NEWS", "")
                    }
                }

                val tpCode = tpViewModel.currentThemepark.value!!.tpCode
                if (tpCode != "NEWS") {
                    refreshAtrcDynamic(tpCode)
                }

                super.onPageSelected(position)

                val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
                hashMap["tpCode"] = tpCode
                TPAnalytics.sendEvent("ClickMainMenu", hashMap)
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }
        })
    }

    private fun tpTitlescrollTo(amount: Int) {
        activityView.svTpTitle.post {
            activityView.svTpTitle.smoothScrollTo(amount, 0)
        }
    }

    private fun refreshAtrcDynamic(tpCode: String) {
        atRepository.updateAttractionsDynamic(tpCode) {
            if (tpCode == "ERROR") {
                Toast.makeText(this@TPMainActivity, "정보 불러오기에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    fun openAppInfo() {
        val bottomSheet = TPAppInfoDialog()
        bottomSheet.show(
            (binding.root.context as AppCompatActivity).supportFragmentManager,
            ""
        )
        val hashMap: MutableMap<String, String> = hashMapOf<String, String>()
        hashMap["tpCode"] = "AppInfo"
        TPAnalytics.sendEvent("ClickMainMenu", hashMap)
    }

    override fun onResume() {
        if (atInit) {
//            refreshAtrcDynamic("")
        } else {
            val tpCode = tpViewModel.currentThemepark.value!!.tpCode
            if (tpCode != "NEWS") {
                refreshAtrcDynamic(tpCode)
            }
        }

        super.onResume()
    }
}