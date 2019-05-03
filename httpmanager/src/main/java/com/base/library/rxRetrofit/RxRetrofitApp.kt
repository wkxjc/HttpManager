package com.base.library.rxRetrofit

import android.app.Application
import com.base.library.rxRetrofit.http.bean.BaseApiConfig
import com.base.library.rxRetrofit.http.bean.DefaultResultFunc
import com.base.library.rxRetrofit.http.listener.IResultFunc

/**
 * Description:
 * 全局application
 *
 * @author  WZG
 * Company: Mobile CPX
 * Date:    2019-04-25
 */
object RxRetrofitApp {
    /**全局Application*/
    @JvmStatic
    var application: Application? = null
        private set
    /**返回数据统一处理*/
    @JvmStatic
    lateinit var resultFunc: IResultFunc
        private set
    /**全局修改BaseApi配置*/
    @JvmStatic
    lateinit var apiConfig: BaseApiConfig
        private set

    /**
     * 初始化RxRetrofit库
     * @param application 全局Application
     * @param resultFunc 返回数据统一处理
     * @param apiConfig 全局修改BaseApi配置
     */
    fun init(
        application: Application,
        resultFunc: IResultFunc = DefaultResultFunc(),
        apiConfig: BaseApiConfig = BaseApiConfig()
    ) {
        this.application = application
        this.resultFunc = resultFunc
        this.apiConfig = apiConfig
    }
}
