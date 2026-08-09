package com.luanmuc.openwrtmanager.ui.system

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.luanmuc.openwrtmanager.data.model.LogEntry
import com.luanmuc.openwrtmanager.data.model.ProcessInfo
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.CacheRepository
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 系统管理 ViewModel
 * 实现缓存优先策略：先显示缓存数据，同时后台发起网络请求
 */
class SystemViewModel(application: Application) : BaseViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository.getInstance(getApplication())
    private val cacheRepository = CacheRepository.getInstance(application)

    private val _uiState = MutableStateFlow(SystemUiState())
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    data class SystemUiState(
        val logs: List<LogEntry> = emptyList(),
        val processes: List<ProcessInfo> = emptyList(),
        val isLoadingLogs: Boolean = false,
        val isLoadingProcesses: Boolean = false,
        val error: String? = null,
        val hasRouter: Boolean = false,
        val selectedTab: Int = 0,
        val logsFromCache: Boolean = false,
        val processesFromCache: Boolean = false,
        val logsCacheTimestamp: Long? = null,
        val processesCacheTimestamp: Long? = null,
        val isOfflineMode: Boolean = false,
        val cpuUsage: Float = 0f,
        val memoryUsage: Float = 0f,
        val cpuHistory: List<Float> = emptyList(),
        val memoryHistory: List<Float> = emptyList()
    )

    init {
        initNetworkMonitor()
        observeRouters()
        startRealtimeMonitoring()
    }
    
    /**
     * 启动实时监控（CPU、内存）
     */
    private fun startRealtimeMonitoring() {
        viewModelScope.launch {
            while (true) {
                if (DebugMode.isDebugMode) {
                    // 调试模式：生成假数据
                    val newCpu = (30 + Math.random() * 40).toFloat()
                    val newMemory = (40 + Math.random() * 30).toFloat()
                    
                    val newCpuHistory = (_uiState.value.cpuHistory + newCpu).takeLast(30)
                    val newMemoryHistory = (_uiState.value.memoryHistory + newMemory).takeLast(30)
                    
                    _uiState.value = _uiState.value.copy(
                        cpuUsage = newCpu,
                        memoryUsage = newMemory,
                        cpuHistory = newCpuHistory,
                        memoryHistory = newMemoryHistory
                    )
                }
                delay(2000) // 每2秒更新一次
            }
        }
    }

    override fun refreshData() {
        loadLogs()
        loadProcesses()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty()) {
                    // 先加载缓存
                    loadLogsFromCache()
                    // 然后从网络加载
                    loadLogs()
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        if (tab == 1 && _uiState.value.processes.isEmpty()) {
            // 先加载缓存
            loadProcessesFromCache()
            // 然后从网络加载
            loadProcesses()
        }
    }

    /**
     * 从缓存加载日志
     */
    private fun loadLogsFromCache() {
        viewModelScope.launch {
            try {
                // 调试模式不使用缓存
                if (DebugMode.isDebugMode) {
                    return@launch
                }

                val activeRouter = getActiveRouter() ?: return@launch

                val cachedLogs = cacheRepository.getCacheEvenExpired(
                    CacheRepository.KEY_SYSTEM_LOG,
                    activeRouter.id,
                    Array<LogEntry>::class.java
                )?.toList()

                if (!cachedLogs.isNullOrEmpty()) {
                    val cacheTime = cacheRepository.getCacheTimestamp(
                        CacheRepository.KEY_SYSTEM_LOG,
                        activeRouter.id
                    )

                    _uiState.value = _uiState.value.copy(
                        logs = cachedLogs,
                        logsFromCache = true,
                        logsCacheTimestamp = cacheTime,
                        isLoadingLogs = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 从缓存加载进程
     */
    private fun loadProcessesFromCache() {
        viewModelScope.launch {
            try {
                // 调试模式不使用缓存
                if (DebugMode.isDebugMode) {
                    return@launch
                }

                val activeRouter = getActiveRouter() ?: return@launch

                val cachedProcesses = cacheRepository.getCacheEvenExpired(
                    CacheRepository.KEY_PROCESS_LIST,
                    activeRouter.id,
                    Array<ProcessInfo>::class.java
                )?.toList()

                if (!cachedProcesses.isNullOrEmpty()) {
                    val cacheTime = cacheRepository.getCacheTimestamp(
                        CacheRepository.KEY_PROCESS_LIST,
                        activeRouter.id
                    )

                    _uiState.value = _uiState.value.copy(
                        processes = cachedProcesses.sortedByDescending { it.cpu },
                        processesFromCache = true,
                        processesCacheTimestamp = cacheTime,
                        isLoadingProcesses = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            val isFirstLoad = _uiState.value.logs.isEmpty()
            _uiState.value = _uiState.value.copy(
                isLoadingLogs = isFirstLoad,
                error = null
            )

            try {
                // 调试模式：使用假数据
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(600)
                    val logs = DebugMode.getFakeSystemLog()
                    _uiState.value = _uiState.value.copy(
                        logs = logs,
                        isLoadingLogs = false,
                        logsFromCache = false,
                        isOfflineMode = false,
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

                    val logs = luciRepository.getSystemLog()
                    val now = System.currentTimeMillis()

                    // 保存到缓存
                    cacheRepository.saveCache(
                        CacheRepository.KEY_SYSTEM_LOG,
                        activeRouter.id,
                        "List<LogEntry>",
                        logs.toTypedArray()
                    )

                    _uiState.value = _uiState.value.copy(
                        logs = logs,
                        isLoadingLogs = false,
                        logsFromCache = false,
                        isOfflineMode = false,
                        logsCacheTimestamp = now,
                        error = null
                    )
                }
            } catch (e: Exception) {
                handleLogsLoadError(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 处理日志加载错误
     */
    private fun handleLogsLoadError(errorMsg: String) {
        viewModelScope.launch {
            // 检查是否有缓存
            val activeRouter = getActiveRouter()
            val hasCache = activeRouter?.let {
                cacheRepository.hasCache(
                    CacheRepository.KEY_SYSTEM_LOG,
                    it.id
                )
            } ?: false

            if (hasCache && _uiState.value.logs.isNotEmpty()) {
                // 有缓存，显示离线模式
                _uiState.value = _uiState.value.copy(
                    isLoadingLogs = false,
                    isOfflineMode = true,
                    error = null
                )
            } else {
                // 没有缓存，显示错误
                _uiState.value = _uiState.value.copy(
                    isLoadingLogs = false,
                    error = errorMsg
                )
            }
        }
    }

    fun loadProcesses() {
        viewModelScope.launch {
            val isFirstLoad = _uiState.value.processes.isEmpty()
            _uiState.value = _uiState.value.copy(
                isLoadingProcesses = isFirstLoad,
                error = null
            )

            try {
                // 调试模式：使用假数据
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(800)
                    val processes = DebugMode.getFakeProcessList()
                    _uiState.value = _uiState.value.copy(
                        processes = processes.sortedByDescending { it.cpu },
                        isLoadingProcesses = false,
                        processesFromCache = false,
                        isOfflineMode = false,
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

                    val processes = luciRepository.getProcessList()
                    val now = System.currentTimeMillis()

                    // 保存到缓存
                    cacheRepository.saveCache(
                        CacheRepository.KEY_PROCESS_LIST,
                        activeRouter.id,
                        "List<ProcessInfo>",
                        processes.toTypedArray()
                    )

                    _uiState.value = _uiState.value.copy(
                        processes = processes.sortedByDescending { it.cpu },
                        isLoadingProcesses = false,
                        processesFromCache = false,
                        isOfflineMode = false,
                        processesCacheTimestamp = now,
                        error = null
                    )
                }
            } catch (e: Exception) {
                handleProcessesLoadError(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 处理进程加载错误
     */
    private fun handleProcessesLoadError(errorMsg: String) {
        viewModelScope.launch {
            // 检查是否有缓存
            val activeRouter = getActiveRouter()
            val hasCache = activeRouter?.let {
                cacheRepository.hasCache(
                    CacheRepository.KEY_PROCESS_LIST,
                    it.id
                )
            } ?: false

            if (hasCache && _uiState.value.processes.isNotEmpty()) {
                // 有缓存，显示离线模式
                _uiState.value = _uiState.value.copy(
                    isLoadingProcesses = false,
                    isOfflineMode = true,
                    error = null
                )
            } else {
                // 没有缓存，显示错误
                _uiState.value = _uiState.value.copy(
                    isLoadingProcesses = false,
                    error = errorMsg
                )
            }
        }
    }

    fun killProcess(pid: Int) {
        viewModelScope.launch {
            try {
                // 调试模式：模拟杀死进程
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(500)
                    val processes = _uiState.value.processes.filter { it.pid != pid }
                    _uiState.value = _uiState.value.copy(processes = processes)
                    return@launch
                }

                luciRepository.killProcess(pid)
                // 杀死进程后清除缓存并重新加载
                val activeRouter = getActiveRouter()
                activeRouter?.let {
                    cacheRepository.deleteCache(
                        CacheRepository.KEY_PROCESS_LIST,
                        it.id
                    )
                }
                loadProcesses()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "操作失败"
                )
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
