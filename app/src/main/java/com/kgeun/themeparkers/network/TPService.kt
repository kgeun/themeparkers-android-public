package com.kgeun.themeparkers.network

import com.kgeun.themeparkers.util.getShowModeQuery
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface TPService {
    @GET("/attractions")
    fun getItemListStatic(@Query("tpCode") tpCode: String): Single<AttractionsListStaticResponse>

    @GET("/atrcDynamic")
    fun getAtrcDynamic(@Query("tpCode") tpCode: String, @Query("mode") mode: Int = getShowModeQuery(tpCode)): Single<AttractionDynamicResponse>

    @GET("/atrcStatistic")
    fun getAtrcStatistic(@Query("atCode") atCode: String, @Query("historyType") historyType: String?): Single<AttractionStatisticResponse>

    @GET("/attractions/detail")
    fun getItemDetail(@Query("atCode") atCode: String): Single<AttractionDetailResponse>

    @GET("/themeparks")
    fun getThemeparks(): Single<ThemeparkResponse>

    @GET("/articles")
    fun getArticles(@Query("offset") offset: Long?): Single<ArticleResponse>
}