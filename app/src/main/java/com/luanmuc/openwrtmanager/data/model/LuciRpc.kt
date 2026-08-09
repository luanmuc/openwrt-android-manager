package com.luanmuc.openwrtmanager.data.model

/**
 * LuCI ubus RPC 请求封装
 * 基于 OpenWrt rpcd ubus JSON-RPC 2.0 协议
 */
data class LuciRpcRequest(
    val jsonrpc: String = "2.0",
    val id: Int,
    val method: String = "call",
    val params: List<Any>
) {
    companion object {
        /**
         * 创建登录请求
         */
        fun loginRequest(username: String, password: String): LuciRpcRequest {
            return LuciRpcRequest(
                id = 1,
                params = listOf(
                    "00000000000000000000000000000000",
                    "session",
                    "login",
                    mapOf(
                        "username" to username,
                        "password" to password
                    )
                )
            )
        }

        /**
         * 创建通用调用请求
         */
        fun callRequest(
            sessionId: String,
            obj: String,
            method: String,
            params: Map<String, Any> = emptyMap()
        ): LuciRpcRequest {
            return LuciRpcRequest(
                id = (Math.random() * 10000).toInt(),
                params = listOf(sessionId, obj, method, params)
            )
        }
    }
}

/**
 * LuCI ubus RPC 响应封装
 * result 是一个数组：[statusCode, data]
 * statusCode 0 表示成功
 */
data class LuciRpcResponse<T>(
    val jsonrpc: String?,
    val id: Int,
    val result: T?,
    val error: LuciRpcError?
)

data class LuciRpcError(
    val code: Int,
    val message: String
)

/**
 * 登录响应数据
 */
data class LoginResult(
    val ubus_rpc_session: String,
    val expires: Int?,
    val timeout: Int?,
    val username: String?,
    val section: String?
)

/**
 * 系统信息
 */
data class SystemInfo(
    val hostname: String? = null,
    val model: String? = null,
    val release: String? = null,
    val kernel: String? = null,
    val uptime: Long? = null,
    val load: List<Float>? = null,
    val memory: MemoryInfo? = null,
    val root: StorageInfo? = null,
    val swap: SwapInfo? = null,
    val cpu: CpuInfo? = null
)

data class MemoryInfo(
    val total: Long? = null,
    val free: Long? = null,
    val cached: Long? = null,
    val buffered: Long? = null,
    val shared: Long? = null,
    val available: Long? = null
)

data class StorageInfo(
    val total: Long? = null,
    val free: Long? = null,
    val used: Long? = null
)

data class SwapInfo(
    val total: Long? = null,
    val free: Long? = null
)

data class CpuInfo(
    val model: String? = null,
    val cores: Int? = null,
    val frequency: String? = null
)

/**
 * 在线设备
 */
data class DeviceInfo(
    val ip: String = "",
    val mac: String = "",
    val hostname: String = "",
    val vendor: String = "",
    val interfaceName: String = "",
    val connectedTime: Long = 0,
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    val signal: Int = 0,
    val isOnline: Boolean = true,
    val isBlocked: Boolean = false,
    val note: String = ""
)

/**
 * 网络接口信息
 */
data class NetworkInterface(
    val name: String = "",
    val device: String = "",
    val proto: String = "",
    val ipaddr: String = "",
    val netmask: String = "",
    val gateway: String = "",
    val dns: List<String> = emptyList(),
    val uptime: Long = 0,
    val rxBytes: Long = 0,
    val txBytes: Long = 0,
    val rxPackets: Long = 0,
    val txPackets: Long = 0,
    val isUp: Boolean = false,
    val isConnected: Boolean = false
)

/**
 * WiFi 接口信息
 */
data class WifiInterface(
    val name: String = "",
    val device: String = "",
    val ssid: String = "",
    val encryption: String = "",
    val channel: Int = 0,
    val htmode: String = "",
    val txpower: Int = 0,
    val isUp: Boolean = false,
    val isGuest: Boolean = false,
    val band: String = "2.4g",
    val clients: Int = 0
)

/**
 * 插件信息
 */
data class PackageInfo(
    val name: String = "",
    val version: String = "",
    val description: String = "",
    val size: Long = 0,
    val installed: Boolean = false,
    val category: String = "",
    val depends: List<String> = emptyList()
)

/**
 * 软件源信息
 */
data class RepoInfo(
    val name: String = "",
    val url: String = "",
    val enabled: Boolean = true,
    val priority: Int = 0,
    val type: String = "src/gz"
)

/**
 * 推荐插件信息
 */
data class RecommendedPlugin(
    val name: String = "",
    val displayName: String = "",
    val description: String = "",
    val category: String = "",
    val icon: String = ""
)

/**
 * 端口转发规则
 */
data class PortForwardRule(
    val name: String = "",
    val proto: String = "tcp",
    val src: String = "wan",
    val srcPort: String = "",
    val dest: String = "lan",
    val destIp: String = "",
    val destPort: String = "",
    val enabled: Boolean = true
)

/**
 * DDNS 配置
 */
data class DdnsConfig(
    val name: String = "",
    val service: String = "",
    val domain: String = "",
    val username: String = "",
    val password: String = "",
    val interfaceName: String = "wan",
    val enabled: Boolean = true,
    val status: String = ""
)

/**
 * 进程信息
 */
data class ProcessInfo(
    val pid: Int = 0,
    val name: String = "",
    val cpu: Float = 0f,
    val memory: Float = 0f,
    val vsz: Long = 0,
    val rss: Long = 0,
    val user: String = "",
    val command: String = ""
)

/**
 * 系统日志条目
 */
data class LogEntry(
    val time: String = "",
    val level: String = "",
    val facility: String = "",
    val message: String = ""
)

/**
 * DHCP 静态租约
 */
data class DhcpStaticLease(
    val mac: String = "",
    val ip: String = "",
    val hostname: String = ""
)

/**
 * 定时任务
 */
data class CronJob(
    val id: Int = 0,
    val minute: String = "*",
    val hour: String = "*",
    val dayOfMonth: String = "*",
    val month: String = "*",
    val dayOfWeek: String = "*",
    val command: String = "",
    val enabled: Boolean = true
)

/**
 * 流量统计数据点
 */
data class TrafficDataPoint(
    val time: Long = 0,
    val rx: Long = 0,
    val tx: Long = 0
)

/**
 * CPU 使用率数据点
 */
data class CpuDataPoint(
    val time: Long = 0,
    val usage: Float = 0f
)


/**
 * 系统信息
 */
data class FullSystemInfo(
    val hostname: String = "",
    val model: String = "",
    val firmwareVersion: String = "",
    val kernelVersion: String = "",
    val architecture: String = "",
    val packageManager: PackageManagerType = PackageManagerType.OPKG,
    val boardName: String = "",
    val release: String = "",
    val distribution: String = "",
    val revision: String = "",
    val target: String = "",
    val description: String = "",
    val title: String = ""
)

/**
 * 包管理器类型
 */
enum class PackageManagerType(val displayName: String) {
    OPKG("OPKG"),
    APK("APK"),
    UNKNOWN("未知")
}

/**
 * 架构类型
 */
enum class ArchitectureType(val displayName: String, val opkgArch: String, val apkArch: String) {
    X86_64("x86_64", "x86_64", "x86_64"),
    ARMV7("armv7", "arm_cortex-a7_neon-vfpv4", "armv7"),
    AARCH64("aarch64", "aarch64_cortex-a53", "aarch64"),
    MIPSEL("mipsel", "mipsel_24kc", "mipsel"),
    MIPS("mips", "mips_24kc", "mips"),
    UNKNOWN("未知", "all", "all")
}

/**
 * 软件源预设
 */
data class RepoPreset(
    val id: String,
    val name: String,
    val description: String,
    val baseUrl: String,
    val type: RepoPresetType,
    val supportedArchitectures: List<ArchitectureType> = listOf()
)

/**
 * 软件源预设类型
 */
enum class RepoPresetType(val displayName: String) {
    OFFICIAL("官方源"),
    TSINGHUA("清华源"),
    USTC("中科大源"),
    ALIYUN("阿里云源"),
    KENZOK8("kenzok8源"),
    CUSTOM("自定义")
}
