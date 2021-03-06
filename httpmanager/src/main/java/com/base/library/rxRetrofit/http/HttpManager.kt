package com.base.library.rxRetrofit.http

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.base.library.rxRetrofit.RxRetrofitApp
import com.base.library.rxRetrofit.common.extension.bindIOToMainThread
import com.base.library.rxRetrofit.common.retry.RetryFunction
import com.base.library.rxRetrofit.http.api.BaseApi
import com.base.library.rxRetrofit.http.converter.HttpResultConverter
import com.base.library.rxRetrofit.http.httpList.HttpListConfig
import com.base.library.rxRetrofit.http.httpList.HttpListListener
import com.base.library.rxRetrofit.http.httpList.HttpListObserver
import com.base.library.rxRetrofit.http.listener.HttpListener
import com.base.library.rxRetrofit.http.observer.HttpObserver
import io.reactivex.Observable

/**
 * Description:
 * HttpManager，用来进行单个api请求或多个api请求
 *
 * @author  WZG
 * Date:    2019-04-25
 */
@Suppress("unused")
class HttpManager {
    private var activity: AppCompatActivity? = null
    private var fragment: Fragment? = null
    private val context: Context
        get() = activity ?: fragment?.context ?: RxRetrofitApp.application.applicationContext
        ?: throw Throwable("context is null")

    constructor(activity: AppCompatActivity) {
        this.activity = activity
    }

    constructor(fragment: Fragment) {
        this.fragment = fragment
    }

    /**
     * 如果不传Activity和Fragment，默认使用application的Context
     */
    constructor()

    /**
     * 单个api请求
     */
    fun request(api: BaseApi, listener: HttpListener) {
        api.getObservable()
            /*失败后retry处理控制*/
            .retryWhen(RetryFunction(api.apiConfig.retry))
            /*返回数据统一判断*/
            .map(HttpResultConverter(api))
            .bindIOToMainThread()
            .subscribe(HttpObserver(activity, fragment, context, api, listener))
    }

    /**
     * 多个api请求
     */
    fun request(
        apis: Array<out BaseApi>,
        listener: HttpListListener,
        config: HttpListConfig = RxRetrofitApp.httpListConfig
    ) {
        val resultMap = HashMap<BaseApi, Any>()
        val observable = with(Observable.fromArray(*apis)) {
            if (config.order) this.concatMap { requestSingleApi(it, resultMap, listener) }
            else this.flatMap { requestSingleApi(it, resultMap, listener) }
        }
        observable.buffer(apis.size)
            .bindIOToMainThread()
            .subscribe(HttpListObserver(activity, fragment, context, resultMap, config, listener))
    }

    private fun requestSingleApi(
        api: BaseApi,
        resultMap: HashMap<BaseApi, Any>,
        listener: HttpListListener
    ): Observable<Unit> {
        return api.getObservable()
            /*失败后retry处理控制*/
            .retryWhen(RetryFunction(api.apiConfig.retry))
            /*返回数据统一判断*/
            .map(HttpResultConverter(api))
            .map { resultMap[api] = listener.onSingleNext(api, it) }
            .bindIOToMainThread()
    }
}