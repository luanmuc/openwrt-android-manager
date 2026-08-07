package com.luanmuc.openwrtmanager.ui.plugins

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.PackageInfo
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 插件页 ViewModel
 */
class PluginsViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(PluginsUiState())
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    data class PluginsUiState(
        val installedPackages: List<PackageInfo> = emptyList(),
        val availablePackages: List<PackageInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val hasRouter: Boolean = false,
        val actionLoading: String? = null
    )

    init {
        observeRouters()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty() && _uiState.value.installedPackages.isEmpty()) {
                    loadPackages()
                }
            }
        }
    }

    fun loadPackages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            try {
                // 调试模式：使用假数据
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(800)
                    val installed = DebugMode.getFakeInstalledPackages()
                    val available = DebugMode.getFakeAvailablePackages()
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available,
                        isLoading = false,
                        error = null
                    )
                    return@launch
                }

                val activeRouter = getActiveRouter()
                if (activeRouter != null) {
                    val password = EncryptionUtil.decrypt(activeRouter.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(activeRouter.address, activeRouter.username, password)
                    }
                    val installed = luciRepository.getInstalledPackages()
                    val available = luciRepository.getAvailablePackages()
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun installPackage(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionLoading = name)
            try {
                // 调试模式：模拟安装
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(1500)
                    val installed = _uiState.value.installedPackages.toMutableList()
                    val available = _uiState.value.availablePackages.toMutableList()
                    val pkg = available.find { it.name == name }
                    if (pkg != null) {
                        available.remove(pkg)
                        installed.add(pkg.copy(installed = true))
                    }
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available
                    )
                    return@launch
                }

                val success = luciRepository.installPackage(name)
                if (success) {
                    loadPackages()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "安装失败"
                )
            } finally {
                _uiState.value = _uiState.value.copy(actionLoading = null)
            }
        }
    }

    fun removePackage(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionLoading = name)
            try {
                // 调试模式：模拟卸载
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(1000)
                    val installed = _uiState.value.installedPackages.toMutableList()
                    val available = _uiState.value.availablePackages.toMutableList()
                    val pkg = installed.find { it.name == name }
                    if (pkg != null) {
                        installed.remove(pkg)
                        available.add(pkg.copy(installed = false))
                    }
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available
                    )
                    return@launch
                }

                val success = luciRepository.removePackage(name)
                if (success) {
                    loadPackages()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "卸载失败"
                )
            } finally {
                _uiState.value = _uiState.value.copy(actionLoading = null)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private suspend fun getActiveRouter(): Router? {
        val routers = routerRepository.getRoutersList()
        val activeId = routerRepository.getActiveRouterId()
        return if (activeId != null) {
            routers.find { it.id == activeId } ?: routers.firstOrNull()
        } else {
            routers.firstOrNull()
        }
    }
}
