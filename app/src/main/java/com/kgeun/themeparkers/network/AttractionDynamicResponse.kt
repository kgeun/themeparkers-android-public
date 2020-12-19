package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.WaitTimeItem

data class AttractionDynamicResponse (
    var code: Int,
    var message: String,
    var result: ArrayList<WaitTimeItem>
)