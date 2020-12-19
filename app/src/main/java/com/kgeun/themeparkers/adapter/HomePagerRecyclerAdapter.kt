package com.kgeun.themeparkers.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.ListPreloader.PreloadSizeProvider
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.data.*
import com.kgeun.themeparkers.databinding.PageItemMainBinding
import com.kgeun.themeparkers.util.dp2px
import kotlinx.android.synthetic.main.list_item_articles_in_main_sub.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*


class HomePagerRecyclerAdapter(val parentView: ViewGroup) : RecyclerView.Adapter<HomePagerRecyclerAdapter.PageViewHolder>(), KoinComponent {

    val NEWS = 0
    val KR_EVL = 1
    val KR_LTW = 2

    var articles: MutableList<Article> = arrayListOf()
        set(value) {
            homeAdapter.submitList(value)
        }
    var atrcs: List<Attraction> = arrayListOf()
        set(value) {
            atrcAdapter[0].submitList(value.filter { it.tpCode == "KR_EVL" })
            atrcAdapter[1].submitList(value.filter { it.tpCode == "KR_LTW" })

            atrcs.forEach {
                val myOptions = RequestOptions()
                    .centerCrop()
                    .override(
                        dp2px(80, parentView.context),
                        dp2px(130, parentView.context)
                    )

                Glide
                    .with(parentView.context)
                    .asBitmap()
                    .load(it.thumbnailUrl)
                    .apply(myOptions)
//                    .transition(withCrossFade(200))
                    .preload()
            }

            field = value
        }
    var favAtrcs: List<Attraction> = arrayListOf()
        set(value) {
            atrcAdapter[0].favoriteList = value.filter { it.tpCode == "KR_EVL" }
            atrcAdapter[1].favoriteList = value.filter { it.tpCode == "KR_LTW" }
            field = value
        }
    var alarms: List<ShowAlarmItem> = arrayListOf()
        set(value) {
            atrcAdapter[0].alarmList = value.filter { it.tpCode == "KR_EVL" }
            atrcAdapter[1].alarmList = value.filter { it.tpCode == "KR_LTW" }
            field = value
        }
    var rideFilters: MutableMap<String, RideFilterItem> = mutableMapOf(
        "KR_EVL" to RideFilterItem(),
        "KR_LTW" to RideFilterItem()
    )
        set(value) {
            if (value["KR_EVL"] != null) {
                atrcAdapter[0].mRideFilter = value.get("KR_EVL")!!
            }
            if (value["KR_LTW"] != null) {
                atrcAdapter[1].mRideFilter = value.get("KR_LTW")!!
            }
            field = value
        }
    var operTimes: MutableList<OperTime> = mutableListOf(
        OperTime("", 0, 0, ""), OperTime(
            "",
            0,
            0,
            ""
        )
    )
        set(value) {
            field = value
            notifyOperTimesChanged()
        }

    var atrcAdapter: List<AttractionAdapter> = listOf(
        AttractionAdapter("KR_EVL"), AttractionAdapter(
            "KR_LTW"
        )
    )
    var homeAdapter: HomeAdapter = HomeAdapter()

    fun notifyOperTimesChanged() {
        atrcAdapter[0].operTimeItem = operTimes[0]
        atrcAdapter[1].operTimeItem = operTimes[1]
    }

    private val atRepository: AttractionRepository by inject()
    private val vhMap: MutableMap<Int, PageViewHolder> = mutableMapOf()

    init {
        vhMap.put(NEWS, PageViewHolder(
            PageItemMainBinding.inflate(
                LayoutInflater.from(parentView.context), parentView , false
            ), "NEWS"
        ))
        vhMap.put(KR_EVL, PageViewHolder(
            PageItemMainBinding.inflate(
                LayoutInflater.from(parentView.context), parentView, false
            ), "KR_EVL"
        ))
        vhMap.put(KR_LTW, PageViewHolder(
            PageItemMainBinding.inflate(
                LayoutInflater.from(parentView.context), parentView, false
            ), "KR_LTW"
        ))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return vhMap[viewType]!!
//        return when (viewType) {
//            NEWS -> vhMap[viewType]!!
//            KR_EVL ->
//            else ->
//        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 1) {
            KR_EVL
        } else if (position == 2) {
            KR_LTW
        } else {
            NEWS
        }
    }

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        holder.bind(getItemViewType(position))
    }

    fun addArticles(articles: List<Article>) {
        homeAdapter.addAll(articles)
    }

//    fun getModelProvider(tpCode: String, binding: PageItemMainBinding): PreloadModelProvider<Attraction> {
//        return object : PreloadModelProvider<Attraction> {
//            override fun getPreloadItems(position: Int): MutableList<Attraction> {
//                val startTime = System.currentTimeMillis()
//
////                // 코루틴으로 처리
////                while (atrcs.isEmpty()) {
////                    val currentTime = System.currentTimeMillis()
////
////                    if ( currentTime - startTime > 3000 ) {
////                        break
////                    }
////                }
////
////                Log.i("kglee" , "getPreloadItems 속도: " + (System.currentTimeMillis() - startTime).toString())
//
//                return atrcs.filter { it.tpCode == tpCode }.toMutableList()
//            }
//
//            override fun getPreloadRequestBuilder(item: Attraction): RequestBuilder<*>? {
//                val myOptions = RequestOptions()
//                    .centerCrop()
//                    .override(
//                        dp2px(70, binding.root.context),
//                        dp2px(130, binding.root.context)
//                    )
//
//                return Glide
//                    .with(binding.root.context)
//                    .load(item.thumbnailUrl)
////                            .placeholder(R.drawable.image_placeholder)
//                    .apply(myOptions)
//                    .transition(DrawableTransitionOptions.withCrossFade(200))
//            }
//
//        }
//    }

    inner class PageViewHolder(
        private val binding: PageItemMainBinding,
        val tpCode: String
    ) : RecyclerView.ViewHolder(binding.root) {
//        init {
//            Log.i("kglee", "init PageViewHolder " + tpCode)
//            if (tpCode != "NEWS") {
//                val sizeProvider: PreloadSizeProvider<Attraction> =
//                    FixedPreloadSizeProvider<Attraction>(
//                        dp2px(70, binding.root.context),
//                        dp2px(130, binding.root.context)
//                    )
//
////                while (getModelProvider(tpCode, binding) != null) {}
//
//                val modelProvider = object : PreloadModelProvider<Attraction> {
//                    override fun getPreloadItems(position: Int): MutableList<Attraction> {
//                        val startTime = System.currentTimeMillis()
//
//                        Log.i("kglee", "getPreloadItems 시점")
//
////                // 코루틴으로 처리
//                        return atrcs.filter { it.tpCode == tpCode }.toMutableList()
//                    }
//
//                    override fun getPreloadRequestBuilder(item: Attraction): RequestBuilder<*>? {
//                        val myOptions = RequestOptions()
//                            .centerCrop()
//                            .override(
//                                dp2px(70, binding.root.context),
//                                dp2px(130, binding.root.context)
//                            )
//
//                        return Glide
//                            .with(binding.root.context)
//                            .load(item.thumbnailUrl)
//                            .apply(myOptions)
//                    }
//
//                }

//                val preloader: RecyclerViewPreloader<Attraction> = RecyclerViewPreloader(
//                    Glide.with(binding.root.context), modelProvider, sizeProvider, 100 /*maxPreload*/
//                )
//
//                binding.rvMainList.addOnScrollListener(preloader)
//            }
//        }

        fun bind(type: Int) {
            if (type == NEWS) {
                binding.apply {
                    rvMainList.adapter = homeAdapter

                    homeAdapter.apply {
                        if (articleSubBinding != null) {
                            val sizeProvider: PreloadSizeProvider<Article> =
                                FixedPreloadSizeProvider<Article>(
                                    (articleSubBinding!!.root.imageArticleThumbnail.width / 1.5).toInt(),
                                    (articleSubBinding!!.root.imageArticleThumbnail.height * 1.5 / 1.5).toInt()
                                )

                            val modelProvider = object : PreloadModelProvider<Article> {
                                override fun getPreloadItems(position: Int): MutableList<Article> {
                                    return Collections.singletonList(articles[position])
                                }

                                override fun getPreloadRequestBuilder(item: Article): RequestBuilder<*>? {
                                    val myOptions = RequestOptions()
                                        .centerCrop()
                                        .override(
                                            (articleSubBinding!!.root.imageArticleThumbnail.width / 1.5).toInt(),
                                            (articleSubBinding!!.root.imageArticleThumbnail.height * 1.5 / 1.5).toInt()
                                        )

                                    return Glide
                                        .with(binding.root.context)
                                        .load(item.thumbnailUrl)
                                        .placeholder(R.drawable.image_placeholder)
                                        .apply(myOptions)
                                        .transition(withCrossFade(200))
                                }
                            }

                            val preloader: RecyclerViewPreloader<Article> =
                                RecyclerViewPreloader(
                                    Glide.with(binding.root.context),
                                    modelProvider,
                                    sizeProvider,
                                    40 /*maxPreload*/
                                )

                            rvMainList.addOnScrollListener(preloader)
                        }
                    }

                    rvMainList.addOnScrollListener(object :
                        RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                val lastVisibleItemPosition =
                                    (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastCompletelyVisibleItemPosition()
                                val itemTotalCount: Int = homeAdapter.itemCount - 1
                                if (lastVisibleItemPosition >= itemTotalCount) {
                                    if (atRepository.articleResultLiveData.value?.hasNext == true) {
                                        GlobalScope.launch {
                                            atRepository.updateAttractionsDynamic("NEWS")
                                        }
                                    }
                                }
                            }
                        }
                    })

                    srlLayout.setOnRefreshListener {
                        refresh(
                            binding.srlLayout,
                            "NEWS"
                        )
                    }

                    executePendingBindings()
                }
            } else if (type == KR_EVL) {
                binding.apply {
                    rvMainList.adapter = atrcAdapter[0]
                    atrcAdapter[0].submitList(atrcs.filter { it.tpCode == "KR_EVL" })
                    atrcAdapter[0].favoriteList = favAtrcs.filter { it.tpCode == "KR_EVL" }
                    atrcAdapter[0].alarmList = alarms.filter { it.tpCode == "KR_EVL" }
                    atrcAdapter[0].operTimeItem = operTimes[0]
                    atrcAdapter[0].mRideFilter = rideFilters["KR_EVL"]!!

                    srlLayout.setOnRefreshListener {
                        refresh(
                            srlLayout,
                            "KR_EVL"
                        )
                    }

                    val sizeProvider: PreloadSizeProvider<Attraction> = FixedPreloadSizeProvider<Attraction>(dp2px(70, binding.root.context), dp2px(130, binding.root.context))

                    val modelProvider = object : PreloadModelProvider<Attraction> {
                        override fun getPreloadItems(position: Int): MutableList<Attraction> {
                            atrcAdapter[0].apply {
                                when {
                                    position in CATEGORY_TITLE_ATRC_POS + 1 until CATEGORY_TITLE_SHOW_POS -> {
                                        return Collections.singletonList(atrcList[position - (CATEGORY_TITLE_ATRC_POS + 1)])
                                    }
                                    position > CATEGORY_TITLE_SHOW_POS -> {
                                        return Collections.singletonList(showList[position - (CATEGORY_TITLE_SHOW_POS + 1)])
                                    }
                                    position in (CATEGORY_TITLE_FAVORITE_POS + 1) until CATEGORY_TITLE_ATRC_POS -> {
                                        return Collections.singletonList(favoriteList[position - (CATEGORY_TITLE_FAVORITE_POS + 1)])
                                    }
                                }
                            }

                            return Collections.singletonList(atrcs[0])
                        }

                        override fun getPreloadRequestBuilder(item: Attraction): RequestBuilder<*>? {
                            val myOptions = RequestOptions()
                                .centerCrop()
                                .override(dp2px(80, binding.root.context), dp2px(130, binding.root.context))

                            return Glide
                                .with(binding.root.context)
                                .load(item.thumbnailUrl)
                                .placeholder(R.drawable.image_placeholder)
                                .apply(myOptions)
                                .transition(withCrossFade(200))
                        }
                    }

                    val preloader: RecyclerViewPreloader<Attraction> = RecyclerViewPreloader(
                        Glide.with(binding.root.context), modelProvider, sizeProvider, atrcAdapter[0].itemCount /*maxPreload*/
                    )

                    rvMainList.addOnScrollListener(preloader)

                    executePendingBindings()
                }
            } else if (type == KR_LTW) {

                binding.apply {
                    rvMainList.adapter = atrcAdapter[1]
                    atrcAdapter[1].submitList(atrcs.filter { it.tpCode == "KR_LTW" })
                    atrcAdapter[1].favoriteList = favAtrcs.filter { it.tpCode == "KR_LTW" }
                    atrcAdapter[1].alarmList = alarms.filter { it.tpCode == "KR_LTW" }
                    atrcAdapter[1].operTimeItem = operTimes[1]
                    atrcAdapter[1].mRideFilter = rideFilters["KR_LTW"]!!

                    binding.srlLayout.setOnRefreshListener {
                        refresh(
                            srlLayout,
                            "KR_LTW"
                        )
                    }

                    val sizeProvider: PreloadSizeProvider<Attraction> = FixedPreloadSizeProvider<Attraction>(dp2px(70, binding.root.context), dp2px(130, binding.root.context))

                    val modelProvider = object : PreloadModelProvider<Attraction> {
                        override fun getPreloadItems(position: Int): MutableList<Attraction> {
                            atrcAdapter[1].apply {
                                when {
                                    position in CATEGORY_TITLE_ATRC_POS + 1 until CATEGORY_TITLE_SHOW_POS -> {
                                        return Collections.singletonList(atrcList[position - (CATEGORY_TITLE_ATRC_POS + 1)])
                                    }
                                    position > CATEGORY_TITLE_SHOW_POS -> {
                                        return Collections.singletonList(showList[position - (CATEGORY_TITLE_SHOW_POS + 1)])
                                    }
                                    position in (CATEGORY_TITLE_FAVORITE_POS + 1) until CATEGORY_TITLE_ATRC_POS -> {
                                        return Collections.singletonList(favoriteList[position - (CATEGORY_TITLE_FAVORITE_POS + 1)])
                                    }
                                }
                            }

                            return Collections.singletonList(atrcs[0])
                        }

                        override fun getPreloadRequestBuilder(item: Attraction): RequestBuilder<*>? {
                            val myOptions = RequestOptions()
                                .centerCrop()
                                .override(dp2px(80, binding.root.context), dp2px(130, binding.root.context))

                            return Glide
                                .with(binding.root.context)
                                .load(item.thumbnailUrl)
                                .placeholder(R.drawable.image_placeholder)
                                .apply(myOptions)
                                .transition(withCrossFade(200))
                        }
                    }

                    val preloader: RecyclerViewPreloader<Attraction> = RecyclerViewPreloader(
                        Glide.with(binding.root.context), modelProvider, sizeProvider, atrcAdapter[1].itemCount /*maxPreload*/
                    )

                    rvMainList.addOnScrollListener(preloader)

                    executePendingBindings()
                }
            }
        }

        private fun refresh(
            srlLayout: SwipeRefreshLayout,
            tpCode: String,
            updateCallback: (() -> Unit)? = null
        ) {
            GlobalScope.launch {
                atRepository.updateAttractionsDynamic(tpCode, true) { step ->
                    if (srlLayout.isRefreshing) {
                        srlLayout.isRefreshing = false
                    }

                    if (updateCallback != null) {
                        updateCallback()
                    }
//                    else if (showSpring) {
//                        val mLoadAnimation: Animation = AnimationUtils.loadAnimation(
//                            binding.root.context,
//                            R.anim.translate_fade_in_attraction
//                        )
//                        mLoadAnimation.duration = 800
//                        binding.rvMainList.startAnimation(mLoadAnimation)
////                        srlLayout.context.runOnUiThread {
////                            binding.rvMainList.visibility = View.VISIBLE
////                        }
//                    }

                }
            }
        }
    }
}