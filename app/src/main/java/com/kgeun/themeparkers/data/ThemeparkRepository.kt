package com.kgeun.themeparkers.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kgeun.themeparkers.network.KR_EVLService
import com.kgeun.themeparkers.network.KR_LTWService
import com.kgeun.themeparkers.network.TPService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.koin.core.KoinComponent
import org.koin.core.inject


class ThemeparkRepository private constructor(
    private val themeparkDao: ThemeparkDao,
    private val tpService: TPService
) : KoinComponent {
//    suspend fun createThemepark(name: String) {
//        val themepark = Themepark(name, false)
//        themeparkDao.insertThemepark(themepark)
//    }

    val operTimeLiveData = MutableLiveData<List<OperTime>>()
    val currentThemeparkLiveData = MutableLiveData<Themepark>()

    fun getThemeparks(): LiveData<List<Themepark>> {
        return themeparkDao.getThemeparks()
    }

//    fun getThemeparkByTpCode(tpCode: String): Themepark = runBlocking {
//        themeparkDao.getThemeparkByCodeSync(tpCode)
//    }

    fun getThemeparkByTpCode(tpCode: String): Themepark = runBlocking {
        withContext(Dispatchers.Default) {
            val a = async { themeparkDao.getThemeparkByCodeSync(tpCode) }
            a.await()
        }
    }

    fun findNameByTPCode(tpCode: String): Pair<String, String> {
        val tmpk = themeparkDao.getThemeparkByCodeSync(tpCode)
        val name = tmpk.name
        val nameKR = tmpk.nameKR
        return Pair(name, nameKR)
    }

    suspend fun updateThemeparks(callback: ((Int) -> Unit)? = null) {

        val tpService: TPService by inject()

        tpService.getThemeparks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                result ->
                GlobalScope.launch {
                    themeparkDao.deleteAndInsert(result.result.data)
                }

                if (callback != null) {
                    callback(0)
                }
            }, {
                if (callback != null) {
                    callback(-1)
                }
            })
    }

    suspend fun updateOperTimeInfo() {

        val operTimeList: MutableList<OperTime> = mutableListOf()

        val krEvlService: KR_EVLService by inject()
        val krLtwService: KR_LTWService by inject()

        val krLtwOperTimeSuccess: (ResponseBody) -> Unit =
            { result ->
                val startTime: Int
                val endTime: Int

                try {
                    val source: Document = Jsoup.parse(result.string())

                    val operTimeText =
                        source.getElementsByClass("time")[0].getElementsByClass("txt")[0].text().replace("-", "~")

                    val operTimeTextArray =
                        source.getElementsByClass("time")[0].getElementsByClass("txt")[0].getElementsByTag("span")

                    val startTimeText = operTimeTextArray[0].text()
                    val endTimeText = operTimeTextArray[1].text().substring(2, operTimeTextArray[1].text().length)

                    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
                    startTime = dtf.parseDateTime(startTimeText).secondOfDay().get()
                    endTime = dtf.parseDateTime(endTimeText).secondOfDay().get()

                    operTimeList.add(OperTime(
                        operTimeText = operTimeText,
                        startTime = startTime,
                        endTime = endTime,
                        calenderUrl = ""
                    ))
                } catch ( e: Exception ) {
                    e.printStackTrace()

                    operTimeList.add(OperTime(
                        operTimeText = "정보를 불러올 수 없습니다.",
                        startTime = 0,
                        endTime = 0,
                        calenderUrl = ""
                    ))
                }

                operTimeLiveData.value = operTimeList
            }

        val krLtwOperTimeFail: (Throwable) -> Unit =
            {
                operTimeList.add(OperTime(
                    operTimeText = "정보를 불러올 수 없습니다.",
                    startTime = 0,
                    endTime = 0,
                    calenderUrl = ""
                ))

                operTimeLiveData.value = operTimeList
            }

        val krEvlOperTimeSuccess: (ResponseBody) -> Unit =
            { result ->

                val startTime: Int
                val endTime: Int

                try {
                    val source: Document = Jsoup.parse(result.string())

                    val operTimeText =
                        source.getElementsByClass("usetime").select("strong").text()

                    val startTimeText = operTimeText.split(" ~ ")[0]
                    val endTimeText = operTimeText.split(" ~ ")[1]

                    val dtf: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")
                    startTime = dtf.parseDateTime(startTimeText).secondOfDay().get()
                    endTime = dtf.parseDateTime(endTimeText).secondOfDay().get()

                    operTimeList.add(OperTime(
                        operTimeText = operTimeText,
                        startTime = startTime,
                        endTime = endTime,
                        calenderUrl = ""
                    ))
                } catch ( e: Exception ) {
                    e.printStackTrace()

                    operTimeList.add(OperTime(
                        operTimeText = "정보를 불러올 수 없습니다.",
                        startTime = 0,
                        endTime = 0,
                        calenderUrl = ""
                    ))
                }

                krLtwService.getOpertimeInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(krLtwOperTimeSuccess, krLtwOperTimeFail)
            }

        val krEvlOperTimeFail: (Throwable) -> Unit =
            {
                operTimeList.add(OperTime(
                    operTimeText = "정보를 불러올 수 없습니다.",
                    startTime = 0,
                    endTime = 0,
                    calenderUrl = getThemeparkByTpCode("KR_EVL").calenderUrl ?: ""
                ))

                krLtwService.getOpertimeInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(krLtwOperTimeSuccess, krLtwOperTimeFail)
            }


        krEvlService.getOpertimeInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(krEvlOperTimeSuccess, krEvlOperTimeFail)

    }

    companion object {
        @Volatile private var instance: ThemeparkRepository? = null

        fun getInstance(themeparkDao: ThemeparkDao, tpService: TPService) =
            instance ?: synchronized(this) {
                instance ?: ThemeparkRepository(themeparkDao, tpService).also { instance = it }
            }
    }
}