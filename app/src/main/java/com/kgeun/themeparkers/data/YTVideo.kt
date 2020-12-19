package com.kgeun.themeparkers.data

import com.google.gson.annotations.SerializedName

data class YTVideo (
    val publishedAt: String?,
    val channelId: String?,
    val title: String?,
    val description: String?,
    val thumbnails: YTThumbnailsList?,
    val channelTitle: String?,
    val liveBroadcastContent: String?,
    val publishTime: String?
) {
    data class YTThumbnailsList (
        @SerializedName("default")
        val small: YTThumbnails?,
        val medium: YTThumbnails?,
        val high: YTThumbnails?
    ) {
        data class YTThumbnails (
            val url: String?,
            val width: Int?,
            val height: Int?
        )
    }
}