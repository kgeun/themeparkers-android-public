package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.data.Attraction
import java.util.*

data class AttractionsListStaticResponse (
    var code: Int,
    var message: String,
    var result: List<Attraction>
)