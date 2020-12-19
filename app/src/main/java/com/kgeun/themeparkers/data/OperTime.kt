package com.kgeun.themeparkers.data

import java.io.Serializable

data class OperTime (
    var operTimeText: String,
    var startTime: Int,
    var endTime: Int,
    var calenderUrl: String = ""
): Serializable