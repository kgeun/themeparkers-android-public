package com.kgeun.themeparkers.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.debop.kodatimes.minutes
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import kotlinx.serialization.Serializable
import org.joda.time.DateTime

@Serializable
@Entity(tableName = "attraction")
data class Attraction (
    var seq: Int,
    var tpCode: String,
    @PrimaryKey
    var atCode: String,
    var atCode2: String?,
    var areaKR: String?,
    var nameKR: String?,
    var area: String?,
    var name: String?,
    var thumbnailUrl: String?,
    var largeThumbnailUrl: String?,
    var status: String?,
    var waitTime: Int?,
    var waitText: String?,
    var tpName: String?,
    var tpNameKR: String?,
    var kindCode: String?,
    var heightFrom: String?,
    var heightTo: String?,
    var showTimeRaw: String?,
    var keyword: String?,
    var showPeriod: String?,
    var additionalInfo: String?,
    var openTm: String?,
    var closeTm: String?,
    var waitLevel: Int?,
    var popularity: Int?
) {
    val showTimeParsedString: String
        get() = try {
                val type  = object : TypeToken<List<ShowTimeItem>>() {}.type
                val obj = Gson().fromJson<List<ShowTimeItem>>(showTimeRaw, type)
                val stringBuffer = StringBuffer()

                obj.forEach {
                    if (it.statCd == "OPEN") {
                        if (it.playFromTm != null && it.playFromTm.indexOf(":") > -1) {
                            stringBuffer.append(" ${it.playFromTm}")
                        } else {
                            stringBuffer.append(
                                " ${it.playFromTm?.substring(0..1)}:${
                                    it.playFromTm?.substring(
                                        2..3
                                    )
                                }"
                            )
                        }
                        stringBuffer.append(",")
                    }
                }
                stringBuffer.setLength((stringBuffer.length - 1).coerceAtLeast(0))

                stringBuffer.toString()
            } catch (e: Exception) {
                ""
            }

    val showtimeList: List<ShowTimeItem>
        get() = try {
            val type  = object : TypeToken<List<ShowTimeItem>>() {}.type
            val list = Gson().fromJson<List<ShowTimeItem>>(showTimeRaw, type)

            list.map { it.showTime = if(it.playFromTm != null && it.playFromTm.indexOf(":") > -1) it.playFromTm else "${it.playFromTm?.substring(0..1)}:${it.playFromTm?.substring(2..3)}"}
            list
        } catch (e: Exception) {
            arrayListOf<ShowTimeItem>()
        }
}