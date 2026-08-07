package org.openwrt.manager

import android.app.Application
import org.openwrt.manager.data.repository.RouterRepository

/**
 * Application 类
 */
class OpenWrtApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // 初始化仓库
        RouterRepository.getInstance(this)
    }
}
