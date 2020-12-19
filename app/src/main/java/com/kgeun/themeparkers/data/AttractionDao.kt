package com.kgeun.themeparkers.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.kgeun.themeparkers.TPPrefCtl

@Dao
interface AttractionDao {

    @Query("SELECT * FROM attraction WHERE atCode=:atCode LIMIT 1")
    fun getOne(atCode: String): LiveData<Attraction>

    @Query("SELECT * FROM attraction")
    fun getItems(): LiveData<List<Attraction>>

    @Query("SELECT * FROM attraction")
    fun getItemsSync(): List<Attraction>

    @Query("SELECT * FROM attraction WHERE tpCode=:tpCode AND kindCode=:kindCode")
    fun getKindItemsSync(tpCode: String, kindCode: String): List<Attraction>

    @Query("SELECT * FROM attraction WHERE atCode IN (SELECT atCode FROM fav_atrc) ORDER BY popularity DESC")
    fun getFavItems(): LiveData<List<Attraction>>

    @Query("SELECT * FROM attraction WHERE atCode IN (SELECT atCode FROM fav_atrc) ORDER BY popularity DESC")
    fun getFavItemsSync(): List<Attraction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReplace(attraction: List<Attraction>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(attraction: List<Attraction>)

    @Delete
    suspend fun deleteAttraction(attraction: Attraction)

    @Query("DELETE FROM attraction")
    suspend fun deleteAll()

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAttraction(attraction: Attraction)

    @Transaction
    suspend fun deleteAndInsert(atrcs: List<Attraction>) {
        deleteAll()
        insertAllIgnore(atrcs)
    }
}


