package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.Article
import java.io.Serializable

data class ArticleResult (
    val hasNext: Boolean,
    val isFirst: Boolean,
    val articles: List<Article>
) : Serializable