package org.openwrt.manager.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.openwrt.manager.data.model.Router
import org.openwrt.manager.data.model.RouterStatus
import org.openwrt.manager.data.repository.LuciException
import org.openwrt.manager.data.repository.LuciRepository
import org.openwrt.manager.data.repository.RouterRepository
import org.openwrt.manager.util.EncryptionUtil

/**
 * 首页 ViewModel
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    data class HomeUiState(
        val activeRouter: Router? = null,
        val routerStatus: RouterStatus? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val hasRouter: Boolean = false,
        val isRefreshing: Boolean = false
    )

    init {
        observeRouters()
    }

    /**
     * 观察路由器数据变化
     */
    private fun observeRouters() {
        viewModelScope.launch {
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
                _uiState.value = _uiState.value.copy(
                    activeRouter = activeRouter,
                    hasRouter = routers.isNotEmpty()
                )
                if (activeRouter != null && _uiState.value.routerStatus == null) {
                    loadRouterStatus(activeRouter)
                }
            }
        }
    }

    /**
     * 加载路由器状态
     */
    private fun loadRouterStatus(router: Router) {
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
                _uiState.value = _uiState.value.copy(
                    routerStatus = status,
                    isLoading = false,
                    isRefreshing = false
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
     * 刷新
     */
    fun refresh() {
        _uiState.value.activeRouter?.let {
            loadRouterStatus(it)
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
}
