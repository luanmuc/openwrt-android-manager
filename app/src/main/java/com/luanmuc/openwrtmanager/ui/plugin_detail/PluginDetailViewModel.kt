package com.luanmuc.openwrtmanager.ui.plugin_detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luanmuc.openwrtmanager.data.model.PluginDetail
import com.luanmuc.openwrtmanager.data.model.PluginReview
import com.luanmuc.openwrtmanager.data.repository.PluginMarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 插件详情ViewModel
 */
class PluginDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val pluginMarketRepository = PluginMarketRepository.getInstance(application)
    
    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 插件详情
    private val _pluginDetail = MutableStateFlow<PluginDetail?>(null)
    val pluginDetail: StateFlow<PluginDetail?> = _pluginDetail.asStateFlow()
    
    // 插件评论
    private val _reviews = MutableStateFlow<List<PluginReview>>(emptyList())
    val reviews: StateFlow<List<PluginReview>> = _reviews.asStateFlow()
    
    // 是否正在安装
    private val _isInstalling = MutableStateFlow(false)
    val isInstalling: StateFlow<Boolean> = _isInstalling.asStateFlow()
    
    // 操作结果
    private val _operationResult = MutableStateFlow<String?>(null)
    val operationResult: StateFlow<String?> = _operationResult.asStateFlow()
    
    /**
     * 加载插件详情
     */
    fun loadPluginDetail(packageName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载插件详情
                val detail = pluginMarketRepository.getPluginDetail(packageName)
                _pluginDetail.value = detail
                
                // 加载评论
                val reviews = pluginMarketRepository.getPluginReviews(packageName)
                _reviews.value = reviews
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 安装插件
     */
    fun installPlugin() {
        viewModelScope.launch {
            _isInstalling.value = true
            try {
                // 模拟安装
                kotlinx.coroutines.delay(2000)
                _operationResult.value = "安装成功"
                // 重新加载详情
                _pluginDetail.value = _pluginDetail.value?.copy(isInstalled = true)
            } catch (e: Exception) {
                _operationResult.value = "安装失败: ${e.message}"
            } finally {
                _isInstalling.value = false
            }
        }
    }
    
    /**
     * 卸载插件
     */
    fun uninstallPlugin() {
        viewModelScope.launch {
            _isInstalling.value = true
            try {
                // 模拟卸载
                kotlinx.coroutines.delay(1500)
                _operationResult.value = "卸载成功"
                // 重新加载详情
                _pluginDetail.value = _pluginDetail.value?.copy(isInstalled = false)
            } catch (e: Exception) {
                _operationResult.value = "卸载失败: ${e.message}"
            } finally {
                _isInstalling.value = false
            }
        }
    }
    
    /**
     * 更新插件
     */
    fun updatePlugin() {
        viewModelScope.launch {
            _isInstalling.value = true
            try {
                // 模拟更新
                kotlinx.coroutines.delay(2500)
                _operationResult.value = "更新成功"
                // 重新加载详情
                _pluginDetail.value = _pluginDetail.value?.copy(
                    isUpdateAvailable = false,
                    version = _pluginDetail.value?.latestVersion ?: ""
                )
            } catch (e: Exception) {
                _operationResult.value = "更新失败: ${e.message}"
            } finally {
                _isInstalling.value = false
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
     * 格式化文件大小
     */
    fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    /**
     * 格式化时间
     */
    fun formatTime(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(date)
    }
}
