package com.kgeun.themeparkers.adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.kgeun.themeparkers.R
import com.kgeun.themeparkers.custom.TPRideFilterDialog
import com.kgeun.themeparkers.data.Attraction
import com.kgeun.themeparkers.data.OperTime
import com.kgeun.themeparkers.data.RideFilterItem
import com.kgeun.themeparkers.data.ShowAlarmItem
import com.kgeun.themeparkers.databinding.*
import com.kgeun.themeparkers.util.*
import com.kgeun.themeparkers.view.TPAttractionActivity
import com.nmp.studygeto.analytics.TPAnalytics
import kotlinx.android.synthetic.main.list_item_category_title.view.*
import org.koin.core.KoinComponent
import java.util.*

class AttractionAdapter(val tpCode: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    val CATEGORY_TITLE_OPER_TIME = 1
    val OPER_TIME_ITEM = 2
    val CATEGORY_TITLE_FAVORITE = 3
    val CATEGORY_TITLE_ATTRACTION = 4
    val ATRC_ITEM = 5
    val CATEGORY_TITLE = 6
    val ALARM_ITEM = 7
    val HIDE_ITEM = 8

    var favoriteList: List<Attraction> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var alarmList: List<ShowAlarmItem> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var mRideFilter: RideFilterItem = RideFilterItem()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var currentList: List<Attraction> = arrayListOf()

    val atrcList: List<Attraction>
        get() = rideFilter(currentList.filter { it.kindCode == "RIDE" }, mRideFilter)

    val showList: List<Attraction>
        get() = currentList.filter { it.kindCode == "SHOW" }

    var operTimeItem: OperTime = OperTime("", 0, 0, "")
        set(value) {
            field = value
            notifyItemChanged(CATEGORY_OPER_TIME_POS)
        }

    val CATEGORY_OPER_TIME_TITLE_POS = 0
    val CATEGORY_OPER_TIME_POS = 1
    val CATEGORY_TITLE_ALARM_POS = 2

    val CATEGORY_TITLE_FAVORITE_POS: Int
        get() = alarmList.size + 3
    val CATEGORY_TITLE_ATRC_POS: Int
        get() = favoriteList.size + CATEGORY_TITLE_FAVORITE_POS + 1
    val CATEGORY_TITLE_SHOW_POS: Int
        get() = atrcList.size + CATEGORY_TITLE_ATRC_POS + 1

    fun submitList(list: List<Attraction>?) {
        if (!list.isNullOrEmpty()) {
            currentList = list!!
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CATEGORY_TITLE -> CategoryTitleViewHolder(
                ListItemCategoryTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            CATEGORY_TITLE_ATTRACTION -> AttractionTitleViewHolder(
                ListItemRideCategoryTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            OPER_TIME_ITEM -> OperTimeViewHolder(
                ListItemOpertimeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            ALARM_ITEM -> AlarmViewHolder(
                ListItemShowAlarmMainlistBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            HIDE_ITEM -> CategoryTitleViewHolder(
                ListItemCategoryTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> ItemViewHolder(
                ListItemAttractionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when {
            position == CATEGORY_OPER_TIME_TITLE_POS -> (holder as CategoryTitleViewHolder).bind(
                "오늘의 운영시간",
                "Today\'s Operation Time"
            )
            position == CATEGORY_TITLE_ALARM_POS -> {
                if (getItemViewType(position) == HIDE_ITEM) {
                    (holder as CategoryTitleViewHolder).hide()
                } else {
                    (holder as CategoryTitleViewHolder).bind(
                        "알람",
                        "Alarms"
                    )
                }
            }
            position == CATEGORY_TITLE_FAVORITE_POS -> (holder as CategoryTitleViewHolder).bind(
                "즐겨찾기",
                "Favorites"
            )
            position == CATEGORY_TITLE_ATRC_POS -> (holder as AttractionTitleViewHolder).bind(
                "어트랙션",
                "Attractions",
                mRideFilter
            )
            position == CATEGORY_TITLE_SHOW_POS -> (holder as CategoryTitleViewHolder).bind(
                "엔터테인먼트",
                "Entertainments"
            )
            position == CATEGORY_OPER_TIME_POS -> (holder as OperTimeViewHolder).bind(operTimeItem)
            position in CATEGORY_TITLE_ALARM_POS + 1 until CATEGORY_TITLE_FAVORITE_POS-> {
                val atrc = alarmList[position - (CATEGORY_TITLE_ALARM_POS + 1)]
                (holder as AlarmViewHolder).bind(atrc)
            }
            position in CATEGORY_TITLE_ATRC_POS + 1 until CATEGORY_TITLE_SHOW_POS-> {
                val atrc = atrcList[position - (CATEGORY_TITLE_ATRC_POS + 1)]
                (holder as ItemViewHolder).bind(atrc)
            }
            position > CATEGORY_TITLE_SHOW_POS -> {
                val show = showList[position - (CATEGORY_TITLE_SHOW_POS + 1)]
                (holder as ItemViewHolder).bind(show)
            }
            position in (CATEGORY_TITLE_FAVORITE_POS + 1) until CATEGORY_TITLE_ATRC_POS -> {
                val atrc = favoriteList[position - (CATEGORY_TITLE_FAVORITE_POS + 1)]
                (holder as ItemViewHolder).bind(atrc)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        when (position) {
            CATEGORY_OPER_TIME_TITLE_POS -> {
                return CATEGORY_TITLE
            }
            CATEGORY_TITLE_ALARM_POS -> {
                if (alarmList.isEmpty()) {
                    return HIDE_ITEM
                } else {
                    return CATEGORY_TITLE
                }
            }
            CATEGORY_TITLE_FAVORITE_POS -> {
                return CATEGORY_TITLE
            }
            CATEGORY_TITLE_ATRC_POS -> {
                return CATEGORY_TITLE_ATTRACTION
            }
            CATEGORY_TITLE_SHOW_POS -> {
                return CATEGORY_TITLE
            }
            CATEGORY_OPER_TIME_POS -> {
                return OPER_TIME_ITEM
            }
            in (CATEGORY_TITLE_ALARM_POS + 1 until CATEGORY_TITLE_FAVORITE_POS) -> {
                return ALARM_ITEM
            }
            else -> {
                return ATRC_ITEM
            }
        }
    }

    override fun getItemCount(): Int {
        return alarmList.size + atrcList.size + showList.size + favoriteList.size + 5
    }

    class ItemViewHolder(
        private val binding: ListItemAttractionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { view ->
                val thisAtrc = binding.atItem!!

                val intent = Intent(binding.root.context, TPAttractionActivity::class.java)
                intent.putExtra(TPAttractionActivity.EXTRA_KEY_AT_CODE, thisAtrc.atCode)
                intent.putExtra(TPAttractionActivity.EXTRA_KEY_KIND_CODE, thisAtrc.kindCode)

                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                binding.root.context.startActivity(intent)
                (binding.root.context as Activity).overridePendingTransition(
                    R.anim.fade_in,
                    R.anim.fade_out
                )

                TPAnalytics.sendClick("ClickAttractionDetail")
            }
        }

        fun bind(item: Attraction) {
            binding.apply {
                atItem = item
                executePendingBindings()
            }
        }
    }

    inner class CategoryTitleViewHolder(
        private val binding: ListItemCategoryTitleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, titleEn: String) {
            binding.apply {
                categoryTitle = title
                categoryTitleEn = titleEn

                if (title == "즐겨찾기" && favoriteList.isEmpty()) {
                    root.additionalInfoLayout.visibility = View.VISIBLE
                    root.additionalInfoLayout.removeAllViews()
                    root.additionalInfoLayout.addView(
                        LayoutInflater.from(binding.root.context)
                            .inflate(R.layout.list_item_no_favourites, root as ViewGroup, false)
                    )
                } else {
                    root.additionalInfoLayout.visibility = View.GONE
                    root.additionalInfoLayout.removeAllViews()
                }
                executePendingBindings()
            }
        }

        fun hide() {
            binding.root.layoutParams = LinearLayout.LayoutParams(0, 0)
        }
    }

    inner class AttractionTitleViewHolder(
        private val binding: ListItemRideCategoryTitleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, titleEn: String, mRideFilter: RideFilterItem) {
            binding.apply {
                categoryTitle = title
                categoryTitleEn = titleEn

                btnFilterLayout.setOnClickListener {
                    val bottomSheet = TPRideFilterDialog(tpCode)
                    bottomSheet.filterItem = mRideFilter.copy(
                        height = mRideFilter.height,
                        statusMode = mRideFilter.statusMode
                    )
                    bottomSheet.show(
                        (binding.root.context as AppCompatActivity).supportFragmentManager,
                        ""
                    )
                }

                btnFilter.text = getFilterName(mRideFilter)

                executePendingBindings()
            }
        }

        fun getFilterName(filter: RideFilterItem): String {
            if (filter.statusMode == RIDE_ALL && filter.height == 151) {
                return "${filter.height}cm 이상"
            } else if (filter.statusMode == RIDE_ALL && filter.height > -1) {
                return "${filter.height}cm"
            }

            val sb = StringBuffer()

            if (filter.height == 151){
                sb.append("${filter.height}cm 이상, ")
            } else if (filter.height > -1) {
                sb.append("${filter.height}cm, ")
            }

            when (filter.statusMode) {
                RIDE_ALL -> sb.append("전체")
                RIDE_OPERATING -> sb.append("운행 중")
                RIDE_UNDER_1HR -> sb.append("60분 이하")
                RIDE_UNDER_30MIN -> sb.append("30분 이하")
            }

            return sb.toString()
        }
    }

    class OperTimeViewHolder(
        private val binding: ListItemOpertimeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(opTime: OperTime) {
            binding.apply {
                operTimeItem = opTime
                executePendingBindings()
            }
        }
    }

    class AlarmViewHolder(
        private val binding: ListItemShowAlarmMainlistBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShowAlarmItem) {
            binding.apply {
                alarmItem = item

                root.setOnClickListener { view ->
                    val thisAtrc = binding.alarmItem

                    thisAtrc?.let {
                        val intent = Intent(binding.root.context, TPAttractionActivity::class.java)
                        intent.putExtra(TPAttractionActivity.EXTRA_KEY_AT_CODE, it.atCode)
                        intent.putExtra(TPAttractionActivity.EXTRA_KEY_KIND_CODE, "SHOW")
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        binding.root.context.startActivity(intent)
                        (binding.root.context as Activity).overridePendingTransition(
                            R.anim.fade_in,
                            R.anim.fade_out
                        )
                    }
                }
                executePendingBindings()
            }
        }
    }
}


private class AtrDiffCallback : DiffUtil.ItemCallback<Attraction>() {

    override fun areItemsTheSame(
        oldItem: Attraction,
        newItem: Attraction
    ): Boolean {
        return oldItem.atCode == newItem.atCode
    }

    override fun areContentsTheSame(
        oldItem: Attraction,
        newItem: Attraction
    ): Boolean {
        return oldItem == newItem
    }
}