package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.StatisticItem

data class AttractionStatisticResponse (
    val code: Int,
    val message: String,
    val result: StatisticItem
)