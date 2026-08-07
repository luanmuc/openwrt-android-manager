package com.luanmuc.openwrtmanager.ui.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdvancedUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRebooting: Boolean = false,
    val isShuttingDown: Boolean = false,
    val actionSuccess: Boolean = false
)

class AdvancedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdvancedUiState())
    val uiState: StateFlow<AdvancedUiState> = _uiState.asStateFlow()

    private val repository = LuciRepository()

    fun reboot() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isRebooting = true, error = null)
            try {
                // 调试模式：模拟重启
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(2000)
                    _uiState.value = _uiState.value.copy(isLoading = false, isRebooting = false, actionSuccess = true)
                    return@launch
                }

                repository.reboot()
                _uiState.value = _uiState.value.copy(isLoading = false, isRebooting = false, actionSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRebooting = false, error = e.message ?: "重启失败")
            }
        }
    }

    fun shutdown() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isShuttingDown = true, error = null)
            try {
                // 调试模式：模拟关机
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(2000)
                    _uiState.value = _uiState.value.copy(isLoading = false, isShuttingDown = false, actionSuccess = true)
                    return@launch
                }

                repository.shutdown()
                _uiState.value = _uiState.value.copy(isLoading = false, isShuttingDown = false, actionSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, isShuttingDown = false, error = e.message ?: "关机失败")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(actionSuccess = false)
    }
}
