package com.luanmuc.openwrtmanager.ui.firewall

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.PortForwardRule
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 防火墙 ViewModel
 */
class FirewallViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(FirewallUiState())
    val uiState: StateFlow<FirewallUiState> = _uiState.asStateFlow()

    data class FirewallUiState(
        val portForwards: List<PortForwardRule> = emptyList(),
        val firewallEnabled: Boolean = true,
        val dmzEnabled: Boolean = false,
        val dmzIp: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val success: String? = null,
        val hasRouter: Boolean = false
    )

    init {
        observeRouters()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty()) {
                    loadFirewallConfig()
                }
            }
        }
    }

    fun loadFirewallConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            try {
                // 调试模式：使用假数据
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(600)
                    val portForwards = DebugMode.getFakePortForwards()
                    _uiState.value = _uiState.value.copy(
                        portForwards = portForwards,
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
                    // 加载端口转发规则
                    val portForwards = luciRepository.getPortForwards()
                    _uiState.value = _uiState.value.copy(
                        portForwards = portForwards,
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

    fun deletePortForward(name: String) {
        viewModelScope.launch {
            try {
                // 调试模式：模拟删除
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(500)
                    val portForwards = _uiState.value.portForwards.filter { it.name != name }
                    _uiState.value = _uiState.value.copy(
                        portForwards = portForwards,
                        success = "规则删除成功"
                    )
                    return@launch
                }

                // 删除端口转发规则
                luciRepository.deletePortForward(name)
                luciRepository.commitUci("firewall")
                // 重新加载
                loadFirewallConfig()
                _uiState.value = _uiState.value.copy(success = "规则删除成功")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "删除失败")
            }
        }
    }

    fun addPortForward(rule: PortForwardRule) {
        viewModelScope.launch {
            try {
                // 调试模式：模拟添加
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(800)
                    val portForwards = _uiState.value.portForwards + rule
                    _uiState.value = _uiState.value.copy(
                        portForwards = portForwards,
                        success = "规则添加成功"
                    )
                    return@launch
                }

                // 添加端口转发规则
                luciRepository.addPortForward(rule)
                luciRepository.commitUci("firewall")
                // 重新加载
                loadFirewallConfig()
                _uiState.value = _uiState.value.copy(success = "规则添加成功")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "添加失败")
            }
        }
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
