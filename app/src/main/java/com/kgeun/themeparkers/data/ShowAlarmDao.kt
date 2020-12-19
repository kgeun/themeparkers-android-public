package com.kgeun.themeparkers.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ShowAlarmDao {
    @Query("SELECT * FROM show_alarm")
    fun getShowAlarmList(): LiveData<List<ShowAlarmItem>>

    @Query("SELECT * FROM show_alarm")
    fun getShowAlarmListSync(): List<ShowAlarmItem>

    @Query("SELECT * FROM show_alarm WHERE atCode=:atCode")
    fun getAtrcShowAlarmList(atCode: String): LiveData<List<ShowAlarmItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShowAlarm(showAlarm: ShowAlarmItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShowAlarms(showAlarm: ShowAlarmItem)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(themeparks: List<Themepark>)

    @Query("DELETE FROM show_alarm WHERE atCode=:atCode AND showTime=:showTime")
    suspend fun deleteShowAlarm(atCode: String, showTime: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(attraction: List<ShowAlarmItem>)

    @Query("DELETE FROM show_alarm")
    suspend fun deleteAll()

    @Transaction
    suspend fun deleteAndInsert(items: List<ShowAlarmItem>) {
        deleteAll()
        insertAllIgnore(items)
    }
}