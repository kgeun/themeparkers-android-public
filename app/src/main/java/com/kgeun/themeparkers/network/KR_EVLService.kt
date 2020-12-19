package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.util.getToday
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface KR_EVLService {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded", "Accept-Language: ko-KR")
    @POST("/service/front/frontTime.do")
    fun getOpertimeInfo(
        @Field("method")
        method: String = "operTimeRslt",
        @Field("siteCode")
        siteCode: String = "CT00101",
        @Field("baseDate")
        baseDate: String = getToday()
    ): Single<ResponseBody>
}