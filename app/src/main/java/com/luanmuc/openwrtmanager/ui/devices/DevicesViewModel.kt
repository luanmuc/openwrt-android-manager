package com.luanmuc.openwrtmanager.ui.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.DebugMode

/**
 * 设备页 ViewModel
 */
class DevicesViewModel(application: Application) : BaseViewModel(application) {
    private val repository = RouterRepository.getInstance(application)

    private val _uiState = MutableStateFlow(DevicesUiState())
    val uiState: StateFlow<DevicesUiState> = _uiState.asStateFlow()

    data class DevicesUiState(
        val routers: List<Router> = emptyList(),
        val activeRouterId: String? = null,
        val isLoading: Boolean = true,
        val isDemoMode: Boolean = false
    )

    init {
        initNetworkMonitor()
        loadRouters()
    }

    private fun loadRouters() {
        viewModelScope.launch {
            repository.routers.collect { routers ->
                // 如果是演示模式，添加演示路由器
                val allRouters = if (DebugMode.isDebugMode) {
                    listOf(DebugMode.getDemoRouter()) + routers
                } else {
                    routers
                }
                _uiState.value = _uiState.value.copy(
                    routers = allRouters,
                    isLoading = false,
                    isDemoMode = DebugMode.isDebugMode
                )
            }
        }

        viewModelScope.launch {
            repository.activeRouterId.collect { id ->
                _uiState.value = _uiState.value.copy(activeRouterId = id)
            }
        }
    }

    fun setActiveRouter(routerId: String) {
        viewModelScope.launch {
            // 如果是演示路由器，开启演示模式
            if (DebugMode.isDemoRouter(routerId)) {
                DebugMode.enableDemoMode()
                repository.setActiveRouter(routerId)
            } else {
                repository.setActiveRouter(routerId)
            }
        }
    }

    fun deleteRouter(routerId: String) {
        viewModelScope.launch {
            // 演示路由器不能删除
            if (DebugMode.isDemoRouter(routerId)) {
                return@launch
            }
            repository.removeRouter(routerId)
            if (_uiState.value.activeRouterId == routerId) {
                repository.setActiveRouter(null)
            }
        }
    }

    /**
     * 添加演示路由器
     */
    fun addDemoRouter() {
        viewModelScope.launch {
            DebugMode.enableDemoMode()
            repository.setActiveRouter("demo-router")
            loadRouters()
        }
    }

    override fun refreshData() {
        loadRouters()
    }
}
