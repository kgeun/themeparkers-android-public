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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kgeun.themeparkers.data.OperTime
import com.kgeun.themeparkers.data.Themepark
import com.kgeun.themeparkers.data.ThemeparkRepository

class ThemeparkViewModel(themeparkRepository: ThemeparkRepository) : ViewModel() {

    val themeparks: LiveData<List<Themepark>> = themeparkRepository.getThemeparks()

    val operTime: MutableLiveData<List<OperTime>> = themeparkRepository.operTimeLiveData

    val currentThemepark: MutableLiveData<Themepark> = themeparkRepository.currentThemeparkLiveData
}
