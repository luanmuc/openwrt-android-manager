package com.luanmuc.openwrtmanager.ui.devicemanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luanmuc.openwrtmanager.data.model.DeviceGroup
import com.luanmuc.openwrtmanager.data.model.DeviceHistory
import com.luanmuc.openwrtmanager.data.model.DeviceNote
import com.luanmuc.openwrtmanager.data.repository.DeviceManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 设备管理增强ViewModel
 */
class DeviceManagerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val deviceManagerRepository = DeviceManagerRepository.getInstance(application)
    
    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 设备分组列表
    private val _deviceGroups = MutableStateFlow<List<DeviceGroup>>(emptyList())
    val deviceGroups: StateFlow<List<DeviceGroup>> = _deviceGroups.asStateFlow()
    
    // 设备上下线历史
    private val _deviceHistory = MutableStateFlow<List<DeviceHistory>>(emptyList())
    val deviceHistory: StateFlow<List<DeviceHistory>> = _deviceHistory.asStateFlow()
    
    // 操作结果
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()
    
    init {
        loadDeviceManagerData()
    }
    
    fun loadDeviceManagerData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // 加载设备分组
                val groups = deviceManagerRepository.getDeviceGroups()
                _deviceGroups.value = groups
                
                // 加载设备历史
                val history = deviceManagerRepository.getDeviceHistory(limit = 50)
                _deviceHistory.value = history
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 拉黑设备
     */
    fun blockDevice(mac: String) {
        viewModelScope.launch {
            try {
                val success = deviceManagerRepository.blockDevice(mac)
                _operationResult.value = if (success) "设备已拉黑" else "拉黑失败"
                loadDeviceManagerData()
            } catch (e: Exception) {
                _operationResult.value = "操作失败: ${e.message}"
            }
        }
    }
    
    /**
     * 解除拉黑
     */
    fun unblockDevice(mac: String) {
        viewModelScope.launch {
            try {
                val success = deviceManagerRepository.unblockDevice(mac)
                _operationResult.value = if (success) "已解除拉黑" else "解除失败"
                loadDeviceManagerData()
            } catch (e: Exception) {
                _operationResult.value = "操作失败: ${e.message}"
            }
        }
    }
    
    /**
     * 清除操作结果
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }
    
    /**
     * 格式化时间
     */
    fun formatTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val sdf = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
        return sdf.format(date)
    }
}
