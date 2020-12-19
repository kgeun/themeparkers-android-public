package com.kgeun.themeparkers.data

import kotlinx.serialization.Serializable

@Serializable
data class ShowTimeItem (
    val playFromTm: String?,
    val statCd: String?,
    val validYn: String?,
    var showTime: String?
)