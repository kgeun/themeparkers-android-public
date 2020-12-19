package com.kgeun.themeparkers.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "show_alarm", primaryKeys= ["alarmDate", "showTime", "atCode"])
data class ShowAlarmItem(
    val alarmDate: String = "",
    val showTime: String,
    val statCd: String? = "",
    val validYn: String? = "",
    var before30min: Boolean = false,
    var before15min: Boolean = false,
    var before5min: Boolean = false,
    var tpCode: String? = null,
    var tpName: String? = null,
    var tpNameKR: String? = null,
    var atCode: String,
    var atName: String? = null,
    var atNameKR: String? = null,
    var thumbnailUrl: String? = null
)