package com.example.httpmanager.httpList

import android.os.Bundle
import com.base.library.rxRetrofit.http.HttpManager
import com.base.library.rxRetrofit.http.api.BaseApi
import com.base.library.rxRetrofit.http.httpList.HttpListConfig
import com.base.library.rxRetrofit.http.httpList.HttpListListener
import com.example.httpmanager.R
import com.example.httpmanager.commen.api.BannerApi
import com.example.httpmanager.commen.api.HomeArticlesApi
import com.example.httpmanager.commen.bean.Articles
import com.example.httpmanager.commen.bean.BannerBean
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_http_list.*

/**
 * Description:
 * 多接口合并请求示例
 *
 * @author  Alpinist Wang
 * Date:    2019-05-06
 */
class HttpListActivity : RxAppCompatActivity() {
    private val httpManager by lazy { HttpManager(this) }
    private val homeArticlesApi by lazy { HomeArticlesApi() }
    private val bannerApi by lazy { BannerApi() }
    private val apis = arrayOf(homeArticlesApi, bannerApi)
    private val listener = object : HttpListListener() {

        override fun onSingleNext(api: BaseApi, result: String): Any {
            return when (api) {
                // 这里可以将返回的字符串转换为任意对象，一般在这里使用Gson/fastJson解析对象
                // 对数据的解析或其他处理都在api中进行，此界面只做展示相关处理
                homeArticlesApi -> homeArticlesApi.convert(result)
                bannerApi -> bannerApi.convert(result)
                else -> super.onSingleNext(api, result)
            }
        }

        override fun onNext(resultMap: HashMap<BaseApi, Any>) {
            // 通过 as 方法，将resultMap中保存的对象取出并转换成onSingleNext返回的类型
            val articles = resultMap[homeArticlesApi] as Articles
            tvResultList.append("获取首页文章结果示例：${articles.datas.firstOrNull()?.title}\n")
            val banners: List<BannerBean> = resultMap[bannerApi] as List<BannerBean>
            tvResultList.append("获取首页 Banner 结果示例：${banners.firstOrNull()?.title}\n\n")
        }

        override fun onError(error: Throwable) {
            tvResultList.text = error.message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_http_list)
        initView()
    }

    private fun initView() {
        btnRequestList.setOnClickListener {
            httpManager.request(apis, listener)
        }
    }
}
