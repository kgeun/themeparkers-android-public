package com.kgeun.themeparkers.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "themepark")
data class Themepark (
    var seq: Long,
    var name: String,
    var nameKR: String,
    @PrimaryKey var tpCode: String,
    var calenderUrl: String
)