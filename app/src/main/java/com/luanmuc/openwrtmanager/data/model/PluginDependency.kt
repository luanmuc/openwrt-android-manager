package com.luanmuc.openwrtmanager.data.model

/**
 * 插件依赖信息
 */
data class PluginDependency(
    val featureName: String,       // 功能名称
    val packageName: String,       // 插件包名
    val description: String,       // 插件说明
    val isRequired: Boolean = true, // 是否必需
    val installCommand: String = "", // 安装命令（默认opkg install）
    val category: PluginCategory = PluginCategory.OTHER
)

/**
 * 插件分类
 */
enum class PluginCategory {
    NETWORK,      // 网络相关
    WIFI,         // WiFi相关
    SYSTEM,       // 系统相关
    STORAGE,      // 存储相关
    DIAGNOSTIC,   // 诊断相关
    OTHER         // 其他
}

/**
 * 插件安装状态
 */
data class PluginInstallStatus(
    val dependency: PluginDependency,
    val isInstalled: Boolean,
    val isInstalling: Boolean = false,
    val installProgress: Int = 0,
    val installMessage: String = "",
    val installError: String = ""
)

/**
 * 插件依赖配置
 * 定义每个功能需要的插件
 */
object PluginDependencies {
    // DDNS功能
    val DDNS = PluginDependency(
        featureName = "DDNS动态域名",
        packageName = "ddns-scripts",
        description = "DDNS动态域名更新服务，用于将动态IP映射到固定域名",
        category = PluginCategory.NETWORK
    )

    // 设备限速功能
    val TRAFFIC_CONTROL = PluginDependency(
        featureName = "设备限速",
        packageName = "ip-tiny",
        description = "流量控制工具(tc命令)，用于限制设备上下行速度",
        category = PluginCategory.NETWORK
    )

    // 挂载点管理
    val BLOCK_MOUNT = PluginDependency(
        featureName = "挂载点管理",
        packageName = "block-mount",
        description = "块设备挂载管理工具，用于管理USB存储等挂载点",
        category = PluginCategory.STORAGE
    )

    // WiFi信息
    val IWINFO = PluginDependency(
        featureName = "WiFi信息",
        packageName = "rpcd-mod-iwinfo",
        description = "WiFi信息查询模块，用于获取无线设备状态和连接信息",
        category = PluginCategory.WIFI
    )

    // 系统日志
    val LOGD = PluginDependency(
        featureName = "系统日志",
        packageName = "logd",
        description = "系统日志服务，用于记录和查看系统运行日志",
        category = PluginCategory.SYSTEM
    )

    // 诊断工具-traceroute
    val TRACEROUTE = PluginDependency(
        featureName = "路由追踪",
        packageName = "traceroute",
        description = "路由追踪工具，用于诊断网络路径和延迟",
        isRequired = false,
        category = PluginCategory.DIAGNOSTIC
    )

    // 所有必需插件列表
    val ALL_REQUIRED = listOf(
        DDNS,
        TRAFFIC_CONTROL,
        BLOCK_MOUNT,
        IWINFO,
        LOGD
    )

    // 所有插件列表（含可选）
    val ALL = listOf(
        DDNS,
        TRAFFIC_CONTROL,
        BLOCK_MOUNT,
        IWINFO,
        LOGD,
        TRACEROUTE
    )
}
