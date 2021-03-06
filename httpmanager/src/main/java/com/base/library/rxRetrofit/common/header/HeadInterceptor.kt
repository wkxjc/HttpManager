package com.base.library.rxRetrofit.common.header

import com.base.library.rxRetrofit.http.api.BaseApi
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Description:
 * Http 请求 head 拦截器
 *
 * @author  WZG
 * Date:    2019-05-04
 */
class HeadInterceptor(private val api: BaseApi, private val headers: Headers?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        // 添加api中的header信息
        if (headers != null) requestBuilder.headers(headers)
        val request = requestBuilder.method(original.method, original.body)
            .build()
        val response = chain.proceed(request)
        if (api.apiConfig.ignoreResponseProcessor) return response
        return api.apiConfig.httpResponseProcessor.handleResponse(request, response)
    }
}
