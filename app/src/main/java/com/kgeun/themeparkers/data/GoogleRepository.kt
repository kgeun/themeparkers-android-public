package com.kgeun.themeparkers.data

import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.KoinComponent
import org.koin.core.inject

class GoogleRepository: KoinComponent {

    val ytVideoItems = MutableLiveData<List<YTVideoItem>>()

    companion object {
        @Volatile private var instance: GoogleRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: GoogleRepository().also { instance = it }
            }
    }
}