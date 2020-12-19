package com.kgeun.themeparkers.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_atrc")
data class FavAtrc (
    @PrimaryKey
    var atCode: String
)