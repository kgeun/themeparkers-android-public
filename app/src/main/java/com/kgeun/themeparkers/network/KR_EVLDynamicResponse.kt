package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.ShowTimeItem
import kotlinx.serialization.Serializable


data class KR_EVLDynamicResponse (
    val status: Int,
    val message: String,
    val data: DynamicResult
) {
    data class DynamicResult (
        val facilOperList: List<FacilOperList>
    ) {
        data class FacilOperList (
            val facilId: String,
            val openTm: String,
            val closeTm: String,
            val closeStatCd: String,
            val waitMm: String,
            val waitLvl: String,
            val useEp: String,
            val facilPlayList: List<ShowTimeItem>?
        )
    }
}