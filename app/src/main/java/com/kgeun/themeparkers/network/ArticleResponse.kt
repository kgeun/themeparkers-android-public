package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.Article

data class ArticleResponse (
    var code: Int,
    var message: String,
    var result: ArticleResult
)