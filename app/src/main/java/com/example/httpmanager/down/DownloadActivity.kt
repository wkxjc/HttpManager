package com.example.httpmanager.down

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.base.library.rxRetrofit.download.HttpDownManager
import com.base.library.rxRetrofit.download.bean.DownloadProgress
import com.base.library.rxRetrofit.download.config.DownConfig
import com.base.library.rxRetrofit.download.listener.HttpDownListener
import com.example.httpmanager.R
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_download.*

class DownloadActivity : RxAppCompatActivity() {
    private val config = DownConfig().apply {
        url = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
//        url = "http://dldir1.qq.com/qqmi/aphone_p2p/TencentVideo_V6.0.0.14297_848.apk"
        /**进度更新频率，下载多少Byte后更新一次进度。默认每下载4KB更新一次，这里设置为每512KB更新一次。
         * 使用[DownConfig.PROGRESS_BY_PERCENT]表示每下载百分之一更新一次*/
        progressStep = 1024 * 512
    }

    private val httpDownListener = object : HttpDownListener() {
        override fun onProgress(downloadProgress: DownloadProgress) {
            Log.d("~~~", "onProgress:$downloadProgress")
            tvProgress.text = "下载中: ${downloadProgress.memoryProgress}"
            progressBar.progress = downloadProgress.progress
        }

        override fun onComplete() {
            Log.d("~~~", "onComplete")
            tvProgress.text = "下载完成"
            // 有可能下完时没有达到更新进度要求progressStep，会导致进度条没有走到100%，所以手动设置到100%。
            // 当然也可以在HttpDownManager回调onComplete之前回调一次onProgress(100)，但是笔者为了保持库的简洁所以没这么做
            progressBar.progress = 100
            showDownloadIcon()
        }

        override fun onError(e: Throwable) {
            Log.d("~~~", "onError:$e")
            tvProgress.text = "下载出错，点击下载按钮继续下载"
            showDownloadIcon()
        }

        override fun onSubscribe(d: Disposable) {
            super.onSubscribe(d)
            Log.d("~~~", "onSubscribe：开始下载或继续下载")
            showPauseIcon()
        }

        override fun onPause() {
            super.onPause()
            Log.d("~~~", "onPause")
            tvProgress.text = "下载暂停"
            showDownloadIcon()
        }

        override fun onDelete() {
            super.onDelete()
            Log.d("~~~", "onDelete")
            tvProgress.text = "已删除"
            progressBar.progress = 0
            showDownloadIcon()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)
        HttpDownManager.bindListener(config, httpDownListener)
        initView()
        initDownState()
    }

    private fun initView() {
        tvName.text = config.saveFileName
        tvProgress.text = "尚未开始"
        ivDownload.setOnClickListener {
            if (HttpDownManager.isDownloading(config)) {
                HttpDownManager.pause(config)
            } else {
                if (HttpDownManager.isCompleted(config)) {
                    Toast.makeText(
                        this@DownloadActivity,
                        "已经下载完成，如果需要重新下载，请先删除下载文件",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                HttpDownManager.down(config)
            }
        }
        ivDelete.setOnClickListener {
            HttpDownManager.delete(config)
        }
    }

    /**
     * 初始化下载任务的状态
     */
    private fun initDownState() {
        when {
            // 显示正在下载状态
            HttpDownManager.isDownloading(config) -> {
                Log.d("~~~", "isDownloading")
                val downloadProgress = HttpDownManager.getProgress(config)
                tvProgress.text = "下载中: ${downloadProgress.memoryProgress}"
                progressBar.progress = downloadProgress.progress
                showPauseIcon()
            }
            // 显示已完成状态
            HttpDownManager.isCompleted(config) -> {
                tvProgress.text = "下载完成"
                progressBar.progress = 100
                showDownloadIcon()
            }
            // 显示暂停状态
            HttpDownManager.isPause(config) -> {
                val downloadProgress = HttpDownManager.getProgress(config)
                tvProgress.text = "下载暂停: ${downloadProgress.memoryProgress}"
                progressBar.progress = downloadProgress.progress
                showDownloadIcon()
            }
            // 显示出错状态
            HttpDownManager.isError(config) -> {
                val downloadProgress = HttpDownManager.getProgress(config)
                tvProgress.text = "下载出错，点击下载按钮继续下载: ${downloadProgress.memoryProgress}"
                progressBar.progress = downloadProgress.progress
                showDownloadIcon()
            }
        }
    }

    private fun showDownloadIcon() {
        ivDownload.setImageResource(android.R.drawable.stat_sys_download)
    }

    private fun showPauseIcon() {
        ivDownload.setImageResource(android.R.drawable.ic_media_pause)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭页面时，解绑监听器，防止内存泄漏
        HttpDownManager.unbindListener(config)
    }
}