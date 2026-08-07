package com.luanmuc.openwrtmanager.ui.system

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.LogEntry
import com.luanmuc.openwrtmanager.data.model.ProcessInfo
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 系统管理 ViewModel
 */
class SystemViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(SystemUiState())
    val uiState: StateFlow<SystemUiState> = _uiState.asStateFlow()

    data class SystemUiState(
        val logs: List<LogEntry> = emptyList(),
        val processes: List<ProcessInfo> = emptyList(),
        val isLoadingLogs: Boolean = false,
        val isLoadingProcesses: Boolean = false,
        val error: String? = null,
        val hasRouter: Boolean = false,
        val selectedTab: Int = 0
    )

    init {
        observeRouters()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty()) {
                    loadLogs()
                }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        if (tab == 1 && _uiState.value.processes.isEmpty()) {
            loadProcesses()
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingLogs = true,
                error = null
            )
            try {
                val activeRouter = getActiveRouter()
                if (activeRouter != null) {
                    val password = EncryptionUtil.decrypt(activeRouter.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(activeRouter.address, activeRouter.username, password)
                    }

                    val logs = luciRepository.getSystemLog()
                    _uiState.value = _uiState.value.copy(
                        logs = logs,
                        isLoadingLogs = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingLogs = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun loadProcesses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingProcesses = true,
                error = null
            )
            try {
                val activeRouter = getActiveRouter()
                if (activeRouter != null) {
                    val password = EncryptionUtil.decrypt(activeRouter.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(activeRouter.address, activeRouter.username, password)
                    }

                    val processes = luciRepository.getProcessList()
                    _uiState.value = _uiState.value.copy(
                        processes = processes.sortedByDescending { it.cpu },
                        isLoadingProcesses = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProcesses = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    fun killProcess(pid: Int) {
        viewModelScope.launch {
            try {
                luciRepository.killProcess(pid)
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
