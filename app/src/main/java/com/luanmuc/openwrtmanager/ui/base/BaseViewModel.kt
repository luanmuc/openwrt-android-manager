package com.luanmuc.openwrtmanager.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luanmuc.openwrtmanager.util.NetworkMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 基础ViewModel，包含网络状态监听和自动刷新功能
 * 
 * 特性：
 * - 断网显示离线状态，暂停请求
 * - 网络恢复自动重连、自动刷新所有数据
 * - 指数退避重试
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    
    // 网络状态
    var isNetworkAvailable: Boolean = true
    var isOnlineMode: Boolean = true
    
    // 自动刷新相关
    private var networkMonitorJob: Job? = null
    private var retryJob: Job? = null
    private var currentRetryCount = 0
    private val maxRetries = 5
    private val baseRetryDelay = 1000L  // 初始重试延迟1秒
    
    // 是否已经初始化过网络监听
    private var networkMonitorInitialized = false
    
    /**
     * 初始化网络监听
     * 在ViewModel初始化时调用
     */
    protected fun initNetworkMonitor() {
        if (networkMonitorInitialized) return
        networkMonitorInitialized = true
        
        viewModelScope.launch {
            NetworkMonitor.isConnected.collectLatest { connected ->
                isNetworkAvailable = connected
                
                if (connected) {
                    // 网络恢复
                    onNetworkRestored()
                } else {
                    // 网络断开
                    onNetworkLost()
                }
            }
        }
    }
    
    /**
     * 网络恢复时调用
     * 子类重写此方法以实现自动刷新
     */
    protected open fun onNetworkRestored() {
        isOnlineMode = true
        // 重置重试计数
        currentRetryCount = 0
        // 取消之前的重试任务
        retryJob?.cancel()
        // 自动刷新数据
        refreshData()
    }
    
    /**
     * 网络断开时调用
     * 子类重写此方法以处理离线状态
     */
    protected open fun onNetworkLost() {
        isOnlineMode = false
        // 取消重试任务
        retryJob?.cancel()
    }
    
    /**
     * 刷新数据
     * 子类必须重写此方法
     */
    protected abstract fun refreshData()
    
    /**
     * 指数退避重试
     * 当网络请求失败时调用，会自动进行指数退避重试
     */
    protected fun scheduleRetry() {
        if (currentRetryCount >= maxRetries) {
            // 达到最大重试次数，停止重试
            return
        }
        
        retryJob?.cancel()
        retryJob = viewModelScope.launch {
            val delayTime = baseRetryDelay * (1 shl currentRetryCount)  // 指数退避
            currentRetryCount++
            
            delay(delayTime)
            
            if (isNetworkAvailable) {
                // 网络可用，重试
                refreshData()
            }
        }
    }
    
    /**
     * 重置重试计数
     * 当请求成功时调用
     */
    protected fun resetRetryCount() {
        currentRetryCount = 0
        retryJob?.cancel()
    }
    
    override fun onCleared() {
        super.onCleared()
        networkMonitorJob?.cancel()
        retryJob?.cancel()
    }
}
