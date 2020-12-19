package com.kgeun.themeparkers.data

data class StatisticItem (
    val today: ArrayList<WaitTimeItem>?,
    val history: ArrayList<WaitTimeItem>?,
    val maxTime: Long,
    val type: String?
)