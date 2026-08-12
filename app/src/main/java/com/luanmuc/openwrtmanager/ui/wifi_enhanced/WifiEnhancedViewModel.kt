package com.luanmuc.openwrtmanager.ui.wifi_enhanced

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import com.luanmuc.openwrtmanager.data.model.GuestNetworkConfig
import com.luanmuc.openwrtmanager.data.model.WifiBand
import com.luanmuc.openwrtmanager.data.model.WifiChannelInfo
import com.luanmuc.openwrtmanager.data.model.WifiSchedule
import com.luanmuc.openwrtmanager.data.repository.WifiConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * WiFi增强ViewModel
 */
class WifiEnhancedViewModel(application: Application) : BaseViewModel(application) {
    
    private val wifiConfigRepository = WifiConfigRepository.getInstance(application)
    
    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // WiFi定时配置列表
    private val _wifiSchedules = MutableStateFlow<List<WifiSchedule>>(emptyList())
    val wifiSchedules: StateFlow<List<WifiSchedule>> = _wifiSchedules.asStateFlow()
    
    // 访客网络配置
    private val _guestConfig = MutableStateFlow<GuestNetworkConfig?>(null)
    val guestConfig: StateFlow<GuestNetworkConfig?> = _guestConfig.asStateFlow()
    
    // 信道扫描结果
    private val _channelInfo = MutableStateFlow<List<WifiChannelInfo>>(emptyList())
    val channelInfo: StateFlow<List<WifiChannelInfo>> = _channelInfo.asStateFlow()
    
    // 是否正在扫描信道
    private val _isScanningChannels = MutableStateFlow(false)
    val isScanningChannels: StateFlow<Boolean> = _isScanningChannels.asStateFlow()
    
    // 当前选中的频段
    private val _selectedBand = MutableStateFlow(WifiBand.BAND_2G)
    val selectedBand: StateFlow<WifiBand> = _selectedBand.asStateFlow()
    
    // 发射功率
    private val _txPower = MutableStateFlow(20)  // 默认20dBm
    val txPower: StateFlow<Int> = _txPower.asStateFlow()
    
    // 操作结果
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()
    
    init {
        initNetworkMonitor()
        loadWifiConfig()
    }
    
    private fun loadWifiConfig() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载定时配置
                val schedules = wifiConfigRepository.getWifiSchedules()
                _wifiSchedules.value = schedules
                
                // 加载访客网络配置
                val guest = wifiConfigRepository.getGuestNetworkConfig()
                _guestConfig.value = guest
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 切换定时配置开关
     */
    fun toggleSchedule(scheduleId: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val schedules = _wifiSchedules.value.map { 
                    if (it.id == scheduleId) it.copy(enabled = enabled) else it
                }
                wifiConfigRepository.saveWifiSchedules(schedules)
                _wifiSchedules.value = schedules
                _operationResult.value = if (enabled) "定时已开启" else "定时已关闭"
            } catch (e: Exception) {
                _operationResult.value = "操作失败: ${e.message}"
            }
        }
    }
    
    /**
     * 切换访客网络
     */
    fun toggleGuestNetwork(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val success = if (enabled) {
                    wifiConfigRepository.enableGuestNetwork(_guestConfig.value ?: GuestNetworkConfig())
                } else {
                    wifiConfigRepository.disableGuestNetwork()
                }
                
                if (success) {
                    val config = wifiConfigRepository.getGuestNetworkConfig()
                    _guestConfig.value = config
                    _operationResult.value = if (enabled) "访客网络已开启" else "访客网络已关闭"
                }
            } catch (e: Exception) {
                _operationResult.value = "操作失败: ${e.message}"
            }
        }
    }
    
    /**
     * 扫描信道
     */
    fun scanChannels() {
        viewModelScope.launch {
            _isScanningChannels.value = true
            try {
                val channels = wifiConfigRepository.scanWifiChannels(_selectedBand.value)
                _channelInfo.value = channels
                _operationResult.value = "扫描完成，发现 ${channels.size} 个信道"
            } catch (e: Exception) {
                _operationResult.value = "扫描失败: ${e.message}"
            } finally {
                _isScanningChannels.value = false
            }
        }
    }
    
    /**
     * 设置选中的频段
     */
    fun setSelectedBand(band: WifiBand) {
        _selectedBand.value = band
    }
    
    /**
     * 设置发射功率
     */
    fun setTxPower(power: Int) {
        viewModelScope.launch {
            try {
                val success = wifiConfigRepository.setTxPower("wlan0", power)
                if (success) {
                    _txPower.value = power
                    _operationResult.value = "发射功率已设置为 ${power}dBm"
                }
            } catch (e: Exception) {
                _operationResult.value = "设置失败: ${e.message}"
            }
        }
    }
    
    /**
     * 清除操作结果
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    override fun refreshData() {
        loadWifiConfig()
    }
}
