package com.base.library.rxRetrofit.http

import android.content.Context
import com.base.library.rxRetrofit.http.api.BaseApi
import com.base.library.rxRetrofit.http.func.ResultFunc
import com.base.library.rxRetrofit.http.func.RetryFunc
import com.base.library.rxRetrofit.http.listener.HttpListListener
import com.base.library.rxRetrofit.http.listener.HttpListener
import com.base.library.rxRetrofit.http.observer.HttpListObserver
import com.base.library.rxRetrofit.http.observer.HttpObserver
import com.base.library.rxRetrofit.http.utils.bind
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import com.trello.rxlifecycle3.components.support.RxFragment
import io.reactivex.Observable

/**
 * Description:
 * HttpManager
 *
 * @author  WZG
 * Company: Mobile CPX
 * Date:    2019-04-25
 */
class HttpManager {

    private var activity: RxAppCompatActivity? = null
    private var fragment: RxFragment? = null
    private val context: Context
        get() = activity ?: fragment?.context ?: throw Throwable("activity or fragment is null")

    constructor(activity: RxAppCompatActivity) {
        this.activity = activity
    }

    constructor(fragment: RxFragment) {
        this.fragment = fragment
    }

    fun request(api: BaseApi, listener: HttpListener) {
        api.getObservable()
                /*失败后retry处理控制*/
                .retryWhen(RetryFunc(api.retry))
                /*返回数据统一判断*/
                .map(ResultFunc(api))
                .bind(fragment, activity)
                .subscribe(HttpObserver(context, api, listener))
    }

    fun request(apis: Array<BaseApi>, listener: HttpListListener) {
        val resultMap = HashMap<BaseApi, Any>()
        val errorMap = HashMap<BaseApi, Throwable>()
        Observable.fromArray(*apis)
                .flatMap { api ->
                    api.getObservable()
                            /*失败后retry处理控制*/
                            .retryWhen(RetryFunc(api.retry))
                            /*返回数据统一判断*/
                            .map(ResultFunc(api))
                            .map {
                                resultMap[api] = listener.onSingleNext(api, it)
                            }
                            .bind(fragment, activity)
                }
                .buffer(apis.size)
                .bind(fragment, activity)
                .subscribe(HttpListObserver(resultMap, errorMap, listener))
    }
}