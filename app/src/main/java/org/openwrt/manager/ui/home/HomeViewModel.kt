package org.openwrt.manager.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openwrt.manager.data.model.Router
import org.openwrt.manager.data.model.RouterStatus
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
        val hasRouter: Boolean = false
    )

    init {
        loadActiveRouter()
    }

    private fun loadActiveRouter() {
        viewModelScope.launch {
            routerRepository.activeRouterId.collect { routerId ->
                if (routerId != null) {
                    val router = routerRepository.getRouterById(routerId)
                    _uiState.value = _uiState.value.copy(
                        activeRouter = router,
                        hasRouter = router != null
                    )
                    if (router != null) {
                        loadRouterStatus(router)
                    }
                } else {
                    // 检查是否有路由器列表
                    routerRepository.routers.collect { routers ->
                        if (routers.isNotEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                activeRouter = routers.first(),
                                hasRouter = true
                            )
                            loadRouterStatus(routers.first())
                        } else {
                            _uiState.value = _uiState.value.copy(
                                hasRouter = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadRouterStatus(router: Router) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val password = EncryptionUtil.decrypt(router.encryptedPassword)
                luciRepository.login(router.address, router.username, password)
                val status = luciRepository.getRouterStatus()
                _uiState.value = _uiState.value.copy(
                    routerStatus = status,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "连接失败"
                )
            }
        }
    }

    fun refresh() {
        _uiState.value.activeRouter?.let {
            loadRouterStatus(it)
        }
    }

    fun reboot() {
        viewModelScope.launch {
            try {
                luciRepository.reboot()
            } catch (e: Exception) {
                // 忽略，重启会断开连接
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
