package com.luanmuc.openwrtmanager.ui.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 通知设置ViewModel
 */
class NotificationViewModel(application: Application) : BaseViewModel(application) {
    
    // 通知开关状态
    private val _notificationEnabled = MutableStateFlow(true)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()
    
    // 各个渠道的开关状态
    private val _routerStatusEnabled = MutableStateFlow(true)
    val routerStatusEnabled: StateFlow<Boolean> = _routerStatusEnabled.asStateFlow()
    
    private val _firmwareEnabled = MutableStateFlow(true)
    val firmwareEnabled: StateFlow<Boolean> = _firmwareEnabled.asStateFlow()
    
    private val _networkAlertEnabled = MutableStateFlow(true)
    val networkAlertEnabled: StateFlow<Boolean> = _networkAlertEnabled.asStateFlow()
    
    private val _deviceEventEnabled = MutableStateFlow(true)
    val deviceEventEnabled: StateFlow<Boolean> = _deviceEventEnabled.asStateFlow()
    
    // 通知历史记录
    private val _notificationHistory = MutableStateFlow<List<NotificationHistoryItem>>(emptyList())
    val notificationHistory: StateFlow<List<NotificationHistoryItem>> = _notificationHistory.asStateFlow()
    
    init {
        initNetworkMonitor()
        // 加载演示数据
        loadDemoData()
    }
    
    private fun loadDemoData() {
        // 演示模式的通知历史
        val demoHistory = listOf(
            NotificationHistoryItem(
                id = 1,
                title = "路由器已在线",
                content = "OpenWrt 路由器已恢复连接",
                time = System.currentTimeMillis() - 3600000,
                type = NotificationType.ROUTER_STATUS
            ),
            NotificationHistoryItem(
                id = 2,
                title = "新设备上线",
                content = "iPhone-15-Pro 已连接到网络",
                time = System.currentTimeMillis() - 7200000,
                type = NotificationType.DEVICE_EVENT
            ),
            NotificationHistoryItem(
                id = 3,
                title = "固件更新可用",
                content = "发现新版本 23.05.2，建议升级",
                time = System.currentTimeMillis() - 86400000,
                type = NotificationType.FIRMWARE
            )
        )
        _notificationHistory.value = demoHistory
    }
    
    fun setNotificationEnabled(enabled: Boolean) {
        _notificationEnabled.value = enabled
    }
    
    fun setRouterStatusEnabled(enabled: Boolean) {
        _routerStatusEnabled.value = enabled
    }
    
    fun setFirmwareEnabled(enabled: Boolean) {
        _firmwareEnabled.value = enabled
    }
    
    fun setNetworkAlertEnabled(enabled: Boolean) {
        _networkAlertEnabled.value = enabled
    }
    
    fun setDeviceEventEnabled(enabled: Boolean) {
        _deviceEventEnabled.value = enabled
    }
    
    fun clearHistory() {
        _notificationHistory.value = emptyList()
    }

    override fun refreshData() {
        // 无需刷新
    }
}

/**
 * 通知历史记录项
 */
data class NotificationHistoryItem(
    val id: Int = 0,
    val title: String = "",
    val content: String = "",
    val time: Long = 0,
    val type: NotificationType = NotificationType.ROUTER_STATUS
)

/**
 * 通知类型
 */
enum class NotificationType(val displayName: String) {
    ROUTER_STATUS("路由器状态"),
    FIRMWARE("固件更新"),
    NETWORK_ALERT("网络告警"),
    DEVICE_EVENT("设备事件")
}
