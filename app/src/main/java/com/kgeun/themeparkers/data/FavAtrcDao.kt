package com.kgeun.themeparkers.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavAtrcDao {

    @Query("SELECT * FROM fav_atrc")
    fun getFavAtrcList(): LiveData<List<FavAtrc>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavAtrc(favAtrc: FavAtrc)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(themeparks: List<Themepark>)

    @Query("SELECT EXISTS(SELECT * FROM fav_atrc WHERE atCode=:atCode)")
    fun checkIsFavorite(atCode: String): LiveData<Boolean>

    @Delete
    suspend fun deleteFavAtrc(favAtrc: FavAtrc)
}