package com.kgeun.themeparkers.network

import io.reactivex.Single
import retrofit2.http.*

interface KR_EVLDynamicService {
    @Headers("User-Agent: okhttp/3.9.0")
    @GET("epsvc/facility/getDynamicFacilities")
    fun getDynamicInfo(
        @Query("facilKindCd")
        facilKindCd: String = "CT00401",
        @Query("langCd")
        langCd: String = "KR"
    ): Single<KR_EVLDynamicResponse>
}