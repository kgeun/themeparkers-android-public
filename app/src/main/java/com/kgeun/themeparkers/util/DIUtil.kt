package com.kgeun.themeparkers.util

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kgeun.themeparkers.BuildConfig
import com.kgeun.themeparkers.data.AppDatabase
import com.kgeun.themeparkers.data.AttractionRepository
import com.kgeun.themeparkers.data.GoogleRepository
import com.kgeun.themeparkers.data.ThemeparkRepository
import com.kgeun.themeparkers.network.KR_EVLDynamicService
import com.kgeun.themeparkers.network.KR_EVLService
import com.kgeun.themeparkers.network.KR_LTWService
import com.kgeun.themeparkers.network.TPService
import com.kgeun.themeparkers.viewmodels.AttractionViewModel
import com.kgeun.themeparkers.viewmodels.GoogleViewModel
import com.kgeun.themeparkers.viewmodels.ThemeparkViewModel
import com.kgeun.themeparkers.workers.ThemeparkDatabaseWorker
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


var modulePart = module {

    single<KR_EVLDynamicService> {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        Retrofit.Builder()
            .baseUrl("http://stk.everland.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
//            .client(httpClient.build())
            .build()
            .create(KR_EVLDynamicService::class.java)
    }

    single<KR_EVLService> {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        Retrofit.Builder()
            .baseUrl("http://www.everland.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .client(httpClient.build())
            .build()
            .create(KR_EVLService::class.java)
    }

    single<KR_LTWService> {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        Retrofit.Builder()
            .baseUrl("https://adventure.lotteworld.com/")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .client(httpClient.build())
            .build()
            .create(KR_LTWService::class.java)
    }

    single<TPService> {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
//            .client(httpClient.build())
            .build()
            .create(TPService::class.java)
    }

    single { Room.databaseBuilder(androidContext(), AppDatabase::class.java, THEMEPARKERS_DB)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val request = OneTimeWorkRequestBuilder<ThemeparkDatabaseWorker>().build()
                WorkManager.getInstance(androidContext()).enqueue(request)
            }
        })
        .build() }

    single { get<AppDatabase>().themeparkDao() }

    single { get<AppDatabase>().attractionDao() }

    single { get<AppDatabase>().favAtrcDao() }

    single { get<AppDatabase>().showAlarmDao() }

    single { ThemeparkRepository.getInstance(get(), get()) }

    single { AttractionRepository.getInstance(get(), get(), get()) }

    single { GoogleRepository.getInstance() }

    viewModel { ThemeparkViewModel(get()) }

    viewModel { AttractionViewModel(get()) }

    viewModel { GoogleViewModel(get()) }
}

var diModule = listOf(modulePart)