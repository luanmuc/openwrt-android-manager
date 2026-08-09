package com.luanmuc.openwrtmanager.ui.plugins

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.luanmuc.openwrtmanager.data.model.PackageInfo
import com.luanmuc.openwrtmanager.data.model.Router
import com.luanmuc.openwrtmanager.data.repository.CacheRepository
import com.luanmuc.openwrtmanager.data.repository.LuciRepository
import com.luanmuc.openwrtmanager.data.repository.RouterRepository
import com.luanmuc.openwrtmanager.ui.base.BaseViewModel
import com.luanmuc.openwrtmanager.util.DebugMode
import com.luanmuc.openwrtmanager.util.EncryptionUtil

/**
 * 插件页 ViewModel
 * 实现缓存优先策略：先显示缓存数据，同时后台发起网络请求
 */
class PluginsViewModel(application: Application) : BaseViewModel(application) {
    private val routerRepository = RouterRepository.getInstance(application)
    private val luciRepository = LuciRepository.getInstance(getApplication())
    private val cacheRepository = CacheRepository.getInstance(application)

    private val _uiState = MutableStateFlow(PluginsUiState())
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    // 插件分类
    enum class PluginCategory(val displayName: String, val filter: String) {
        ALL("全部", ""),
        SYSTEM("系统", "system"),
        NETWORK("网络", "network"),
        SERVICE("服务", "service"),
        UTILITIES("工具", "utilities"),
        MULTIMEDIA("多媒体", "multimedia"),
        ADMINISTRATION("管理", "admin"),
        OTHER("其他", "other")
    }
    
    // 排序方式
    enum class SortType(val displayName: String) {
        NAME("按名称"),
        SIZE("按大小"),
        INSTALLED("按安装状态")
    }
    
    data class PluginsUiState(
        val installedPackages: List<PackageInfo> = emptyList(),
        val availablePackages: List<PackageInfo> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val searchQuery: String = "",
        val hasRouter: Boolean = false,
        val actionLoading: String? = null,
        val isFromCache: Boolean = false,
        val cacheTimestamp: Long? = null,
        val isOfflineMode: Boolean = false,
        val selectedCategory: PluginCategory = PluginCategory.ALL,
        val sortType: SortType = SortType.NAME,
        val selectedPackage: PackageInfo? = null,
        val showDetail: Boolean = false,
        val installProgress: Map<String, Int> = emptyMap()
    )

    init {
        initNetworkMonitor()
        observeRouters()
    }

    override fun refreshData() {
        loadPackages()
    }

    private fun observeRouters() {
        viewModelScope.launch {
            routerRepository.routers.collect { routers ->
                _uiState.value = _uiState.value.copy(hasRouter = routers.isNotEmpty())
                if (routers.isNotEmpty() && _uiState.value.installedPackages.isEmpty()) {
                    // 先加载缓存
                    loadFromCache()
                    // 然后从网络加载
                    loadPackages()
                }
            }
        }
    }

    /**
     * 从缓存加载数据
     */
    private fun loadFromCache() {
        viewModelScope.launch {
            try {
                // 调试模式不使用缓存
                if (DebugMode.isDebugMode) {
                    return@launch
                }

                val activeRouter = getActiveRouter() ?: return@launch

                val cachedInstalled = cacheRepository.getCacheEvenExpired(
                    CacheRepository.KEY_INSTALLED_PACKAGES,
                    activeRouter.id,
                    Array<PackageInfo>::class.java
                )?.toList()

                val cachedAvailable = cacheRepository.getCacheEvenExpired(
                    CacheRepository.KEY_AVAILABLE_PACKAGES,
                    activeRouter.id,
                    Array<PackageInfo>::class.java
                )?.toList()

                if (!cachedInstalled.isNullOrEmpty() || !cachedAvailable.isNullOrEmpty()) {
                    val cacheTime = cacheRepository.getCacheTimestamp(
                        CacheRepository.KEY_INSTALLED_PACKAGES,
                        activeRouter.id
                    )

                    _uiState.value = _uiState.value.copy(
                        installedPackages = cachedInstalled ?: emptyList(),
                        availablePackages = cachedAvailable ?: emptyList(),
                        isFromCache = true,
                        cacheTimestamp = cacheTime,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadPackages() {
        viewModelScope.launch {
            val isFirstLoad = _uiState.value.installedPackages.isEmpty()
            _uiState.value = _uiState.value.copy(
                isLoading = isFirstLoad,
                error = null
            )

            try {
                // 调试模式：使用假数据
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(800)
                    val installed = DebugMode.getFakeInstalledPackages()
                    val available = DebugMode.getFakeAvailablePackages()
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available,
                        isLoading = false,
                        isFromCache = false,
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

                    val installed = luciRepository.getInstalledPackages()
                    val available = luciRepository.getAvailablePackages()
                    val now = System.currentTimeMillis()

                    // 保存到缓存
                    cacheRepository.saveCache(
                        CacheRepository.KEY_INSTALLED_PACKAGES,
                        activeRouter.id,
                        "List<PackageInfo>",
                        installed.toTypedArray()
                    )
                    cacheRepository.saveCache(
                        CacheRepository.KEY_AVAILABLE_PACKAGES,
                        activeRouter.id,
                        "List<PackageInfo>",
                        available.toTypedArray()
                    )

                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available,
                        isLoading = false,
                        isFromCache = false,
                        isOfflineMode = false,
                        cacheTimestamp = now,
                        error = null
                    )
                }
            } catch (e: Exception) {
                handleLoadError(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 处理加载错误
     */
    private fun handleLoadError(errorMsg: String) {
        viewModelScope.launch {
            // 检查是否有缓存
            val activeRouter = getActiveRouter()
            val hasCache = activeRouter?.let {
                cacheRepository.hasCache(
                    CacheRepository.KEY_INSTALLED_PACKAGES,
                    it.id
                )
            } ?: false

            if (hasCache && _uiState.value.installedPackages.isNotEmpty()) {
                // 有缓存，显示离线模式
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isOfflineMode = true,
                    error = null
                )
            } else {
                // 没有缓存，显示错误
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }
        }
    }

    fun installPackage(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionLoading = name)
            try {
                // 调试模式：模拟安装
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(1500)
                    val installed = _uiState.value.installedPackages.toMutableList()
                    val available = _uiState.value.availablePackages.toMutableList()
                    val pkg = available.find { it.name == name }
                    if (pkg != null) {
                        available.remove(pkg)
                        installed.add(pkg.copy(installed = true))
                    }
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available
                    )
                    return@launch
                }

                val success = luciRepository.installPackage(name)
                if (success) {
                    // 安装成功后刷新并清除缓存
                    val activeRouter = getActiveRouter()
                    activeRouter?.let {
                        cacheRepository.deleteCache(
                            CacheRepository.KEY_INSTALLED_PACKAGES,
                            it.id
                        )
                        cacheRepository.deleteCache(
                            CacheRepository.KEY_AVAILABLE_PACKAGES,
                            it.id
                        )
                    }
                    loadPackages()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "安装失败"
                )
            } finally {
                _uiState.value = _uiState.value.copy(actionLoading = null)
            }
        }
    }

    fun removePackage(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionLoading = name)
            try {
                // 调试模式：模拟卸载
                if (DebugMode.isDebugMode) {
                    DebugMode.simulateDelay(1000)
                    val installed = _uiState.value.installedPackages.toMutableList()
                    val available = _uiState.value.availablePackages.toMutableList()
                    val pkg = installed.find { it.name == name }
                    if (pkg != null) {
                        installed.remove(pkg)
                        available.add(pkg.copy(installed = false))
                    }
                    _uiState.value = _uiState.value.copy(
                        installedPackages = installed,
                        availablePackages = available
                    )
                    return@launch
                }

                val success = luciRepository.removePackage(name)
                if (success) {
                    // 卸载成功后刷新并清除缓存
                    val activeRouter = getActiveRouter()
                    activeRouter?.let {
                        cacheRepository.deleteCache(
                            CacheRepository.KEY_INSTALLED_PACKAGES,
                            it.id
                        )
                        cacheRepository.deleteCache(
                            CacheRepository.KEY_AVAILABLE_PACKAGES,
                            it.id
                        )
                    }
                    loadPackages()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "卸载失败"
                )
            } finally {
                _uiState.value = _uiState.value.copy(actionLoading = null)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setCategory(category: PluginCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSortType(sortType: SortType) {
        _uiState.value = _uiState.value.copy(sortType = sortType)
    }

    fun showPackageDetail(pkg: PackageInfo) {
        _uiState.value = _uiState.value.copy(
            selectedPackage = pkg,
            showDetail = true
        )
    }

    fun hidePackageDetail() {
        _uiState.value = _uiState.value.copy(
            selectedPackage = null,
            showDetail = false
        )
    }

    /**
     * 获取过滤和排序后的已安装插件列表
     */
    fun getFilteredInstalled(): List<PackageInfo> {
        val state = _uiState.value
        var list = state.installedPackages

        // 搜索过滤
        if (state.searchQuery.isNotEmpty()) {
            list = list.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                it.description.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // 分类过滤
        if (state.selectedCategory != PluginCategory.ALL) {
            list = list.filter {
                it.category.contains(state.selectedCategory.filter, ignoreCase = true)
            }
        }

        // 排序
        list = when (state.sortType) {
            SortType.NAME -> list.sortedBy { it.name.lowercase() }
            SortType.SIZE -> list.sortedByDescending { it.size }
            SortType.INSTALLED -> list.sortedBy { it.name.lowercase() }
        }

        return list
    }

    /**
     * 获取过滤和排序后的可用插件列表
     */
    fun getFilteredAvailable(): List<PackageInfo> {
        val state = _uiState.value
        var list = state.availablePackages

        // 搜索过滤
        if (state.searchQuery.isNotEmpty()) {
            list = list.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                it.description.contains(state.searchQuery, ignoreCase = true)
            }
        }

        // 分类过滤
        if (state.selectedCategory != PluginCategory.ALL) {
            list = list.filter {
                it.category.contains(state.selectedCategory.filter, ignoreCase = true)
            }
        }

        // 排序
        list = when (state.sortType) {
            SortType.NAME -> list.sortedBy { it.name.lowercase() }
            SortType.SIZE -> list.sortedByDescending { it.size }
            SortType.INSTALLED -> list.sortedBy { it.name.lowercase() }
        }

        return list
    }

    /**
     * 获取所有可用分类
     */
    fun getAvailableCategories(): List<PluginCategory> {
        return PluginCategory.values().toList()
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
