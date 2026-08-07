package com.luanmuc.openwrtmanager.ui.home

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 首页仪表板配置
 * 支持卡片排序、显示/隐藏、大小调整
 */
class DashboardConfig(private val context: Context) {

    val config: Flow<List<DashboardCard>> = flowOf(getDefaultConfig())

    suspend fun saveConfig(cards: List<DashboardCard>) {
        // 暂时使用默认配置，后续完善
    }

    suspend fun resetToDefault() {
        // 暂时使用默认配置，后续完善
    }

    private fun getDefaultConfig(): List<DashboardCard> {
        return listOf(
            DashboardCard(
                id = CardType.ROUTER_STATUS,
                visible = true,
                size = CardSize.LARGE,
                order = 0
            ),
            DashboardCard(
                id = CardType.NETWORK_SPEED,
                visible = true,
                size = CardSize.MEDIUM,
                order = 1
            ),
            DashboardCard(
                id = CardType.QUICK_ACTIONS,
                visible = true,
                size = CardSize.LARGE,
                order = 2
            ),
            DashboardCard(
                id = CardType.SYSTEM_STATUS,
                visible = true,
                size = CardSize.MEDIUM,
                order = 3
            ),
            DashboardCard(
                id = CardType.ONLINE_DEVICES,
                visible = true,
                size = CardSize.MEDIUM,
                order = 4
            ),
            DashboardCard(
                id = CardType.WIFI_STATUS,
                visible = true,
                size = CardSize.MEDIUM,
                order = 5
            )
        )
    }

    companion object {
        @Volatile
        private var instance: DashboardConfig? = null

        fun getInstance(context: Context): DashboardConfig {
            return instance ?: synchronized(this) {
                instance ?: DashboardConfig(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * 仪表板卡片配置
 */
data class DashboardCard(
    val id: CardType,
    val visible: Boolean,
    val size: CardSize,
    val order: Int
)

/**
 * 卡片类型
 */
enum class CardType(val displayName: String) {
    ROUTER_STATUS("路由器状态"),
    NETWORK_SPEED("实时网速"),
    QUICK_ACTIONS("常用功能"),
    SYSTEM_STATUS("系统状态"),
    ONLINE_DEVICES("在线设备"),
    WIFI_STATUS("WiFi状态"),
    PLUGINS("插件管理"),
    FIREWALL("防火墙"),
    DDNS("DDNS")
}

/**
 * 卡片尺寸
 */
enum class CardSize {
    SMALL,      // 小卡片（1/2宽度）
    MEDIUM,     // 中等卡片（全宽）
    LARGE       // 大卡片（全宽+大高度）
}

/**
 * 预设布局
 */
enum class PresetLayout(val displayName: String) {
    DEFAULT("默认布局"),
    COMPACT("紧凑布局"),
    DETAILED("详细布局"),
    MINIMAL("极简布局")
}
