package com.luanmuc.openwrtmanager.ui.diagnostic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.model.RouterStatus
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil
import com.luanmuc.openwrtmanager.util.NetworkDiagnostic

/**
 * 智能诊断 ViewModel
 */
class DiagnosticViewModel(application: Application) : BaseViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository.getInstance(getApplication())

    private val _uiState = MutableStateFlow(DiagnosticUiState())
    val uiState: StateFlow<DiagnosticUiState> = _uiState.asStateFlow()

    data class DiagnosticUiState(
        val isRunning: Boolean = false,
        val result: NetworkDiagnostic.DiagnosticResult? = null,
        val error: String? = null,
        val hasRouter: Boolean = false,
        val currentStep: String = ""
    )

    init {
        initNetworkMonitor()
        observeRouters()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
            }
        }
    }

    fun runDiagnostic() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRunning = true,
                error = null,
                result = null,
                currentStep = "正在初始化..."
            )
            try {
                // 调试模式：使用假数据
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(500)
                    _uiState.value = _uiState.value.copy(currentStep = "正在检查CPU使用率...")
                    DebugMode.simulateDelay(500)
                    _uiState.value = _uiState.value.copy(currentStep = "正在检查内存使用率...")
                    DebugMode.simulateDelay(500)
                    _uiState.value = _uiState.value.copy(currentStep = "正在检查存储空间...")
                    DebugMode.simulateDelay(500)
                    _uiState.value = _uiState.value.copy(currentStep = "正在检查WAN连接...")
                    DebugMode.simulateDelay(500)
                    _uiState.value = _uiState.value.copy(currentStep = "正在检查运行时间...")
                    DebugMode.simulateDelay(500)

                    // 生成假的诊断结果
                    val issues = listOf(
                        NetworkDiagnostic.DiagnosticIssue(
                            type = NetworkDiagnostic.IssueType.CPU_USAGE,
                            severity = NetworkDiagnostic.Severity.WARNING,
                            description = "CPU使用率偏高",
                            suggestion = "建议关闭不必要的进程或服务"
                        ),
                        NetworkDiagnostic.DiagnosticIssue(
                            type = NetworkDiagnostic.IssueType.MEMORY_USAGE,
                            severity = NetworkDiagnostic.Severity.INFO,
                            description = "内存使用率正常",
                            suggestion = "内存使用状况良好"
                        ),
                        NetworkDiagnostic.DiagnosticIssue(
                            type = NetworkDiagnostic.IssueType.STORAGE_USAGE,
                            severity = NetworkDiagnostic.Severity.WARNING,
                            description = "存储空间使用率较高",
                            suggestion = "建议清理不必要的日志和缓存文件"
                        ),
                        NetworkDiagnostic.DiagnosticIssue(
                            type = NetworkDiagnostic.IssueType.UPTIME,
                            severity = NetworkDiagnostic.Severity.INFO,
                            description = "设备运行稳定",
                            suggestion = "设备已稳定运行较长时间"
                        )
                    )

                    val result = NetworkDiagnostic.DiagnosticResult(
                        isHealthy = true,
                        issues = issues,
                        suggestions = listOf(
                            "系统整体运行良好",
                            "建议定期清理日志文件",
                            "关注CPU使用率变化"
                        )
                    )

                    _uiState.value = _uiState.value.copy(
                        isRunning = false,
                        result = result,
                        currentStep = ""
                    )
                    return@launch
                }

                val activeRouter = getActiveRouter()
                if (activeRouter != null) {
                    val password = EncryptionUtil.decrypt(activeRouter.encryptedPassword)
                    if (!luciRepository.isLoggedIn()) {
                        luciRepository.login(activeRouter.address, activeRouter.username, password)
                    }

                    // 获取系统信息
                    _uiState.value = _uiState.value.copy(currentStep = "正在获取系统信息...")
                    val routerStatus = luciRepository.getRouterStatus()

                    // 运行诊断
                    _uiState.value = _uiState.value.copy(currentStep = "正在运行诊断...")
                    val result = NetworkDiagnostic.runFullDiagnostic(routerStatus)

                    _uiState.value = _uiState.value.copy(
                        isRunning = false,
                        result = result,
                        currentStep = ""
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRunning = false,
                    error = e.message ?: "诊断失败",
                    currentStep = ""
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
    
    override fun refreshData() {
        // 诊断页面不需要自动刷新
    }
}

