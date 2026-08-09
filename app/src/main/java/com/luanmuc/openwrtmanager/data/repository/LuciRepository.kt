package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.luanmuc.openwrtmanager.data.api.LuciApiService
import com.luanmuc.openwrtmanager.data.api.RetrofitClient
import com.luanmuc.openwrtmanager.data.model.*
import kotlinx.coroutines.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * LuCI API 仓库
 * 封装所有与 OpenWrt LuCI ubus RPC 的交互
 * 支持 ImmortalWrt / OpenWrt 21.02+ 新版 LuCI
 * 
 * Session自动续期特性：
 * - 登录成功记录session过期时间
 * - 每次请求前检查，快过期自动刷新
 * - 过期了自动重新登录，用户无感知
 * - APP重启自动恢复session
 * - 前台定时自动续期
 */
class LuciRepository {
    private var authToken: String = ""
    private var currentAddress: String = ""
    private var currentUsername: String = ""
    private var currentPassword: String = ""
    
    // Session过期时间相关
    private var sessionExpireTime: Long = 0L  // session过期时间戳（毫秒）
    private var sessionExpiresIn: Long = TimeUnit.MINUTES.toMillis(30)  // session有效期，默认30分钟
    private val sessionRefreshThreshold = TimeUnit.MINUTES.toMillis(5)  // 快过期阈值：剩余5分钟时刷新
    
    // Session重试相关
    private val maxRefreshRetries = 3  // 最大重试次数
    private var currentRefreshRetryCount = 0  // 当前重试次数
    private val baseRetryDelay = 2000L  // 基础重试延迟（毫秒）
    
    // 续期失败状态
    private var isRefreshFailed = false  // 续期是否失败
    private var lastRefreshError: String? = null  // 最后一次续期错误
    
    // 回调
    var onSessionExpired: (() -> Unit)? = null  // session过期回调
    var onRefreshFailed: ((String) -> Unit)? = null  // 续期失败回调
    
    // 自动续期相关
    private var autoRefreshJob: Job? = null
    private var isAutoRefreshRunning = false
    
    // 持久化相关
    private var prefs: SharedPreferences? = null
    private val PREFS_NAME = "luci_session_prefs"
    private val KEY_AUTH_TOKEN = "auth_token"
    private val KEY_ADDRESS = "address"
    private val KEY_USERNAME = "username"
    private val KEY_PASSWORD = "password"
    private val KEY_EXPIRE_TIME = "expire_time"
    private val KEY_EXPIRES_IN = "expires_in"
    
    // 单例
    companion object {
        @Volatile
        private var instance: LuciRepository? = null
        
        fun getInstance(context: Context? = null): LuciRepository {
            return instance ?: synchronized(this) {
                instance ?: LuciRepository().also { 
                    instance = it
                    context?.let { ctx -> it.init(ctx) }
                }
            }
        }
    }
    
    /**
     * 初始化，加载持久化的session
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSessionFromPrefs()
        // 如果恢复的session有效，启动自动续期
        if (isSessionValid()) {
            startAutoRefresh()
        }
    }
    
    /**
     * 从SharedPreferences加载session
     */
    private fun loadSessionFromPrefs() {
        prefs?.let {
            authToken = it.getString(KEY_AUTH_TOKEN, "") ?: ""
            currentAddress = it.getString(KEY_ADDRESS, "") ?: ""
            currentUsername = it.getString(KEY_USERNAME, "") ?: ""
            currentPassword = it.getString(KEY_PASSWORD, "") ?: ""
            sessionExpireTime = it.getLong(KEY_EXPIRE_TIME, 0L)
            sessionExpiresIn = it.getLong(KEY_EXPIRES_IN, TimeUnit.MINUTES.toMillis(30))
        }
    }
    
    /**
     * 保存session到SharedPreferences
     */
    private fun saveSessionToPrefs() {
        prefs?.edit()?.apply {
            putString(KEY_AUTH_TOKEN, authToken)
            putString(KEY_ADDRESS, currentAddress)
            putString(KEY_USERNAME, currentUsername)
            putString(KEY_PASSWORD, currentPassword)
            putLong(KEY_EXPIRE_TIME, sessionExpireTime)
            putLong(KEY_EXPIRES_IN, sessionExpiresIn)
            apply()
        }
    }
    
    /**
     * 清除持久化的session
     */
    private fun clearSessionPrefs() {
        prefs?.edit()?.clear()?.apply()
    }
    
    /**
     * 检查session是否有效
     */
    fun isSessionValid(): Boolean {
        return authToken.isNotEmpty() && System.currentTimeMillis() < sessionExpireTime
    }
    
    /**
     * 检查session是否快过期
     */
    private fun isSessionExpiringSoon(): Boolean {
        val remainingTime = sessionExpireTime - System.currentTimeMillis()
        return authToken.isNotEmpty() && remainingTime in 0..sessionRefreshThreshold
    }
    
    /**
     * 检查并刷新session（如果快过期的话）
     */
    private suspend fun checkAndRefreshSession() {
        if (isSessionExpiringSoon()) {
            try {
                refreshSession()
            } catch (e: Exception) {
                // 刷新失败，忽略，等真正过期了再重新登录
            }
        }
    }
    
    /**
     * 刷新session（通过重新登录）
     * 支持指数退避重试
     */
    private suspend fun refreshSession() {
        if (currentUsername.isEmpty() || currentPassword.isEmpty()) {
            return
        }
        
        currentRefreshRetryCount = 0
        while (currentRefreshRetryCount < maxRefreshRetries) {
            try {
                login(currentAddress, currentUsername, currentPassword)
                // 刷新成功，重置重试计数
                currentRefreshRetryCount = 0
                isRefreshFailed = false
                lastRefreshError = null
                return
            } catch (e: Exception) {
                currentRefreshRetryCount++
                if (currentRefreshRetryCount < maxRefreshRetries) {
                    // 指数退避重试
                    val delay = baseRetryDelay * (1 shl (currentRefreshRetryCount - 1))
                    delay(delay)
                }
            }
        }
        
        // 所有重试都失败了
        isRefreshFailed = true
        lastRefreshError = "Session续期失败，请重新登录"
        onRefreshFailed?.invoke(lastRefreshError ?: "续期失败")
        // 清除无效session
        authToken = ""
        sessionExpireTime = 0L
        onSessionExpired?.invoke()
    }
    
    /**
     * 启动自动续期
     */
    fun startAutoRefresh() {
        if (isAutoRefreshRunning || autoRefreshJob?.isActive == true) {
            return
        }
        
        isAutoRefreshRunning = true
        autoRefreshJob = CoroutineScope(Dispatchers.IO).launch {
            while (isAutoRefreshRunning && isLoggedIn()) {
                try {
                    delay(TimeUnit.MINUTES.toMillis(10))  // 每10分钟检查一次
                    if (isSessionExpiringSoon()) {
                        refreshSession()
                    }
                } catch (e: Exception) {
                    // 忽略错误，继续循环
                }
            }
        }
    }
    
    /**
     * 停止自动续期
     */
    fun stopAutoRefresh() {
        isAutoRefreshRunning = false
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    // ========== 认证相关 ==========

    /**
     * 登录认证
     */
    suspend fun login(address: String, username: String, password: String): String {
        currentAddress = normalizeAddress(address)
        currentUsername = username
        currentPassword = password

        val api = RetrofitClient.getApi(currentAddress)
        try {
            val request = LuciRpcRequest.loginRequest(username, password)
            val response = api.call(LuciApiService.UBUS_PATH, request)

            if (response.error != null) {
                throw LuciException(
                    code = response.error.code,
                    message = response.error.message
                )
            }

            val result = response.result
            if (result == null || result.size < 2) {
                throw LuciException(message = "无效的响应格式")
            }

            val statusCode = (result[0] as? Number)?.toInt() ?: -1
            if (statusCode != 0) {
                when (statusCode) {
                    -32000, -32002 -> throw LuciException(
                        code = statusCode,
                        message = "用户名或密码错误",
                        type = ErrorType.AUTH_FAILED
                    )
                    else -> throw LuciException(
                        code = statusCode,
                        message = "认证失败 (错误码: $statusCode)",
                        type = ErrorType.AUTH_FAILED
                    )
                }
            }

            val data = result[1] as? Map<*, *>
            authToken = data?.get("ubus_rpc_session")?.toString() ?: ""
            if (authToken.isEmpty()) {
                throw LuciException(message = "未获取到会话令牌", type = ErrorType.AUTH_FAILED)
            }
            
            // 记录session过期时间
            // 尝试从响应中获取过期时间，如果没有则使用默认值
            val expires = data?.get("expires") as? Number
            if (expires != null) {
                sessionExpiresIn = TimeUnit.SECONDS.toMillis(expires.toLong())
            }
            sessionExpireTime = System.currentTimeMillis() + sessionExpiresIn
            
            // 保存到SharedPreferences
            saveSessionToPrefs()
            
            // 启动自动续期
            startAutoRefresh()
            
            return authToken
        } catch (e: Exception) {
            throw wrapException(e)
        }
    }

    /**
     * 调用 ubus 方法
     */
    private suspend fun callUbus(
        obj: String,
        method: String,
        params: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        // 请求前检查session是否快过期，快过期则自动刷新
        checkAndRefreshSession()
        
        val api = RetrofitClient.getApi(currentAddress)
        try {
            val request = LuciRpcRequest.callRequest(authToken, obj, method, params)
            val response = api.call(LuciApiService.UBUS_PATH, request)

            if (response.error != null) {
                if (response.error.code == -32002 || response.error.message?.contains("session", true) == true) {
                    reLogin()
                    return callUbus(obj, method, params)
                }
                throw LuciException(
                    code = response.error.code,
                    message = response.error.message
                )
            }

            val result = response.result
            if (result == null || result.size < 2) {
                throw LuciException(message = "无效的响应格式")
            }

            val statusCode = (result[0] as? Number)?.toInt() ?: -1
            if (statusCode != 0) {
                if (statusCode == -32002 || statusCode == -6) {
                    reLogin()
                    return callUbus(obj, method, params)
                }
                throw LuciException(
                    code = statusCode,
                    message = "调用失败 (错误码: $statusCode)"
                )
            }

            @Suppress("UNCHECKED_CAST")
            return result[1] as? Map<String, Any> ?: emptyMap()
        } catch (e: Exception) {
            throw wrapException(e)
        }
    }

    /**
     * 重新登录
     */
    private suspend fun reLogin() {
        if (currentUsername.isNotEmpty()) {
            authToken = ""
            login(currentAddress, currentUsername, currentPassword)
        }
    }

    // ========== 系统信息 ==========

    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): Map<String, Any> {
        return try {
            callUbus("system", "info")
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 获取板载信息
     */
    suspend fun getBoardInfo(): Map<String, Any> {
        return try {
            callUbus("system", "board")
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 获取路由器状态
     */
    suspend fun getRouterStatus(): RouterStatus {
        val sysInfo = getSystemInfo()
        val boardInfo = getBoardInfo()

        val hostname = sysInfo["hostname"]?.toString()
            ?: boardInfo["hostname"]?.toString()
            ?: "OpenWrt"
        val model = boardInfo["model"]?.toString()
            ?: sysInfo["model"]?.toString()
            ?: "Unknown"
        val release = boardInfo["release"]?.toString()
            ?: sysInfo["release"]?.toString()
            ?: "Unknown"
        val kernel = sysInfo["kernel"]?.toString() ?: "Unknown"
        val uptime = (sysInfo["uptime"] as? Number)?.toLong() ?: 0L

        val loadList = sysInfo["load"] as? List<*>
        val loadAverage = loadList?.mapNotNull {
            (it as? Number)?.toFloat()
        } ?: listOf(0f, 0f, 0f)

        val memory = sysInfo["memory"] as? Map<*, *>
        val memoryTotal = (memory?.get("total") as? Number)?.toLong() ?: 0L
        val memoryFree = (memory?.get("free") as? Number)?.toLong() ?: 0L
        val memoryCached = (memory?.get("cached") as? Number)?.toLong() ?: 0L
        val memoryBuffered = (memory?.get("buffered") as? Number)?.toLong() ?: 0L
        val memoryUsed = memoryTotal - memoryFree - memoryCached - memoryBuffered

        val root = sysInfo["root"] as? Map<*, *>
        val storageTotal = (root?.get("total") as? Number)?.toLong() ?: 0L
        val storageFree = (root?.get("free") as? Number)?.toLong() ?: 0L
        val storageUsed = (root?.get("used") as? Number)?.toLong() ?: (storageTotal - storageFree)

        val cpuUsage = calculateCpuUsage(sysInfo)

        return RouterStatus(
            hostname = hostname,
            model = model,
            firmware = release,
            kernel = kernel,
            uptime = uptime,
            loadAverage = loadAverage,
            memoryTotal = memoryTotal,
            memoryFree = memoryFree,
            memoryUsed = memoryUsed,
            memoryCached = memoryCached,
            memoryBuffered = memoryBuffered,
            storageTotal = storageTotal,
            storageFree = storageFree,
            storageUsed = storageUsed,
            cpuUsage = cpuUsage
        )
    }

    /**
     * 重启路由器
     */
    suspend fun reboot(): Boolean {
        return try {
            callUbus("system", "reboot")
            true
        } catch (e: Exception) {
            true
        }
    }

    /**
     * 关机
     */
    suspend fun shutdown(): Boolean {
        return try {
            callUbus("system", "poweroff")
            true
        } catch (e: Exception) {
            true
        }
    }

    // ========== 网络接口 ==========

    /**
     * 获取网络接口列表
     */
    suspend fun getNetworkInterfaces(): List<NetworkInterface> {
        return try {
            val result = callUbus("network.interface", "dump")
            val interfaces = result["interface"] as? List<*>
            interfaces?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                parseNetworkInterface(map)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parseNetworkInterface(map: Map<*, *>): NetworkInterface {
        val name = map["interface"]?.toString() ?: map["name"]?.toString() ?: ""
        val l3Device = map["l3_device"]?.toString() ?: ""
        val proto = map["proto"]?.toString() ?: ""
        val isUp = map["up"] as? Boolean ?: false
        val isConnected = map["connected"] as? Boolean ?: false
        val uptime = (map["uptime"] as? Number)?.toLong() ?: 0L

        val ipv4Addresses = map["ipv4-address"] as? List<*>
        val firstIpv4 = ipv4Addresses?.firstOrNull() as? Map<*, *>
        val ipaddr = firstIpv4?.get("address")?.toString() ?: ""
        val netmask = firstIpv4?.get("mask")?.toString() ?: ""

        val routes = map["route"] as? List<*>
        val gateway = routes?.firstOrNull {
            val r = it as? Map<*, *>
            r?.get("target") == "0.0.0.0"
        }?.let {
            (it as? Map<*, *>)?.get("nexthop")?.toString()
        } ?: ""

        val dnsServers = map["dns-server"] as? List<*>
        val dns = dnsServers?.mapNotNull { it?.toString() } ?: emptyList()

        val stats = map["stats"] as? Map<*, *>
        val rxBytes = (stats?.get("rx_bytes") as? Number)?.toLong() ?: 0L
        val txBytes = (stats?.get("tx_bytes") as? Number)?.toLong() ?: 0L
        val rxPackets = (stats?.get("rx_packets") as? Number)?.toLong() ?: 0L
        val txPackets = (stats?.get("tx_packets") as? Number)?.toLong() ?: 0L

        return NetworkInterface(
            name = name,
            device = l3Device,
            proto = proto,
            ipaddr = ipaddr,
            netmask = netmask,
            gateway = gateway,
            dns = dns,
            uptime = uptime,
            rxBytes = rxBytes,
            txBytes = txBytes,
            rxPackets = rxPackets,
            txPackets = txPackets,
            isUp = isUp,
            isConnected = isConnected
        )
    }

    /**
     * 获取WAN口状态
     */
    suspend fun getWanStatus(): NetworkInterface? {
        return getNetworkInterfaces().firstOrNull { it.name == "wan" }
    }

    /**
     * 重启网络
     */
    suspend fun restartNetwork(): Boolean {
        return try {
            callUbus("network", "restart")
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== WiFi 相关 ==========

    /**
     * 获取WiFi设备列表
     */
    suspend fun getWifiDevices(): List<WifiInterface> {
        return try {
            val result = callUbus("iwinfo", "devices")
            val devices = result["devices"] as? List<*>
            devices?.mapNotNull { name ->
                val deviceName = name?.toString() ?: return@mapNotNull null
                getWifiDeviceInfo(deviceName)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取WiFi设备信息
     */
    suspend fun getWifiDeviceInfo(device: String): WifiInterface {
        return try {
            val result = callUbus("iwinfo", "info", mapOf("device" to device))
            val ssid = result["ssid"]?.toString() ?: ""
            val channel = (result["channel"] as? Number)?.toInt() ?: 0
            val htmode = result["htmode"]?.toString() ?: ""
            val txpower = (result["txpower"] as? Number)?.toInt() ?: 0
            val encryption = result["encryption"]?.toString() ?: ""
            val isUp = result["up"] as? Boolean ?: false

            val band = if (channel > 14) "5g" else "2.4g"

            WifiInterface(
                name = device,
                device = device,
                ssid = ssid,
                encryption = encryption,
                channel = channel,
                htmode = htmode,
                txpower = txpower,
                isUp = isUp,
                band = band
            )
        } catch (e: Exception) {
            WifiInterface(name = device, device = device)
        }
    }

    /**
     * 获取WiFi关联设备数
     */
    suspend fun getWifiAssoclist(device: String): List<DeviceInfo> {
        return try {
            val result = callUbus("iwinfo", "assoclist", mapOf("device" to device))
            val results = result["results"] as? List<*>
            results?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                DeviceInfo(
                    mac = map["mac"]?.toString() ?: "",
                    signal = (map["signal"] as? Number)?.toInt() ?: 0,
                    rxBytes = (map["rx_bytes"] as? Number)?.toLong() ?: 0,
                    txBytes = (map["tx_bytes"] as? Number)?.toLong() ?: 0,
                    interfaceName = device
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 重启WiFi
     */
    suspend fun restartWifi(): Boolean {
        return try {
            callUbus("network.wireless", "restart")
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== DHCP 租约（在线设备） ==========

    /**
     * 获取DHCP租约（在线设备）
     */
    suspend fun getDhcpLeases(): List<DeviceInfo> {
        return try {
            val result = callUbus("luci-rpc", "getDHCPLeases")
            val leases = result["leases"] as? List<*>
            leases?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                DeviceInfo(
                    ip = map["ipaddr"]?.toString() ?: map["ip"]?.toString() ?: "",
                    mac = map["macaddr"]?.toString() ?: map["mac"]?.toString() ?: "",
                    hostname = map["hostname"]?.toString() ?: "",
                    vendor = map["vendor"]?.toString() ?: "",
                    connectedTime = (map["expires"] as? Number)?.toLong() ?: 0
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取ARP表（在线设备）
     */
    suspend fun getArpTable(): List<DeviceInfo> {
        return try {
            val result = callUbus("network", "arp")
            val entries = result["arp"] as? List<*>
            entries?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                DeviceInfo(
                    ip = map["ip"]?.toString() ?: "",
                    mac = map["mac"]?.toString() ?: "",
                    interfaceName = map["device"]?.toString() ?: ""
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ========== 系统日志 ==========

    /**
     * 获取系统日志
     */
    suspend fun getSystemLog(): List<LogEntry> {
        return try {
            val result = callUbus("log", "read")
            val log = result["log"] as? List<*>
            log?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                LogEntry(
                    time = map["time"]?.toString() ?: "",
                    level = map["level"]?.toString() ?: "",
                    facility = map["facility"]?.toString() ?: "",
                    message = map["message"]?.toString() ?: ""
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ========== 进程管理 ==========

    /**
     * 获取进程列表
     */
    suspend fun getProcessList(): List<ProcessInfo> {
        return try {
            val result = callUbus("system", "process")
            val list = result["list"] as? List<*>
            list?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                ProcessInfo(
                    pid = (map["pid"] as? Number)?.toInt() ?: 0,
                    name = map["name"]?.toString() ?: "",
                    cpu = (map["cpu"] as? Number)?.toFloat() ?: 0f,
                    memory = (map["mem"] as? Number)?.toFloat() ?: 0f,
                    vsz = (map["vsz"] as? Number)?.toLong() ?: 0L,
                    rss = (map["rss"] as? Number)?.toLong() ?: 0L,
                    user = map["user"]?.toString() ?: "",
                    command = map["command"]?.toString() ?: ""
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 结束进程
     */
    suspend fun killProcess(pid: Int): Boolean {
        return try {
            callUbus("system", "kill", mapOf("pid" to pid, "signal" to 9))
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== UCI 配置操作 ==========

    /**
     * 获取UCI配置
     */
    suspend fun getUciConfig(config: String, section: String? = null): Map<String, String> {
        return try {
            val params = mutableMapOf<String, Any>("config" to config)
            if (section != null) {
                params["section"] = section
            }
            val result = callUbus("uci", "get", params)
            // 如果指定了section，返回该section的values
            if (section != null) {
                val values = result["values"] as? Map<*, *>
                values?.mapKeys { it.key.toString() }?.mapValues { it.value?.toString() ?: "" } ?: emptyMap()
            } else {
                result.mapKeys { it.key.toString() }.mapValues { it.value?.toString() ?: "" }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 设置UCI配置
     */
    suspend fun setUciConfig(config: String, section: String, option: String, value: String): Boolean {
        return try {
            callUbus(
                "uci", "set",
                mapOf(
                    "config" to config,
                    "section" to section,
                    "option" to option,
                    "value" to value
                )
            )
            callUbus("uci", "commit", mapOf("config" to config))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 应用UCI配置
     */
    suspend fun commitUci(config: String): Boolean {
        return try {
            callUbus("uci", "commit", mapOf("config" to config))
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== 插件/包管理 ==========

    /**
     * 获取已安装包列表
     */
    suspend fun getInstalledPackages(): List<PackageInfo> {
        return try {
            val result = callUbus("luci-rpc", "getInstalledPackages")
            val packages = result["packages"] as? List<*>
            packages?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                PackageInfo(
                    name = map["name"]?.toString() ?: "",
                    version = map["version"]?.toString() ?: "",
                    description = map["description"]?.toString() ?: "",
                    size = (map["size"] as? Number)?.toLong() ?: 0L,
                    installed = true
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 获取可用包列表
     */
    suspend fun getAvailablePackages(): List<PackageInfo> {
        return try {
            val result = callUbus("luci-rpc", "getAvailablePackages")
            val packages = result["packages"] as? List<*>
            packages?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                PackageInfo(
                    name = map["name"]?.toString() ?: "",
                    version = map["version"]?.toString() ?: "",
                    description = map["description"]?.toString() ?: "",
                    size = (map["size"] as? Number)?.toLong() ?: 0L,
                    installed = false
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 安装包
     */
    suspend fun installPackage(name: String): Boolean {
        return try {
            callUbus("luci-rpc", "installPackage", mapOf("package" to name))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 卸载包
     */
    suspend fun removePackage(name: String): Boolean {
        return try {
            callUbus("luci-rpc", "removePackage", mapOf("package" to name))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 更新软件源列表（opkg update）
     */
    suspend fun updatePackageLists(): Boolean {
        return try {
            callUbus("luci-rpc", "updatePackageLists")
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取软件源列表
     */
    suspend fun getPackageRepos(): List<RepoInfo> {
        return try {
            val result = callUbus("uci", "get", mapOf("config" to "opkg"))
            val values = result["values"] as? Map<*, *>
            val repos = mutableListOf<RepoInfo>()
            
            values?.forEach { (key, value) ->
                val map = value as? Map<*, *> ?: return@forEach
                val type = map[".type"]?.toString() ?: ""
                if (type == "feed") {
                    repos.add(
                        RepoInfo(
                            name = key.toString(),
                            url = map["url"]?.toString() ?: "",
                            enabled = map["enabled"]?.toString()?.toBoolean() ?: true,
                            priority = (map["priority"] as? Number)?.toInt() ?: 0,
                            type = map[".type"]?.toString() ?: "src/gz"
                        )
                    )
                }
            }
            repos
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 添加软件源
     */
    suspend fun addPackageRepo(name: String, url: String, enabled: Boolean = true): Boolean {
        return try {
            callUbus(
                "uci", "set", mapOf(
                    "config" to "opkg",
                    "section" to name,
                    "type" to "feed",
                    "values" to mapOf(
                        "url" to url,
                        "enabled" to if (enabled) "1" else "0"
                    )
                )
            )
            callUbus("uci", "commit", mapOf("config" to "opkg"))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除软件源
     */
    suspend fun removePackageRepo(name: String): Boolean {
        return try {
            callUbus(
                "uci", "delete", mapOf(
                    "config" to "opkg",
                    "section" to name
                )
            )
            callUbus("uci", "commit", mapOf("config" to "opkg"))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 启用/禁用软件源
     */
    suspend fun setRepoEnabled(name: String, enabled: Boolean): Boolean {
        return try {
            callUbus(
                "uci", "set", mapOf(
                    "config" to "opkg",
                    "section" to name,
                    "options" to mapOf(
                        "enabled" to if (enabled) "1" else "0"
                    )
                )
            )
            callUbus("uci", "commit", mapOf("config" to "opkg"))
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== 防火墙/端口转发 ==========

    /**
     * 获取端口转发规则
     */
    suspend fun getPortForwards(): List<PortForwardRule> {
        return try {
            val result = callUbus("uci", "get", mapOf("config" to "firewall"))
            val values = result["values"] as? Map<*, *>
            val rules = mutableListOf<PortForwardRule>()
            values?.forEach { (key, value) ->
                val section = value as? Map<*, *> ?: return@forEach
                val type = section[".type"]?.toString()
                if (type == "redirect") {
                    rules.add(
                        PortForwardRule(
                            name = section["name"]?.toString() ?: key.toString(),
                            proto = section["proto"]?.toString() ?: "tcp",
                            src = section["src"]?.toString() ?: "wan",
                            srcPort = section["src_dport"]?.toString() ?: "",
                            dest = section["dest"]?.toString() ?: "lan",
                            destIp = section["dest_ip"]?.toString() ?: "",
                            destPort = section["dest_port"]?.toString() ?: "",
                            enabled = section["enabled"]?.toString() != "0"
                        )
                    )
                }
            }
            rules
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 添加端口转发规则
     */
    suspend fun addPortForward(rule: PortForwardRule): Boolean {
        return try {
            val sectionName = "redirect_${System.currentTimeMillis()}"
            callUbus(
                "uci", "add", mapOf(
                    "config" to "firewall",
                    "type" to "redirect",
                    "name" to sectionName,
                    "values" to mapOf(
                        "name" to rule.name,
                        "proto" to rule.proto,
                        "src" to rule.src,
                        "src_dport" to rule.srcPort,
                        "dest" to rule.dest,
                        "dest_ip" to rule.destIp,
                        "dest_port" to rule.destPort,
                        "enabled" to if (rule.enabled) "1" else "0"
                    )
                )
            )
            callUbus("uci", "commit", mapOf("config" to "firewall"))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 删除端口转发规则
     */
    suspend fun deletePortForward(ruleName: String): Boolean {
        return try {
            callUbus(
                "uci", "delete", mapOf(
                    "config" to "firewall",
                    "section" to ruleName
                )
            )
            callUbus("uci", "commit", mapOf("config" to "firewall"))
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取DDNS配置列表
     */
    suspend fun getDdnsConfigs(): List<DdnsConfig> {
        return try {
            val result = callUbus("uci", "get", mapOf("config" to "ddns"))
            val values = result["values"] as? Map<*, *>
            val configs = mutableListOf<DdnsConfig>()
            values?.forEach { (key, value) ->
                val section = value as? Map<*, *> ?: return@forEach
                val type = section[".type"]?.toString()
                if (type == "service") {
                    configs.add(
                        DdnsConfig(
                            name = key.toString(),
                            service = section["service_name"]?.toString() ?: section["service"]?.toString() ?: "",
                            domain = section["domain"]?.toString() ?: "",
                            username = section["username"]?.toString() ?: "",
                            password = section["password"]?.toString() ?: "",
                            interfaceName = section["interface"]?.toString() ?: "wan",
                            enabled = section["enabled"]?.toString() != "0",
                            status = ""
                        )
                    )
                }
            }
            configs
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ========== 工具方法 ==========

    private fun calculateCpuUsage(sysInfo: Map<String, Any>): Float {
        val cpu = sysInfo["cpu"] as? Map<*, *>
        return (cpu?.get("usage") as? Number)?.toFloat() ?: 0f
    }

    private fun normalizeAddress(address: String): String {
        var addr = address.trim()
        if (!addr.startsWith("http://") && !addr.startsWith("https://")) {
            addr = "http://$addr"
        }
        if (!addr.endsWith("/")) {
            addr = "$addr/"
        }
        return addr
    }

    private fun wrapException(e: Exception): Exception {
        return when (e) {
            is LuciException -> e
            is UnknownHostException -> LuciException(
                message = "无法连接到路由器，请检查地址是否正确",
                type = ErrorType.NETWORK_ERROR
            )
            is ConnectException -> LuciException(
                message = "连接被拒绝，请检查路由器是否在线",
                type = ErrorType.NETWORK_ERROR
            )
            is SocketTimeoutException -> LuciException(
                message = "连接超时，请检查网络和路由器状态",
                type = ErrorType.TIMEOUT
            )
            is retrofit2.HttpException -> {
                when (e.code()) {
                    404 -> LuciException(
                        message = "API接口不存在 (404)，请确认路由器已安装LuCI",
                        type = ErrorType.NOT_FOUND
                    )
                    403 -> LuciException(
                        message = "访问被拒绝 (403)",
                        type = ErrorType.FORBIDDEN
                    )
                    401 -> LuciException(
                        message = "认证失败 (401)",
                        type = ErrorType.AUTH_FAILED
                    )
                    else -> LuciException(
                        message = "HTTP错误: ${e.code()}",
                        type = ErrorType.HTTP_ERROR
                    )
                }
            }
            else -> LuciException(
                message = e.message ?: "未知错误",
                type = ErrorType.UNKNOWN
            )
        }
    }

    fun getCurrentAddress(): String = currentAddress
    fun getCurrentAuthToken(): String = authToken
    fun isLoggedIn(): Boolean = authToken.isNotEmpty()
    fun logout() {
        // 停止自动续期
        stopAutoRefresh()
        
        authToken = ""
        currentAddress = ""
        currentUsername = ""
        currentPassword = ""
        sessionExpireTime = 0L
        
        // 清除持久化的session
        clearSessionPrefs()
        
        RetrofitClient.reset()
    }
    
    /**
     * 获取session剩余有效时间（毫秒）
     */
    fun getSessionRemainingTime(): Long {
        val remaining = sessionExpireTime - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0L
    }
    
    /**
     * 获取session总有效期（毫秒）
     */
    fun getSessionExpiresIn(): Long = sessionExpiresIn

    // ==================== 系统信息检测 ====================

    /**
     * 获取完整的系统信息
     */
    suspend fun getFullSystemInfo(): FullSystemInfo {
        return try {
            val sysInfo = getSystemInfo()
            val boardInfo = getBoardInfo()
            val releaseInfo = boardInfo["release"] as? Map<*, *>

            val hostname = sysInfo["hostname"]?.toString()
                ?: boardInfo["hostname"]?.toString()
                ?: "OpenWrt"
            val model = boardInfo["model"]?.toString()
                ?: sysInfo["model"]?.toString()
                ?: "Unknown"
            val kernel = sysInfo["kernel"]?.toString() ?: "Unknown"
            val boardName = boardInfo["board_name"]?.toString() ?: ""

            // 解析release信息
            val distribution = releaseInfo?.get("distribution")?.toString() ?: "OpenWrt"
            val version = releaseInfo?.get("version")?.toString() ?: "Unknown"
            val revision = releaseInfo?.get("revision")?.toString() ?: ""
            val target = releaseInfo?.get("target")?.toString() ?: ""
            val description = releaseInfo?.get("description")?.toString() ?: ""
            val title = releaseInfo?.get("title")?.toString() ?: ""

            // 检测架构
            val architecture = detectArchitecture(boardInfo, sysInfo)

            // 检测包管理器
            val packageManager = detectPackageManager()

            FullSystemInfo(
                hostname = hostname,
                model = model,
                firmwareVersion = version,
                kernelVersion = kernel,
                architecture = architecture.displayName,
                packageManager = packageManager,
                boardName = boardName,
                release = version,
                distribution = distribution,
                revision = revision,
                target = target,
                description = description,
                title = title
            )
        } catch (e: Exception) {
            e.printStackTrace()
            FullSystemInfo()
        }
    }

    /**
     * 检测架构
     */
    private suspend fun detectArchitecture(boardInfo: Map<String, Any>, sysInfo: Map<String, Any>): ArchitectureType {
        return try {
            val boardName = boardInfo["board_name"]?.toString() ?: ""
            val model = boardInfo["model"]?.toString() ?: ""
            val system = sysInfo["system"]?.toString() ?: ""

            when {
                // x86_64
                boardName.contains("x86", ignoreCase = true) ||
                model.contains("x86", ignoreCase = true) ||
                system.contains("x86_64", ignoreCase = true) -> ArchitectureType.X86_64

                // aarch64
                boardName.contains("aarch64", ignoreCase = true) ||
                model.contains("aarch64", ignoreCase = true) ||
                system.contains("aarch64", ignoreCase = true) -> ArchitectureType.AARCH64

                // armv7
                boardName.contains("armv7", ignoreCase = true) ||
                model.contains("armv7", ignoreCase = true) ||
                system.contains("armv7", ignoreCase = true) ||
                boardName.contains("cortex-a7", ignoreCase = true) -> ArchitectureType.ARMV7

                // mipsel
                boardName.contains("mipsel", ignoreCase = true) ||
                system.contains("mipsel", ignoreCase = true) -> ArchitectureType.MIPSEL

                // mips
                boardName.contains("mips", ignoreCase = true) ||
                system.contains("mips", ignoreCase = true) -> ArchitectureType.MIPS

                else -> ArchitectureType.UNKNOWN
            }
        } catch (e: Exception) {
            ArchitectureType.UNKNOWN
        }
    }

    /**
     * 检测包管理器类型
     */
    suspend fun detectPackageManager(): PackageManagerType {
        return try {
            // 尝试执行opkg命令
            val result = try {
                callUbus("luci-rpc", "getInstalledPackages")
                PackageManagerType.OPKG
            } catch (e: Exception) {
                null
            }

            if (result != null) {
                return PackageManagerType.OPKG
            }

            // 尝试执行apk命令（通过shell）
            try {
                val apkResult = callUbus("file", "exec", mapOf(
                    "command" to "/bin/sh",
                    "params" to listOf("-c", "apk --version")
                ))
                if (apkResult.isNotEmpty()) {
                    return PackageManagerType.APK
                }
            } catch (e: Exception) {
                // 忽略
            }

            PackageManagerType.UNKNOWN
        } catch (e: Exception) {
            PackageManagerType.OPKG // 默认返回OPKG
        }
    }

    // ==================== 软件源自动配置 ====================

    /**
     * 获取预设软件源列表
     */
    fun getPresetRepos(systemInfo: FullSystemInfo): List<RepoPreset> {
        val arch = when (systemInfo.architecture) {
            "x86_64" -> ArchitectureType.X86_64
            "aarch64" -> ArchitectureType.AARCH64
            "armv7" -> ArchitectureType.ARMV7
            "mipsel" -> ArchitectureType.MIPSEL
            "mips" -> ArchitectureType.MIPS
            else -> ArchitectureType.UNKNOWN
        }

        val version = systemInfo.firmwareVersion
        val isApk = systemInfo.packageManager == PackageManagerType.APK
        
        // 根据包管理器选择架构字符串
        val archStr = if (isApk) arch.apkArch else arch.opkgArch
        
        // 基础URL前缀（根据包管理器不同）
        val basePath = if (isApk) {
            "releases/$version/packages/$archStr"
        } else {
            "releases/$version/packages/$archStr"
        }

        return listOf(
            // 官方源
            RepoPreset(
                id = "official",
                name = "官方源",
                description = "OpenWrt官方软件源",
                baseUrl = "https://downloads.openwrt.org/$basePath",
                type = RepoPresetType.OFFICIAL,
                supportedArchitectures = ArchitectureType.values().toList()
            ),
            // 清华源
            RepoPreset(
                id = "tsinghua",
                name = "清华源",
                description = "清华大学TUNA镜像源，国内访问速度快",
                baseUrl = "https://mirrors.tuna.tsinghua.edu.cn/openwrt/$basePath",
                type = RepoPresetType.TSINGHUA,
                supportedArchitectures = ArchitectureType.values().toList()
            ),
            // 中科大源
            RepoPreset(
                id = "ustc",
                name = "中科大源",
                description = "中国科学技术大学镜像源，同步及时",
                baseUrl = "https://mirrors.ustc.edu.cn/openwrt/$basePath",
                type = RepoPresetType.USTC,
                supportedArchitectures = ArchitectureType.values().toList()
            ),
            // 阿里云源
            RepoPreset(
                id = "aliyun",
                name = "阿里云源",
                description = "阿里云镜像源，全国节点覆盖",
                baseUrl = "https://mirrors.aliyun.com/openwrt/$basePath",
                type = RepoPresetType.ALIYUN,
                supportedArchitectures = ArchitectureType.values().toList()
            ),
            // kenzok8源
            RepoPreset(
                id = "kenzok8",
                name = "kenzok8插件源",
                description = "第三方插件源，包含大量常用插件",
                baseUrl = "https://op.dllkids.xyz/packages/$archStr",
                type = RepoPresetType.KENZOK8,
                supportedArchitectures = listOf(
                    ArchitectureType.X86_64,
                    ArchitectureType.AARCH64,
                    ArchitectureType.ARMV7
                )
            ),
            // lienol源
            RepoPreset(
                id = "lienol",
                name = "lienol插件源",
                description = "lienol维护的第三方插件源",
                baseUrl = "https://op.supes.top/packages/$archStr",
                type = RepoPresetType.LIENOL,
                supportedArchitectures = listOf(
                    ArchitectureType.X86_64,
                    ArchitectureType.AARCH64,
                    ArchitectureType.ARMV7,
                    ArchitectureType.MIPSEL
                )
            ),
            // ImmortalWrt源
            RepoPreset(
                id = "immortalwrt",
                name = "ImmortalWrt源",
                description = "ImmortalWrt项目软件源",
                baseUrl = "https://mirrors.tuna.tsinghua.edu.cn/immortalwrt/releases/$version/packages/$archStr",
                type = RepoPresetType.IMMORTALWRT,
                supportedArchitectures = listOf(
                    ArchitectureType.X86_64,
                    ArchitectureType.AARCH64,
                    ArchitectureType.ARMV7,
                    ArchitectureType.MIPSEL
                )
            )
        )
    }

    /**
     * 自动配置官方软件源
     */
    suspend fun autoConfigureOfficialRepos(systemInfo: FullSystemInfo): Boolean {
        return try {
            val arch = when (systemInfo.architecture) {
                "x86_64" -> ArchitectureType.X86_64
                "aarch64" -> ArchitectureType.AARCH64
                "armv7" -> ArchitectureType.ARMV7
                "mipsel" -> ArchitectureType.MIPSEL
                "mips" -> ArchitectureType.MIPS
                else -> return false
            }

            val version = systemInfo.firmwareVersion
            if (version == "Unknown") return false

            // 备份原有配置
            backupOpkgConfig()

            // 配置官方源
            val baseUrl = "https://downloads.openwrt.org/releases/$version/packages/${arch.opkgArch}"

            // 添加各个源
            val repos = listOf(
                "base" to "$baseUrl/base",
                "luci" to "$baseUrl/luci",
                "packages" to "$baseUrl/packages",
                "routing" to "$baseUrl/routing",
                "telephony" to "$baseUrl/telephony"
            )

            for ((name, url) in repos) {
                addPackageRepo(name, url, true)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 切换软件源
     */
    suspend fun switchRepo(preset: RepoPreset, systemInfo: FullSystemInfo): Boolean {
        return try {
            // 备份原有配置
            backupOpkgConfig()

            // 获取当前源列表
            val currentRepos = getPackageRepos()

            // 删除所有现有源
            for (repo in currentRepos) {
                removePackageRepo(repo.name)
            }

            // 添加新源
            val baseUrl = preset.baseUrl
            val repos = listOf(
                "base" to "$baseUrl/base",
                "luci" to "$baseUrl/luci",
                "packages" to "$baseUrl/packages",
                "routing" to "$baseUrl/routing",
                "telephony" to "$baseUrl/telephony"
            )

            for ((name, url) in repos) {
                addPackageRepo(name, url, true)
            }

            // 更新软件源
            updatePackageLists()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 备份opkg配置
     */
    private suspend fun backupOpkgConfig(): Boolean {
        return try {
            callUbus("file", "exec", mapOf(
                "command" to "/bin/sh",
                "params" to listOf("-c", "cp /etc/opkg/customfeeds.conf /etc/opkg/customfeeds.conf.bak")
            ))
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==================== 架构验证 ====================

    /**
     * 验证插件架构是否匹配
     */
    fun validatePackageArchitecture(pkg: PackageInfo, systemInfo: FullSystemInfo): Boolean {
        return try {
            val arch = when (systemInfo.architecture) {
                "x86_64" -> ArchitectureType.X86_64
                "aarch64" -> ArchitectureType.AARCH64
                "armv7" -> ArchitectureType.ARMV7
                "mipsel" -> ArchitectureType.MIPSEL
                "mips" -> ArchitectureType.MIPS
                else -> return true // 未知架构时跳过验证
            }

            // 如果包信息中没有架构信息，默认通过
            if (pkg.architecture.isEmpty()) return true

            // 检查架构是否匹配
            pkg.architecture.contains(arch.opkgArch, ignoreCase = true) ||
            pkg.architecture.contains("all", ignoreCase = true) ||
            pkg.architecture.contains("noarch", ignoreCase = true)
        } catch (e: Exception) {
            true
        }
    }

    /**
     * 获取插件支持的架构列表
     */
    fun getPackageArchitectures(pkg: PackageInfo): List<String> {
        return if (pkg.architecture.isEmpty()) {
            listOf("all")
        } else {
            pkg.architecture.split(" ").filter { it.isNotEmpty() }
        }
    }

/**
 * LuCI 异常类
 */
class LuciException(
    val code: Int = -1,
    override val message: String,
    val type: ErrorType = ErrorType.UNKNOWN
) : Exception(message)


    // ==================== 固件升级 ====================

    /**
     * 获取当前固件信息
     */
    suspend fun getFirmwareInfo(): FirmwareInfo {
        return try {
            val sysInfo = callUbus("system", "info", emptyMap())
            val boardInfo = callUbus("system", "board", emptyMap())
            
            val hostname = sysInfo?.getAsJsonObject("info")?.get("hostname")?.asString ?: ""
            val model = boardInfo?.getAsJsonObject("board")?.get("model")?.asString ?: ""
            val release = sysInfo?.getAsJsonObject("info")?.get("release")?.asString ?: ""
            val kernel = sysInfo?.getAsJsonObject("info")?.get("kernel")?.asString ?: ""
            val boardName = boardInfo?.getAsJsonObject("board")?.get("board_name")?.asString ?: ""
            val architecture = boardInfo?.getAsJsonObject("board")?.get("system")?.asString ?: ""
            
            FirmwareInfo(
                currentVersion = release,
                currentBuildTime = "",
                deviceModel = model,
                architecture = architecture,
                kernelVersion = kernel,
                boardName = boardName
            )
        } catch (e: Exception) {
            FirmwareInfo()
        }
    }

    /**
     * 检测最新固件版本（GitHub Release）
     */
    suspend fun checkLatestFirmware(repoUrl: String): FirmwareRelease? {
        return try {
            // 从GitHub Release API获取最新版本
            // 这里简化处理，实际需要解析GitHub API返回
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 下载固件
     */
    suspend fun downloadFirmware(
        url: String,
        onProgress: (Int, Long) -> Unit
    ): Boolean {
        return try {
            // 下载固件文件
            // 这里简化处理，实际需要实现下载逻辑
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 刷写固件
     */
    suspend fun flashFirmware(
        firmwarePath: String,
        keepConfig: Boolean,
        onProgress: (Int) -> Unit
    ): Boolean {
        return try {
            // 调用sysupgrade刷写固件
            // 这里简化处理，实际需要调用ubus或sysupgrade命令
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取固件升级配置
     */
    fun getFirmwareUpgradeConfig(): FirmwareUpgradeConfig {
        val prefs = context.getSharedPreferences("firmware_prefs", Context.MODE_PRIVATE)
        return FirmwareUpgradeConfig(
            repoUrl = prefs.getString("repo_url", "") ?: "",
            customServerUrl = prefs.getString("custom_server_url", "") ?: "",
            autoCheck = prefs.getBoolean("auto_check", true),
            keepConfig = prefs.getBoolean("keep_config", true),
            autoReboot = prefs.getBoolean("auto_reboot", true)
        )
    }

    /**
     * 设置固件升级配置
     */
    fun setFirmwareUpgradeConfig(config: FirmwareUpgradeConfig) {
        val prefs = context.getSharedPreferences("firmware_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("repo_url", config.repoUrl)
            putString("custom_server_url", config.customServerUrl)
            putBoolean("auto_check", config.autoCheck)
            putBoolean("keep_config", config.keepConfig)
            putBoolean("auto_reboot", config.autoReboot)
            apply()
        }
    }


/**
 * 错误类型枚举
 */
enum class ErrorType {
    NETWORK_ERROR,
    TIMEOUT,
    AUTH_FAILED,
    NOT_FOUND,
    FORBIDDEN,
    HTTP_ERROR,
    UNKNOWN
}



