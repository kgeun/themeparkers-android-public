package com.kgeun.themeparkers.data

import com.kgeun.themeparkers.util.RIDE_ALL

data class RideFilterItem (
    var height: Int = -1,
    var statusMode: Int = RIDE_ALL
)