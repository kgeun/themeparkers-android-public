/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kgeun.themeparkers.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kgeun.themeparkers.data.*
import com.kgeun.themeparkers.data.AtrcDetail
import com.kgeun.themeparkers.network.ArticleResult

class AttractionViewModel(private val attractionRepository: AttractionRepository) : ViewModel() {

    val showAlarms: LiveData<List<ShowAlarmItem>> = attractionRepository.getShowAlarms()

    val currentTPAtrcList: LiveData<List<Attraction>> = attractionRepository.atrcListLiveData

    val currentTPFavAtrcList: LiveData<List<Attraction>> = attractionRepository.getFavAttractions()

    val showAlarmList: MutableLiveData<List<ShowAlarmItem>> = attractionRepository.showAlarmListLiveData

    val statItem: MutableLiveData<StatisticItem> = attractionRepository.statLiveData

    val estiChart: MutableLiveData<List<WaitTimeItem>> = attractionRepository.estiChartLiveData

//    val articleList: MutableLiveData<List<Article>> = attractionRepository.articleLiveData
//
    val isLoading: MutableLiveData<Boolean> = attractionRepository.loadingLiveData
//
//    val hasNext: MutableLiveData<Boolean> = attractionRepository.hasNextLiveData
//
//    val isFirst: MutableLiveData<Boolean> = attractionRepository.isFirstLiveData

    val articleRes: MutableLiveData<ArticleResult> = attractionRepository.articleResultLiveData

    val atrcDetail: MutableLiveData<AtrcDetail>
        get() {
            return attractionRepository.atrcDetailLiveData
        }

    val rideFilter: MutableLiveData<MutableMap<String, RideFilterItem>>
        get() {
            return attractionRepository.rideFilterLiveData
        }

    fun getAtrcShowAlarms(atCode: String): LiveData<List<ShowAlarmItem>> {
        return attractionRepository.getAtrcShowAlarms(atCode)
    }

    fun getAtrcSync(): List<Attraction> {
        return attractionRepository.getAttractionsSync()
    }

    fun getFavAtrcSync(): List<Attraction> {
        return attractionRepository.getFavAttractionsSync()
    }

    fun getShowAlarmsSync(): List<ShowAlarmItem> {
        return attractionRepository.getShowAlarmsSync()
    }

    fun getOne(atCode: String): LiveData<Attraction> {
        return attractionRepository.getOne(atCode)
    }

    fun checkIsFavoriteAtrc(atCode: String): LiveData<Boolean> {
        return attractionRepository.checkIsFavoriteAtrc(atCode)
    }
}