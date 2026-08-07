package com.luanmuc.openwrtmanager.ui.wifi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.model.WifiInterface
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * WiFi设置 ViewModel
 */
class WifiViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState: StateFlow<WifiUiState> = _uiState.asStateFlow()

    data class WifiUiState(
        val wifi2g: WifiConfig = WifiConfig(),
        val wifi5g: WifiConfig = WifiConfig(),
        val guestWifi: WifiConfig = WifiConfig(),
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val success: String? = null,
        val hasRouter: Boolean = false,
        val has5g: Boolean = false
    )

    data class WifiConfig(
        val enabled: Boolean = true,
        val ssid: String = "",
        val password: String = "",
        val channel: String = "auto",
        val bandwidth: String = "20",
        val txpower: String = "20",
        val encryption: String = "psk2"
    )

    init {
        observeRouters()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty()) {
                    loadWifiConfig()
                }
            }
        }
    }

    fun loadWifiConfig() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                success = null
            )
            try {
                val activeRouter = getActiveRouter()
                if (activeRouter != null) {
                    val password = EncryptionUtil.decrypt(activeRouter.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(activeRouter.address, activeRouter.username, password)
                    }

                    val wifiDevices = luciRepository.getWifiDevices()
                    val has5g = wifiDevices.size > 1

                    // 加载2.4G配置
                    val iface2g = luciRepository.getWifiDeviceInfo("radio0")
                    val wifi2g = WifiConfig(
                        enabled = iface2g.isUp,
                        ssid = iface2g.ssid,
                        channel = iface2g.channel.toString(),
                        txpower = iface2g.txpower.toString()
                    )

                    // 加载5G配置
                    val wifi5g = if (has5g) {
                        val iface5g = luciRepository.getWifiDeviceInfo("radio1")
                        WifiConfig(
                            enabled = iface5g.isUp,
                            ssid = iface5g.ssid,
                            channel = iface5g.channel.toString(),
                            txpower = iface5g.txpower.toString()
                        )
                    } else {
                        WifiConfig()
                    }

                    _uiState.value = _uiState.value.copy(
                        wifi2g = wifi2g,
                        wifi5g = wifi5g,
                        has5g = has5g,
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

    fun saveWifiConfig(band: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                success = null
            )
            try {
                val config = when (band) {
                    "2g" -> _uiState.value.wifi2g
                    "5g" -> _uiState.value.wifi5g
                    else -> _uiState.value.guestWifi
                }

                val radio = when (band) {
                    "2g" -> "radio0"
                    "5g" -> "radio1"
                    else -> "radio0"
                }

                // 保存WiFi配置
                luciRepository.setUciConfig("wireless", radio, "channel", config.channel)
                luciRepository.setUciConfig("wireless", radio, "txpower", config.txpower)

                // 保存接口配置
                val iface = if (band == "guest") "guest" else "default_radio${band.last()}"
                luciRepository.setUciConfig("wireless", "default_$radio", "ssid", config.ssid)
                luciRepository.setUciConfig("wireless", "default_$radio", "disabled", if (config.enabled) "0" else "1")
                if (config.password.isNotEmpty()) {
                    luciRepository.setUciConfig("wireless", "default_$radio", "key", config.password)
                    luciRepository.setUciConfig("wireless", "default_$radio", "encryption", config.encryption)
                }

                // 提交配置
                luciRepository.commitUci("wireless")

                // 重启WiFi
                luciRepository.restartWifi()

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    success = "WiFi配置保存成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    fun updateWifi2g(config: WifiConfig) {
        _uiState.value = _uiState.value.copy(wifi2g = config)
    }

    fun updateWifi5g(config: WifiConfig) {
        _uiState.value = _uiState.value.copy(wifi5g = config)
    }

    fun updateGuestWifi(config: WifiConfig) {
        _uiState.value = _uiState.value.copy(guestWifi = config)
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
