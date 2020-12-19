package com.kgeun.themeparkers.data

import java.io.Serializable

data class Article (
    val title: String,
    val author: String,
    val official: Boolean,
    val media: String,
    val description: String,
    val thumbnailUrl: String,
    val authorThumbnailUrl: String,
    val siteName: String,
    val datetime: Long,
    val siteDesc: String,
    val url: String
) : Serializable