package com.kgeun.themeparkers.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.kgeun.themeparkers.data.Article
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.databinding.ListItemArticlesFooterBinding
import com.kgeun.themeparkers.databinding.ListItemArticlesInMainSubBinding
import com.kgeun.themeparkers.databinding.ListItemArticlesInMainTopBinding
import com.kgeun.themeparkers.databinding.ListItemCategoryTitleBinding
import org.koin.core.KoinComponent
import org.koin.core.inject

class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {

    val CATEGORY_TITLE = 0
    val ARTICLE_TOP = 1
    val ARTICLE_SUB = 2
    val LOADING_FOOTER = 3

    private val atRepository: AttractionRepository by inject()

    var articleTopBinding: ListItemArticlesInMainTopBinding? = null
    var articleSubBinding: ListItemArticlesInMainSubBinding? = null

    var currentList: MutableList<Article> = arrayListOf()

    fun submitList(list: List<Article>?) {
        if (list != null) {
            currentList = list.toMutableList()
            notifyDataSetChanged()
        }
    }

    fun addAll(list: List<Article>) {
        val formerSize = currentList.size
        currentList.addAll(list)
        notifyItemRangeChanged(formerSize + 1, list.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            CATEGORY_TITLE -> CategoryTitleViewHolder(
                ListItemCategoryTitleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            LOADING_FOOTER -> FooterViewHolder (
                    ListItemArticlesFooterBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            ARTICLE_TOP -> {
                articleTopBinding = ListItemArticlesInMainTopBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ArticleViewHolder(
                    articleTopBinding!!
                )
            }
            else -> {
                articleSubBinding = ListItemArticlesInMainSubBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ArticleSubViewHolder(
                    articleSubBinding!!
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ARTICLE_TOP) {
            (holder as ArticleViewHolder).bind(currentList[position])
        } else if (getItemViewType(position) == ARTICLE_SUB) {
            (holder as ArticleSubViewHolder).bind(currentList[position])
        } else if (getItemViewType(position) == LOADING_FOOTER) {
            if (atRepository.articleResultLiveData.value?.hasNext == true) {
                (holder as FooterViewHolder).show()
            } else {
                (holder as FooterViewHolder).hide()
            }
        }
    }

    override fun getItemCount(): Int {

        return currentList.size + 1

//        if (atRepository.hasNextLiveData.value == true) {
//            return currentList.size + 1
//        } else {
//            return currentList.size
//        }

//        return currentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == currentList.size) {
            return LOADING_FOOTER
        } else if (position == 0) {
            return ARTICLE_TOP
        } else {
            return ARTICLE_SUB
        }
    }

    inner class CategoryTitleViewHolder(
        private val binding: ListItemCategoryTitleBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String, titleEn: String) {
            binding.apply {
                categoryTitle = title
                categoryTitleEn = titleEn
                executePendingBindings()
            }
        }
    }

    inner class ArticleViewHolder(
        val binding: ListItemArticlesInMainTopBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.apply {
                articleItem = article
                executePendingBindings()
            }
        }
    }

    inner class ArticleSubViewHolder(
        val binding: ListItemArticlesInMainSubBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.apply {
                articleItem = article
                executePendingBindings()
            }
        }
    }

    inner class FooterViewHolder(
        private val binding: ListItemArticlesFooterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun show() {
            binding.apply {
                binding.root.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                executePendingBindings()
            }
        }

        fun hide() {
            binding.apply {
                binding.root.layoutParams = LinearLayout.LayoutParams(0, 0)
                executePendingBindings()
            }
        }
    }
}