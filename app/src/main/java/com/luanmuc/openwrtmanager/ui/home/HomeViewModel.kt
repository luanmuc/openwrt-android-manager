package com.luanmuc.openwrtmanager.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.*
import com.luanmuc.openwrtmanager.data.repository.LuciException
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 首页 ViewModel
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    data class HomeUiState(
        val activeRouter: Router? = null,
        val routerStatus: RouterStatus? = null,
        val wanStatus: NetworkInterface? = null,
        val onlineDevices: List<DeviceInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val hasRouter: Boolean = false,
        val isRefreshing: Boolean = false,
        val cpuHistory: List<CpuDataPoint> = emptyList(),
        val trafficHistory: List<TrafficDataPoint> = emptyList(),
        val autoRefresh: Boolean = true,
        val downloadSpeed: Long = 0,
        val uploadSpeed: Long = 0
    )

    init {
        observeRouters()
    }

    /**
     * 观察路由器数据变化
     */
    private fun observeRouters() {
        viewModelScope.launch {
            try {
                combine(
                    routerRepository.routers,
                    routerRepository.activeRouterId
                ) { routers, activeId ->
                    val activeRouter = if (activeId != null) {
                        routers.find { it.id == activeId } ?: routers.firstOrNull()
                    } else {
                        routers.firstOrNull()
                    }
                    Pair(routers, activeRouter)
                }.collect { (routers, activeRouter) ->
                    try {
                        _uiState.value = _uiState.value.copy(
                            activeRouter = activeRouter,
                            hasRouter = routers.isNotEmpty()
                        )
                        if (activeRouter != null && _uiState.value.routerStatus == null) {
                            loadAllData(activeRouter)
                            startAutoRefresh()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 开始自动刷新
     */
    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (_uiState.value.autoRefresh && _uiState.value.activeRouter != null) {
                delay(5000)
                if (_uiState.value.autoRefresh && !_uiState.value.isLoading) {
                    refreshSilent()
                }
            }
        }
    }

    /**
     * 静默刷新
     */
    private fun refreshSilent() {
        _uiState.value.activeRouter?.let { router ->
            viewModelScope.launch {
                try {
                    val password = EncryptionUtil.decrypt(router.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(router.address, router.username, password)
                    }

                    val status = luciRepository.getRouterStatus()
                    val wan = luciRepository.getWanStatus()
                    val devices = luciRepository.getDhcpLeases()

                    val now = System.currentTimeMillis()
                    val newCpuPoint = CpuDataPoint(time = now, usage = status.cpuUsage)
                    val cpuHistory = (_uiState.value.cpuHistory + newCpuPoint).takeLast(20)

                    val wanRx = wan?.rxBytes ?: 0
                    val wanTx = wan?.txBytes ?: 0
                    val newTrafficPoint = TrafficDataPoint(time = now, rx = wanRx, tx = wanTx)
                    val trafficHistory = (_uiState.value.trafficHistory + newTrafficPoint).takeLast(20)

                    // 计算实时速度
                    val lastPoint = _uiState.value.trafficHistory.lastOrNull()
                    val downloadSpeed = if (lastPoint != null && now > lastPoint.time) {
                        val timeDiff = (now - lastPoint.time) / 1000.0
                        if (timeDiff > 0) {
                            ((wanRx - lastPoint.rx) / timeDiff).toLong()
                        } else 0
                    } else 0

                    val uploadSpeed = if (lastPoint != null && now > lastPoint.time) {
                        val timeDiff = (now - lastPoint.time) / 1000.0
                        if (timeDiff > 0) {
                            ((wanTx - lastPoint.tx) / timeDiff).toLong()
                        } else 0
                    } else 0

                    _uiState.value = _uiState.value.copy(
                        routerStatus = status.copy(
                            onlineDevices = devices.size,
                            wanConnected = wan?.isConnected == true || wan?.isUp == true,
                            wanIp = wan?.ipaddr ?: "",
                            wanUptime = wan?.uptime ?: 0
                        ),
                        wanStatus = wan,
                        onlineDevices = devices,
                        cpuHistory = cpuHistory,
                        trafficHistory = trafficHistory,
                        downloadSpeed = downloadSpeed,
                        uploadSpeed = uploadSpeed,
                        error = null
                    )
                } catch (e: Exception) {
                    // 静默刷新失败不显示错误
                }
            }
        }
    }

    /**
     * 加载所有数据
     */
    private fun loadAllData(router: Router) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                isRefreshing = _uiState.value.routerStatus != null,
                error = null
            )
            try {
                val password = EncryptionUtil.decrypt(router.encryptedPassword)
                luciRepository.login(router.address, router.username, password)

                val status = luciRepository.getRouterStatus()
                val wan = luciRepository.getWanStatus()
                val devices = luciRepository.getDhcpLeases()

                val now = System.currentTimeMillis()
                val cpuHistory = listOf(CpuDataPoint(time = now, usage = status.cpuUsage))
                val trafficHistory = listOf(
                    TrafficDataPoint(
                        time = now,
                        rx = wan?.rxBytes ?: 0,
                        tx = wan?.txBytes ?: 0
                    )
                )

                _uiState.value = _uiState.value.copy(
                    routerStatus = status.copy(
                        onlineDevices = devices.size,
                        wanConnected = wan?.isConnected == true,
                        wanIp = wan?.ipaddr ?: "",
                        wanUptime = wan?.uptime ?: 0
                    ),
                    wanStatus = wan,
                    onlineDevices = devices,
                    isLoading = false,
                    isRefreshing = false,
                    cpuHistory = cpuHistory,
                    trafficHistory = trafficHistory,
                    error = null
                )
            } catch (e: LuciException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "连接失败"
                )
            }
        }
    }

    /**
     * 手动刷新
     */
    fun refresh() {
        _uiState.value.activeRouter?.let {
            loadAllData(it)
        }
    }

    /**
     * 重启路由器
     */
    fun reboot() {
        viewModelScope.launch {
            try {
                luciRepository.reboot()
            } catch (e: Exception) {
                // 重启会断开连接，忽略错误
            }
        }
    }

    /**
     * 关机
     */
    fun shutdown() {
        viewModelScope.launch {
            try {
                luciRepository.shutdown()
            } catch (e: Exception) {
                // 关机会断开连接，忽略错误
            }
        }
    }

    /**
     * 切换自动刷新
     */
    fun toggleAutoRefresh() {
        val newAutoRefresh = !_uiState.value.autoRefresh
        _uiState.value = _uiState.value.copy(autoRefresh = newAutoRefresh)
        if (newAutoRefresh) {
            startAutoRefresh()
        } else {
            refreshJob?.cancel()
        }
    }

    /**
     * 格式化运行时间
     */
    fun formatUptime(seconds: Long): String {
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        return when {
            days > 0 -> "${days}天${hours}小时"
            hours > 0 -> "${hours}小时${minutes}分钟"
            else -> "${minutes}分钟"
        }
    }

    /**
     * 格式化字节数
     */
    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1073741824 -> String.format("%.2f GB", bytes / 1073741824.0)
            bytes >= 1048576 -> String.format("%.2f MB", bytes / 1048576.0)
            bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
