package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import com.luanmuc.openwrtmanager.data.model.HotPlugin
import com.luanmuc.openwrtmanager.data.model.PluginCategory
import com.luanmuc.openwrtmanager.data.model.PluginDetail
import com.luanmuc.openwrtmanager.data.model.PluginReview
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.delay

/**
 * 插件市场Repository
 * 负责插件市场的推荐、排行、分类、详情等功能
 */
class PluginMarketRepository private constructor(private val context: Context) {
    
    /**
     * 获取推荐插件
     */
    suspend fun getRecommendedPlugins(): List<HotPlugin> {
        if (DebugMode.isDebugMode) {
            delay(500)
            return getFakeRecommendedPlugins()
        }
        return emptyList()
    }
    
    /**
     * 获取热门插件排行
     */
    suspend fun getHotPlugins(category: String? = null, limit: Int = 20): List<HotPlugin> {
        if (DebugMode.isDebugMode) {
            delay(500)
            val plugins = getFakeHotPlugins()
            return if (category != null) {
                plugins.filter { it.category == category }.take(limit)
            } else {
                plugins.take(limit)
            }
        }
        return emptyList()
    }
    
    /**
     * 获取新上线插件
     */
    suspend fun getNewPlugins(limit: Int = 10): List<HotPlugin> {
        if (DebugMode.isDebugMode) {
            delay(500)
            return getFakeHotPlugins().filter { it.isNew }.take(limit)
        }
        return emptyList()
    }
    
    /**
     * 获取插件分类（更细致的分类）
     */
    suspend fun getPluginCategories(): List<PluginCategory> {
        if (DebugMode.isDebugMode) {
            return getFakeCategories()
        }
        return emptyList()
    }
    
    /**
     * 获取插件详情（增强版）
     */
    suspend fun getPluginDetail(packageName: String): PluginDetail? {
        if (DebugMode.isDebugMode) {
            delay(500)
            return getFakePluginDetail(packageName)
        }
        return null
    }
    
    /**
     * 获取插件评论
     */
    suspend fun getPluginReviews(packageName: String, limit: Int = 20): List<PluginReview> {
        if (DebugMode.isDebugMode) {
            delay(500)
            return getFakeReviews()
        }
        return emptyList()
    }
    
    /**
     * 搜索插件
     */
    suspend fun searchPlugins(query: String): List<HotPlugin> {
        if (DebugMode.isDebugMode) {
            delay(500)
            return getFakeHotPlugins().filter { 
                it.name.contains(query, true) || 
                it.displayName.contains(query, true) ||
                it.description.contains(query, true)
            }
        }
        return emptyList()
    }
    
    /**
     * 假数据：推荐插件
     */
    private fun getFakeRecommendedPlugins(): List<HotPlugin> {
        return listOf(
            HotPlugin(
                name = "luci-app-aria2",
                displayName = "Aria2下载管理器",
                description = "强大的多线程下载工具，支持HTTP/BT/磁力链接",
                icon = "⬇️",
                category = "下载工具",
                downloadCount = 15000,
                rating = 4.8f,
                isNew = false,
                isHot = true
            ),
            HotPlugin(
                name = "luci-app-transmission",
                displayName = "Transmission BT客户端",
                description = "轻量级BT下载客户端，Web界面管理",
                icon = "🌊",
                category = "下载工具",
                downloadCount = 12000,
                rating = 4.6f,
                isNew = false,
                isHot = true
            ),
            HotPlugin(
                name = "luci-app-samba4",
                displayName = "Samba4网络共享",
                description = "文件共享服务，支持Windows/Mac/Linux",
                icon = "📁",
                category = "文件共享",
                downloadCount = 10000,
                rating = 4.5f,
                isNew = false,
                isHot = true
            ),
            HotPlugin(
                name = "luci-app-wireguard",
                displayName = "WireGuard VPN",
                description = "现代、快速、安全的VPN协议",
                icon = "🔒",
                category = "VPN",
                downloadCount = 9000,
                rating = 4.9f,
                isNew = true,
                isHot = true
            ),
            HotPlugin(
                name = "luci-app-adblock",
                displayName = "广告过滤",
                description = "网络级广告过滤，保护隐私",
                icon = "🚫",
                category = "网络安全",
                downloadCount = 8500,
                rating = 4.4f,
                isNew = false,
                isHot = true
            )
        )
    }
    
    /**
     * 假数据：热门插件
     */
    private fun getFakeHotPlugins(): List<HotPlugin> {
        return getFakeRecommendedPlugins() + listOf(
            HotPlugin(
                name = "luci-app-openvpn",
                displayName = "OpenVPN",
                description = "经典的开源VPN解决方案",
                icon = "🔐",
                category = "VPN",
                downloadCount = 7500,
                rating = 4.3f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-minidlna",
                displayName = "MiniDLNA媒体服务器",
                description = "DLNA媒体服务器，投屏到电视",
                icon = "🎬",
                category = "多媒体",
                downloadCount = 7000,
                rating = 4.2f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-sqm",
                displayName = "SQM智能队列管理",
                description = "智能QoS，优化网络延迟",
                icon = "⚡",
                category = "网络优化",
                downloadCount = 6500,
                rating = 4.7f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-ttyd",
                displayName = "Web终端",
                description = "在浏览器中使用命令行终端",
                icon = "💻",
                category = "工具",
                downloadCount = 6000,
                rating = 4.5f,
                isNew = true,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-nlbwmon",
                displayName = "网络带宽监控",
                description = "按设备统计流量使用情况",
                icon = "📊",
                category = "监控",
                downloadCount = 5500,
                rating = 4.4f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-upnp",
                displayName = "UPnP",
                description = "通用即插即用，自动端口映射",
                icon = "🔌",
                category = "网络",
                downloadCount = 5000,
                rating = 4.1f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-wol",
                displayName = "网络唤醒",
                description = "通过网络唤醒局域网内的设备",
                icon = "🌙",
                category = "工具",
                downloadCount = 4500,
                rating = 4.3f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-ddns",
                displayName = "动态DNS",
                description = "动态域名解析服务",
                icon = "🌐",
                category = "网络",
                downloadCount = 8000,
                rating = 4.2f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-statistics",
                displayName = "流量统计",
                description = "详细的网络流量统计图表",
                icon = "📈",
                category = "监控",
                downloadCount = 4000,
                rating = 4.3f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-frps",
                displayName = "Frp服务端",
                description = "内网穿透服务端",
                icon = "🚀",
                category = "网络",
                downloadCount = 3500,
                rating = 4.6f,
                isNew = true,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-frpc",
                displayName = "Frp客户端",
                description = "内网穿透客户端",
                icon = "🛸",
                category = "网络",
                downloadCount = 3000,
                rating = 4.5f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-zerotier",
                displayName = "ZeroTier",
                description = "全球虚拟局域网",
                icon = "🌍",
                category = "VPN",
                downloadCount = 2500,
                rating = 4.4f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-vsftpd",
                displayName = "FTP服务器",
                description = "轻量级FTP服务器",
                icon = "📤",
                category = "文件共享",
                downloadCount = 2000,
                rating = 4.0f,
                isNew = false,
                isHot = false
            ),
            HotPlugin(
                name = "luci-app-udpxy",
                displayName = "UDPXY组播转单播",
                description = "IPTV组播转单播，方便多设备观看",
                icon = "📺",
                category = "多媒体",
                downloadCount = 1500,
                rating = 4.1f,
                isNew = false,
                isHot = false
            )
        )
    }
    
    /**
     * 假数据：插件分类
     */
    private fun getFakeCategories(): List<PluginCategory> {
        return listOf(
            PluginCategory(
                id = "download",
                name = "下载工具",
                icon = "⬇️",
                description = "BT下载、HTTP下载等",
                pluginCount = 5
            ),
            PluginCategory(
                id = "vpn",
                name = "VPN代理",
                icon = "🔒",
                description = "各种VPN和代理工具",
                pluginCount = 8
            ),
            PluginCategory(
                id = "fileshare",
                name = "文件共享",
                icon = "📁",
                description = "Samba、FTP、NFS等",
                pluginCount = 4
            ),
            PluginCategory(
                id = "media",
                name = "多媒体",
                icon = "🎬",
                description = "媒体服务器、DLNA等",
                pluginCount = 5
            ),
            PluginCategory(
                id = "network",
                name = "网络工具",
                icon = "🌐",
                description = "DDNS、内网穿透等",
                pluginCount = 10
            ),
            PluginCategory(
                id = "security",
                name = "网络安全",
                icon = "🛡️",
                description = "广告过滤、防火墙等",
                pluginCount = 6
            ),
            PluginCategory(
                id = "monitor",
                name = "监控统计",
                icon = "📊",
                description = "流量监控、系统监控等",
                pluginCount = 7
            ),
            PluginCategory(
                id = "optimize",
                name = "网络优化",
                icon = "⚡",
                description = "QoS、SQM等",
                pluginCount = 3
            ),
            PluginCategory(
                id = "tools",
                name = "实用工具",
                icon = "🔧",
                description = "终端、唤醒等工具",
                pluginCount = 8
            ),
            PluginCategory(
                id = "system",
                name = "系统工具",
                icon = "⚙️",
                description = "系统管理相关工具",
                pluginCount = 6
            )
        )
    }
    
    /**
     * 假数据：插件详情
     */
    private fun getFakePluginDetail(packageName: String): PluginDetail {
        return PluginDetail(
            name = packageName,
            displayName = "插件名称",
            description = "插件简短描述",
            longDescription = """
                这是一个功能强大的OpenWrt插件，可以帮助你更好地管理路由器。
                
                主要功能：
                • 功能一：详细说明
                • 功能二：详细说明
                • 功能三：详细说明
                • 功能四：详细说明
                
                安装说明：
                1. 点击安装按钮
                2. 等待安装完成
                3. 在菜单中找到并打开
                
                注意事项：
                • 安装前请确保有足够的存储空间
                • 部分功能需要特定的硬件支持
                • 如有问题请查看官方文档
            """.trimIndent(),
            icon = "📦",
            category = "工具",
            version = "1.0.0",
            size = 1024 * 1024,
            author = "OpenWrt社区",
            homepage = "https://openwrt.org",
            license = "GPL-2.0",
            screenshots = listOf(),
            tags = listOf("工具", "实用", "推荐"),
            dependencies = listOf("libc", "libuci"),
            downloadCount = 10000,
            rating = 4.5f,
            ratingCount = 100,
            lastUpdated = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000,
            isInstalled = false,
            isUpdateAvailable = false,
            latestVersion = "1.0.0"
        )
    }
    
    /**
     * 假数据：评论
     */
    private fun getFakeReviews(): List<PluginReview> {
        return listOf(
            PluginReview(
                id = "1",
                userName = "用户A",
                rating = 5f,
                comment = "非常好用的插件，功能很强大！",
                date = System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000,
                helpfulCount = 15
            ),
            PluginReview(
                id = "2",
                userName = "用户B",
                rating = 4f,
                comment = "整体不错，就是配置稍微复杂了一点。",
                date = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000,
                helpfulCount = 8
            ),
            PluginReview(
                id = "3",
                userName = "用户C",
                rating = 5f,
                comment = "一直在用，很稳定，推荐！",
                date = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000,
                helpfulCount = 12
            ),
            PluginReview(
                id = "4",
                userName = "用户D",
                rating = 3f,
                comment = "功能还行，但是希望能增加更多选项。",
                date = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000,
                helpfulCount = 3
            ),
            PluginReview(
                id = "5",
                userName = "用户E",
                rating = 5f,
                comment = "完美，正是我需要的！",
                date = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000,
                helpfulCount = 20
            )
        )
    }
    
    companion object {
        @Volatile
        private var instance: PluginMarketRepository? = null
        
        fun getInstance(context: Context): PluginMarketRepository {
            return instance ?: synchronized(this) {
                instance ?: PluginMarketRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
