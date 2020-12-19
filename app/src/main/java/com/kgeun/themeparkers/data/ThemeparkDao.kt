package com.kgeun.themeparkers.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*

@Dao
interface ThemeparkDao {

    @Query("SELECT * FROM themepark")
    fun getThemeparks(): LiveData<List<Themepark>>

    @Query("SELECT * FROM themepark WHERE tpCode=:tpCode LIMIT 1")
    fun getThemeparkByCodeSync(tpCode: String): Themepark

    @Query("SELECT * FROM themepark WHERE tpCode=:tpCode LIMIT 1")
    fun getThemeparkByCode(tpCode: String): LiveData<Themepark>

    @Insert
    suspend fun insertThemepark(themepark: Themepark) :Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(themeparks: List<Themepark>)

    @Query("DELETE FROM themepark")
    suspend fun deleteAll()

    @Transaction
    suspend fun deleteAndInsert(themeparks: List<Themepark>) {
        deleteAll()
        insertAll(themeparks)
    }

    @Delete
    suspend fun deleteThemepark(themepark: Themepark)
}


