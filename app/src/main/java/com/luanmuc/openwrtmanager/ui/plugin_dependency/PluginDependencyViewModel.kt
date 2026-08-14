package com.luanmuc.openwrtmanager.ui.plugin_dependency

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.luanmuc.openwrtmanager.data.model.PluginDependencies
import com.luanmuc.openwrtmanager.data.model.PluginInstallStatus
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PluginDependencyViewModel(application: Application) : BaseViewModel(application) {

    data class UiState(
        val pluginStatuses: List<PluginInstallStatus> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val installingIndex: Int = -1
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        initNetworkMonitor()
        checkDependencies()
    }

    override fun refreshData() {
        checkDependencies()
    }

    fun checkDependencies() {
        if (DebugMode.isDebugMode) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            viewModelScope.launch {
                kotlinx.coroutines.delay(500)
                val fakeStatuses = PluginDependencies.ALL.mapIndexed { index, dep ->
                    PluginInstallStatus(
                        dependency = dep,
                        isInstalled = index != 1 && index != 2
                    )
                }
                _uiState.value = _uiState.value.copy(
                    pluginStatuses = fakeStatuses,
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                val luci = LuciRepository.getInstance(getApplication())
                val statuses = luci.checkAllPluginDependencies()
                _uiState.value = _uiState.value.copy(
                    pluginStatuses = statuses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "检测失败",
                    isLoading = false
                )
            }
        }
    }

    fun installPlugin(index: Int) {
        val status = _uiState.value.pluginStatuses.getOrNull(index) ?: return
        if (status.isInstalled) return

        if (DebugMode.isDebugMode) {
            _uiState.value = _uiState.value.copy(installingIndex = index)
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                val newStatuses = _uiState.value.pluginStatuses.toMutableList()
                newStatuses[index] = status.copy(isInstalled = true)
                _uiState.value = _uiState.value.copy(
                    pluginStatuses = newStatuses,
                    installingIndex = -1
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(installingIndex = index)
                val luci = LuciRepository.getInstance(getApplication())
                val success = luci.installPluginDependency(status.dependency) { _, _ -> }
                if (success) {
                    val newStatus = luci.checkPluginDependency(status.dependency)
                    val newStatuses = _uiState.value.pluginStatuses.toMutableList()
                    newStatuses[index] = newStatus
                    _uiState.value = _uiState.value.copy(pluginStatuses = newStatuses)
                }
            } catch (e: Exception) {
                // 忽略
            } finally {
                _uiState.value = _uiState.value.copy(installingIndex = -1)
            }
        }
    }
}
