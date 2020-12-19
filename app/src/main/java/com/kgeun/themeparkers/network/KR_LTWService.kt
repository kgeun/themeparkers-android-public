package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.util.getToday
import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.*

interface KR_LTWService {

    @GET("/kor/main/index.do")
    fun getOpertimeInfo(): Single<ResponseBody>
}