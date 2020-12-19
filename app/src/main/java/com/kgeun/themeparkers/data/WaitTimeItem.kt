package com.kgeun.themeparkers.data

import kotlinx.serialization.Serializable

@Serializable
data class WaitTimeItem (
    val time: Long?,
    val tpCode: String?,
    val atCode: String?,
    val atCode2: String?,
    val status: String?,
    val waitTime: String?,
    val waitText: String?,
    val waitLevel: Int?
)