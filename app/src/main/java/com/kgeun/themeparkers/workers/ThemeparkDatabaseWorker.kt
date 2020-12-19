package com.kgeun.themeparkers.workers

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.kgeun.themeparkers.data.AppDatabase
import com.kgeun.themeparkers.data.Themepark
import kotlinx.coroutines.coroutineScope

class ThemeparkDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result = coroutineScope {
        Result.success()
    }

    companion object {
        private const val TAG = "ThemeparkDatabaseWorker"
    }
}