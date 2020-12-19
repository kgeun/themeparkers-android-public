package com.kgeun.themeparkers.data

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.debop.kodatimes.minutes
import com.kgeun.themeparkers.network.*
import com.kgeun.themeparkers.util.getTodayString
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.koin.core.KoinComponent
import org.koin.core.inject

class AttractionRepository private constructor(
    private val attractionDao: AttractionDao,
    private val favAtrcDao: FavAtrcDao,
    private val showAlarmDao: ShowAlarmDao
) : KoinComponent{

    val tpRepository: ThemeparkRepository by inject()

    var atrcDetailLiveData: MutableLiveData<AtrcDetail> = MutableLiveData()

    var rideFilterLiveData: MutableLiveData<MutableMap<String, RideFilterItem>> = MutableLiveData()

    var showAlarmListLiveData: MutableLiveData<List<ShowAlarmItem>> = MutableLiveData()

    val atrcListLiveData: LiveData<List<Attraction>>
        get() = attractionDao.getItems()

    var statLiveData: MutableLiveData<StatisticItem> = MutableLiveData()

    var estiChartLiveData: MutableLiveData<List<WaitTimeItem>> = MutableLiveData()

    var loadingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var articleResultLiveData: MutableLiveData<ArticleResult> = MutableLiveData()

//    val currentTPAtrcListMeditatorLiveData: MediatorLiveData<List<Attraction>> = MediatorLiveData<List<Attraction>>().apply {
////        addSource(tpRepository.currentThemeparkLiveData) { value -> GlobalScope.launch { postValue(getAttractionsSync()) } }
//        addSource(atrcListLiveData) { value -> setValue(value) }
//    }

//    val currentTPFavAtrcListMeditatorLiveData: MediatorLiveData<List<Attraction>> = MediatorLiveData<List<Attraction>>().apply {
////        addSource(tpRepository.currentThemeparkLiveData) { value -> GlobalScope.launch { postValue(getFavAttractionsSync()) } }
//        addSource(getFavAttractions()) { value -> setValue(value) }
//    }

    fun getAttractionsSync(): List<Attraction> {
        return attractionDao.getItemsSync()
    }

    fun getOne(atCode: String): LiveData<Attraction> {
        return attractionDao.getOne(atCode)
    }

    fun getFavAttractions(): LiveData<List<Attraction>> {
        return attractionDao.getFavItems()
    }

    fun getFavAttractionsSync(): List<Attraction> {
        return attractionDao.getFavItemsSync()
    }

    fun getShowAlarms(): LiveData<List<ShowAlarmItem>> {
        return showAlarmDao.getShowAlarmList()
    }

    fun getShowAlarmsSync(): List<ShowAlarmItem> {
        return showAlarmDao.getShowAlarmListSync()
    }

    fun getAtrcShowAlarms(atCode: String): LiveData<List<ShowAlarmItem>> {
        return showAlarmDao.getAtrcShowAlarmList(atCode)
    }

    suspend fun deleteAtrcAlarm(atCode: String, showTime: String) {
        return showAlarmDao.deleteShowAlarm(
            showTime = showTime,
            atCode = atCode
        )
    }

    fun checkIsFavoriteAtrc(atCode: String): LiveData<Boolean> {
        return favAtrcDao.checkIsFavorite(atCode)
    }

    suspend fun insertShowAlarm(showAlarm: ShowAlarmItem) {
        GlobalScope.async {
            showAlarmDao.insertShowAlarm(showAlarm)
        }.await()
    }

    suspend fun insertFavAtrc(atCode: String) {
        favAtrcDao.insertFavAtrc(FavAtrc(atCode))
    }

    suspend fun deleteFavAtrc(atCode: String) {
        favAtrcDao.deleteFavAtrc(FavAtrc(atCode))
    }

    fun findTPNameByTPCode(tpCode: String): Pair<String, String> {
        val tpRepository: ThemeparkRepository by inject()

        return tpRepository.findNameByTPCode(tpCode)
    }

    suspend fun updateAttractions(tpCode: String = "", callback: ((String) -> Unit)? = null) {
        val tpService: TPService by inject()

        tpService.getItemListStatic(tpCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result: AttractionsListStaticResponse ->
                GlobalScope.launch {
                    if (result.code == 1) {
                        async(Dispatchers.IO) {
                            val filtered = result.result.filter { !it.nameKR.isNullOrEmpty() }
                            attractionDao.deleteAndInsert(filtered)
                        }.await()

                        updateAttractionsDynamic("NEWS", true, callback)
                        updateAttractionsDynamic("KR_EVL", false, callback)
                        updateAttractionsDynamic("KR_LTW", false, callback)

                        if (callback != null) {
                            callback("ALL")
                        }
                    } else {
                    }
                }
            }, {
                if (callback != null) {
                    callback("ERROR")
                }
                loadingLiveData.postValue(false)
            })
    }

    fun updateAttractionsStatistic(atCode: String, historyType: String? = null, callback: ((Int) -> Unit)? = null) {
        val tpService: TPService by inject()

        tpService.getAtrcStatistic(atCode, historyType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                if (result.code == 1) {
                    GlobalScope.launch {
                        statLiveData.postValue(result.result)

                        if (callback != null) {
                            callback(2)
                        }
                    }
                }
            }, {
                if (callback != null) {
                    callback(-1)
                }
            })
    }

    fun updateAttractionsDynamic(tpCode: String, refresh: Boolean = false, callback: ((String) -> Unit)? = null) {
        val tpService: TPService by inject()

        if (tpCode == "NEWS") {
            var offset: Long? = if (refresh || articleResultLiveData.value?.articles.isNullOrEmpty()) {
                null
            } else {
                articleResultLiveData.value?.articles?.last()?.datetime
            }

            tpService.getArticles(offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result.code == 1) {
                        GlobalScope.launch {
                            articleResultLiveData.postValue(result.result)
                            if (callback != null) {
                                callback(tpCode)
                            }
                        }
                    }
                }, {
                    if (callback != null) {
                        callback("ERROR")
                    }
                    loadingLiveData.postValue(false)
                })
        } else {

            tpService.getAtrcDynamic(tpCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    if (result.code == 1) {
                        GlobalScope.launch {
                            var atrcList = attractionDao.getKindItemsSync(tpCode, "RIDE")
                            val dynamicAtrcData = result.result
                            val operTime = if (tpCode == "KR_EVL") {
                                tpRepository.operTimeLiveData.value?.get(0)
                            } else {
                                tpRepository.operTimeLiveData.value?.get(1)
                            }
                            val now = DateTime.now().millisOfDay

                            dynamicAtrcData.forEach { dyna ->
                                atrcList.forEach { atrc ->
                                    if (dyna.atCode == atrc.atCode) {
                                        try {
                                            if (!dyna.waitTime.isNullOrEmpty()) {
                                                atrc.waitTime = Integer.parseInt(dyna.waitTime)
                                            }
                                        } catch (e: Exception) {
                                        }
                                        try {
                                            dyna.waitLevel?.let {
                                                atrc.waitLevel = dyna.waitLevel
                                            }
                                        } catch (e: Exception) {
                                        }
                                        try {
                                            dyna.waitText?.let {
                                                atrc.waitText = dyna.waitText
                                            }
                                        } catch (e: Exception) {
                                        }
                                        try {
                                            dyna.status?.let {
                                                atrc.status = dyna.status
                                            }
                                        } catch (e: Exception) {
                                        }
                                    }
                                }
                            }

                            atrcList.forEach { atrc ->
                                if (!atrc.closeTm.isNullOrEmpty() && !atrc.openTm.isNullOrEmpty()) {

                                    val formatter: DateTimeFormatter =
                                        DateTimeFormat.forPattern("HH:mm")

                                    atrc.openTm?.let {
                                        if (it.indexOf(":") <= -1) {
                                            atrc.openTm =
                                                "${it.substring(0..1)}:${it.substring(2..3)}"
                                        }
                                    }
                                    atrc.closeTm?.let {
                                        if (it.isNotEmpty() && it.indexOf(":") <= -1) {
                                            atrc.closeTm =
                                                "${it.substring(0..1)}:${it.substring(2..3)}"
                                        }
                                    }

                                    val openTimeLong =
                                        formatter.parseLocalTime(atrc.openTm).millisOfDay
                                    val closeTimeLong =
                                        formatter.parseLocalTime(atrc.closeTm).millisOfDay

                                    if (now < openTimeLong && atrc.status != "STND") {
                                        atrc.status = "STND"
                                    } else if (now > closeTimeLong && atrc.status != "OVER") {
                                        atrc.status = "OVER"
                                    }
                                }
                            }

                            GlobalScope.launch {
                                attractionDao.insertAllReplace(atrcList)
                            }

                            if (callback != null) {
                                callback(tpCode)
                            }
                        }
                    }
                }, {
                    if (callback != null) {
                        callback("ERROR")
                    }
                    loadingLiveData.postValue(false)
                })
        }
    }

    fun updateAtrcDetail(atCode: String , onFail: (() -> Unit)? = null) {
        val tpService: TPService by inject()

        tpService.getItemDetail(atCode)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                if (result.code == 1) {
                    GlobalScope.launch {
                        atrcDetailLiveData.postValue(result.result)
                    }
                }
            }, {
                if (onFail != null) {
                    onFail()
                }
            })
    }

    suspend fun deleteAndInsertShowAlarm(list: List<ShowAlarmItem>) {
        showAlarmDao.deleteAndInsert(list)
    }

    suspend fun removeExpiredAlarms(emptyAction: (() -> Unit)? = null) {
        GlobalScope.async {
            removeAction()
        }.await()

        if (getShowAlarmsSync().isEmpty()) {
            if (emptyAction != null) {
                emptyAction()
            }
        }
    }

    suspend fun removeAction() {
        getShowAlarmsSync().forEach {
            if (it.alarmDate != getTodayString()) {
                deleteAtrcAlarm(it.atCode, it.showTime)
                return
            } else {
                val nowMillis = DateTime.now().millisOfDay

                val formatter: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
                val showTimeDate: DateTime = formatter.parseDateTime(it.showTime)

                var checked = false

                if ((showTimeDate - 5.minutes()).millisOfDay < nowMillis) {
                    checked = true
                    it.before5min = false
                }

                if ((showTimeDate - 15.minutes()).millisOfDay < nowMillis) {
                    checked = true
                    it.before15min = false
                }

                if ((showTimeDate - 30.minutes()).millisOfDay < nowMillis) {
                    checked = true
                    it.before30min = false
                }

                if (!it.before5min && !it.before15min && !it.before30min) {
                    GlobalScope.async {
                        deleteAtrcAlarm(it.atCode, it.showTime)
                    }.await()
                    return
                } else if (checked) {
                    GlobalScope.async {
                        insertShowAlarm(it)
                    }.await()
                    return
                }
            }
        }
    }

    companion object {
        @Volatile private var instance: AttractionRepository? = null

        fun getInstance(
            attractionDao: AttractionDao,
            favAtrcDao: FavAtrcDao,
            showAlarmDao: ShowAlarmDao
        ) = instance ?: synchronized(this) {
                instance ?: AttractionRepository(attractionDao, favAtrcDao, showAlarmDao).also { instance = it }
            }
    }
}
