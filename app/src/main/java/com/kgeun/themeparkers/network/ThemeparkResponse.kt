package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.Themepark

data class ThemeparkResponse (
    val status: Int,
    val message: String,
    val result: TPResult
) {
    data class TPResult (
        val dafaultTP: String,
        val data: List<Themepark>
    )
}