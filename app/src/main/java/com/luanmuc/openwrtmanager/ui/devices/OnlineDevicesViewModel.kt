package com.luanmuc.openwrtmanager.ui.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.DeviceInfo
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 在线设备 ViewModel
 */
class OnlineDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(OnlineDevicesUiState())
    val uiState: StateFlow<OnlineDevicesUiState> = _uiState.asStateFlow()

    data class OnlineDevicesUiState(
        val devices: List<DeviceInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val hasRouter: Boolean = false,
        val sortBy: SortBy = SortBy.IP
    )

    enum class SortBy {
        IP, NAME, CONNECTED_TIME
    }

    init {
        observeRouters()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty() && _uiState.value.devices.isEmpty()) {
                    loadDevices()
                }
            }
        }
    }

    fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            try {
                val activeRouter = getActiveRouter()
                if (activeRouter != null) {
                    val password = EncryptionUtil.decrypt(activeRouter.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(activeRouter.address, activeRouter.username, password)
                    }

                    val dhcpLeases = luciRepository.getDhcpLeases()
                    val arpTable = luciRepository.getArpTable()

                    // 合并DHCP租约和ARP表
                    val deviceMap = mutableMapOf<String, DeviceInfo>()
                    dhcpLeases.forEach { device ->
                        if (device.mac.isNotEmpty()) {
                            deviceMap[device.mac] = device
                        }
                    }
                    arpTable.forEach { device ->
                        if (device.mac.isNotEmpty()) {
                            val existing = deviceMap[device.mac]
                            if (existing != null) {
                                deviceMap[device.mac] = existing.copy(
                                    interfaceName = device.interfaceName
                                )
                            } else {
                                deviceMap[device.mac] = device
                            }
                        }
                    }

                    val devices = deviceMap.values.toList()
                    _uiState.value = _uiState.value.copy(
                        devices = sortDevices(devices, _uiState.value.sortBy),
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

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSortBy(sortBy: SortBy) {
        _uiState.value = _uiState.value.copy(
            sortBy = sortBy,
            devices = sortDevices(_uiState.value.devices, sortBy)
        )
    }

    private fun sortDevices(devices: List<DeviceInfo>, sortBy: SortBy): List<DeviceInfo> {
        return when (sortBy) {
            SortBy.IP -> devices.sortedBy { it.ip }
            SortBy.NAME -> devices.sortedBy { it.hostname.ifEmpty { it.mac } }
            SortBy.CONNECTED_TIME -> devices.sortedByDescending { it.connectedTime }
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
