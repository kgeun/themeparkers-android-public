package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.AtrcDetail

data class AttractionDetailResponse (
    var code: Int,
    var message: String,
    var result: AtrcDetail
)