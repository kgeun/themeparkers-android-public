package com.kgeun.themeparkers.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kgeun.themeparkers.util.THEMEPARKERS_DB
import org.koin.core.KoinComponent

@Database(entities = [Themepark::class, Attraction::class, FavAtrc::class, ShowAlarmItem::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(), KoinComponent {

    abstract fun themeparkDao(): ThemeparkDao
    abstract fun attractionDao(): AttractionDao
    abstract fun favAtrcDao(): FavAtrcDao
    abstract fun showAlarmDao(): ShowAlarmDao

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, THEMEPARKERS_DB)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
//                        val request = OneTimeWorkRequestBuilder<ThemeparkDatabaseWorker>().build()
//                        WorkManager.getInstance(context).enqueue(request)

//                        val request2 = OneTimeWorkRequestBuilder<AttractionDatabaseWorker>().build()
//                        WorkManager.getInstance(context).enqueue(request2)
                    }
                })
                .build()
        }
    }
}