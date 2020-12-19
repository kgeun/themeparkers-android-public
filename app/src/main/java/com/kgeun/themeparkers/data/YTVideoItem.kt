package com.kgeun.themeparkers.data

data class YTVideoItem(
    val snippet: YTVideo?,
    val id: YTVideoId?
) {
    data class YTVideoId (
        val kind: String?,
        val videoId: String?
    )
}