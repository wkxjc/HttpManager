package com.example.httpmanager.commen.api

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Description:
 * ApiService 示例
 *
 * @author  Alpinist Wang
 * Company: Mobile CPX
 * Date:    2019/4/19
 */
interface DemoApiService {

    @GET("v1/vertical/vertical")
    fun getData(
            @Query("limit") limit: Int = 30,
            @Query("skip") skip: Int = 0,
            @Query("adult") adult: Boolean = false,
            @Query("first") first: Int = 0,
            @Query("order") order: String = "hot"
    ): Observable<String>

    @GET("v1/vertical/category")
    fun getCategory(
            @Query("adult") adult: Boolean = false,
            @Query("first") first: Int = 1
    ): Observable<String>
}