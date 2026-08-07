package org.openwrt.manager.ui.addrouter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.openwrt.manager.data.model.Router
import org.openwrt.manager.data.repository.LuciException
import org.openwrt.manager.data.repository.LuciRepository
import org.openwrt.manager.data.repository.RouterRepository
import org.openwrt.manager.util.EncryptionUtil

/**
 * 添加路由器 ViewModel
 */
class AddRouterViewModel(application: Application) : AndroidViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository()

    private val _uiState = MutableStateFlow(AddRouterUiState())
    val uiState: StateFlow<AddRouterUiState> = _uiState.asStateFlow()

    data class AddRouterUiState(
        val name: String = "",
        val address: String = "",
        val username: String = "root",
        val password: String = "",
        val isConnecting: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null,
        val errorType: ErrorType? = null
    )

    enum class ErrorType {
        NETWORK, AUTH, NOT_FOUND, TIMEOUT, UNKNOWN
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(address = address, error = null)
    }

    fun onUsernameChange(username: String) {
        _uiState.value = _uiState.value.copy(username = username, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    /**
     * 连接并保存路由器
     */
    fun connectAndSave(onSuccess: () -> Unit) {
        val state = _uiState.value

        if (state.address.isBlank()) {
            _uiState.value = state.copy(error = "请输入路由器地址")
            return
        }
        if (state.username.isBlank()) {
            _uiState.value = state.copy(error = "请输入用户名")
            return
        }

        // 自动补全地址格式
        val normalizedAddress = normalizeAddress(state.address)

        viewModelScope.launch {
            _uiState.value = state.copy(
                isConnecting = true,
                error = null,
                address = normalizedAddress
            )
            try {
                // 尝试连接认证
                luciRepository.login(
                    address = normalizedAddress,
                    username = state.username,
                    password = state.password
                )

                // 获取系统信息用于设置设备名称
                var deviceName = state.name
                if (deviceName.isBlank()) {
                    try {
                        val sysInfo = luciRepository.getSystemInfo()
                        val boardInfo = luciRepository.getBoardInfo()
                        deviceName = boardInfo["hostname"]?.toString()
                            ?: sysInfo["hostname"]?.toString()
                            ?: normalizedAddress
                    } catch (e: Exception) {
                        deviceName = normalizedAddress
                    }
                }

                // 加密密码并保存
                val encryptedPassword = EncryptionUtil.encrypt(state.password)
                val router = Router(
                    id = EncryptionUtil.generateId(),
                    name = deviceName,
                    address = normalizedAddress,
                    username = state.username,
                    encryptedPassword = encryptedPassword,
                    isConnected = true,
                    lastConnected = System.currentTimeMillis()
                )

                routerRepository.addRouter(router)
                routerRepository.setActiveRouter(router.id)

                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    isSuccess = true
                )
                onSuccess()
            } catch (e: LuciException) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    error = e.message,
                    errorType = when (e.type) {
                        org.openwrt.manager.data.repository.ErrorType.NETWORK_ERROR -> ErrorType.NETWORK
                        org.openwrt.manager.data.repository.ErrorType.TIMEOUT -> ErrorType.TIMEOUT
                        org.openwrt.manager.data.repository.ErrorType.AUTH_FAILED -> ErrorType.AUTH
                        org.openwrt.manager.data.repository.ErrorType.NOT_FOUND -> ErrorType.NOT_FOUND
                        else -> ErrorType.UNKNOWN
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    error = e.message ?: "连接失败，请检查地址和凭据",
                    errorType = ErrorType.UNKNOWN
                )
            }
        }
    }

    /**
     * 规范化地址格式
     */
    private fun normalizeAddress(address: String): String {
        var addr = address.trim()
        if (addr.isEmpty()) return addr

        // 自动添加 http:// 前缀
        if (!addr.startsWith("http://") && !addr.startsWith("https://")) {
            addr = "http://$addr"
        }

        // 确保以 / 结尾
        if (!addr.endsWith("/")) {
            addr = "$addr/"
        }

        return addr
    }
}
