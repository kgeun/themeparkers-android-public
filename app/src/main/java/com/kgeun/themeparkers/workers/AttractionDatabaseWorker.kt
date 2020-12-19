package com.kgeun.themeparkers.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.coroutineScope
import org.koin.core.KoinComponent

class AttractionDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {
    override suspend fun doWork(): Result = coroutineScope {

        try {
//            val database = AppDatabase.getInstance(applicationContext)
//            val database: AppDatabase by inject()
//            val attractionRepository: AttractionRepository by inject()

//            attractionRepository.updateAttractions("KR_EVL")
//            attractionRepository.updateAttractions()

            Result.success()
//            applicationContext.assets.open("themeparks.json").use { inputStream ->
//                JsonReader(inputStream.reader()).use { jsonReader ->
//                    val themeparkType = object : TypeToken<List<Themepark>>() {}.type
//                    val themeparkList: List<Themepark> = Gson().fromJson(jsonReader, themeparkType)
//
//                    val database = AppDatabase.getInstance(applicationContext)
//                    database.themeparkDao().insertAll(themeparkList)
//
//                    Result.success()
//                }
//            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error seeding database", ex)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "ThemeparkDatabaseWorker"
    }
}