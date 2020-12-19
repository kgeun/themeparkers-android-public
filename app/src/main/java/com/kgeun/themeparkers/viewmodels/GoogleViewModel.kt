package com.kgeun.themeparkers.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kgeun.themeparkers.data.*


class GoogleViewModel(val googleRepository: GoogleRepository) : ViewModel() {

    val ytVideosInAtrc: MutableLiveData<List<YTVideoItem>> = googleRepository.ytVideoItems
}
