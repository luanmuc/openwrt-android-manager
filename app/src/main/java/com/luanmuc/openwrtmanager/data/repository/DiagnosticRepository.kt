package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import com.luanmuc.openwrtmanager.data.model.DiagnosticCategory
import com.luanmuc.openwrtmanager.data.model.DiagnosticItem
import com.luanmuc.openwrtmanager.data.model.DiagnosticSeverity
import com.luanmuc.openwrtmanager.data.model.DiagnosticStatus
import com.luanmuc.openwrtmanager.data.model.DiagnosticSuggestion
import com.luanmuc.openwrtmanager.data.model.FullDiagnosticResult
import com.luanmuc.openwrtmanager.data.model.NetworkQuality
import com.luanmuc.openwrtmanager.data.model.NetworkQualityResult
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.delay

/**
 * 智能诊断Repository
 * 负责全面体检、网络质量检测、优化建议等功能
 */
class DiagnosticRepository private constructor(private val context: Context) {
    private val luciRepository = LuciRepository.getInstance()
    
    /**
     * 执行全面体检
     */
    suspend fun runFullDiagnostic(): FullDiagnosticResult {
        if (DebugMode.isDebugMode) {
            // 模拟检测过程
            delay(2000)
            return getFakeDiagnosticResult()
        }
        
        // 真实模式：执行各项检测
        val items = mutableListOf<DiagnosticItem>()
        val suggestions = mutableListOf<DiagnosticSuggestion>()

        try {
            // 系统检测：获取系统信息
            val systemInfo = luciRepository.getSystemInfo()
            val uptime = (systemInfo["uptime"] as? Number)?.toLong() ?: 0L
            items.add(DiagnosticItem(
                id = "system",
                name = "系统状态",
                status = if (uptime > 0) DiagnosticStatus.GOOD else DiagnosticStatus.WARNING,
                message = "系统运行正常",
                detail = "运行时间: ${uptime / 86400}天"
            ))

            // 网络检测：检查WAN连接
            val wanStatus = luciRepository.getWanStatus()
            items.add(DiagnosticItem(
                id = "network",
                name = "网络连接",
                status = if (wanStatus?.isUp == true) DiagnosticStatus.GOOD else DiagnosticStatus.ERROR,
                message = if (wanStatus?.isUp == true) "WAN连接正常" else "WAN未连接",
                detail = wanStatus?.ipaddr ?: "无IP"
            ))

            // 存储检测：执行df命令
            val dfOutput = luciRepository.executeCommand("df -h /")
            val storageOk = dfOutput?.contains("/") == true
            items.add(DiagnosticItem(
                id = "storage",
                name = "存储空间",
                status = if (storageOk) DiagnosticStatus.GOOD else DiagnosticStatus.WARNING,
                message = if (storageOk) "存储空间正常" else "无法获取存储信息",
                detail = dfOutput?.trim() ?: "未知"
            ))

            // 计算总分
            val goodCount = items.count { it.status == DiagnosticStatus.GOOD }
            val score = (goodCount * 100 / items.size.coerceAtLeast(1))

            return FullDiagnosticResult(
                overallScore = score,
                overallStatus = if (score >= 80) DiagnosticStatus.GOOD else if (score >= 60) DiagnosticStatus.WARNING else DiagnosticStatus.ERROR,
                items = items,
                suggestions = suggestions,
                completedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            return FullDiagnosticResult(
                overallScore = 0,
                overallStatus = DiagnosticStatus.ERROR,
                items = items,
                suggestions = suggestions,
                completedAt = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * 检测网络质量
     */
    suspend fun checkNetworkQuality(): NetworkQualityResult {
        if (DebugMode.isDebugMode) {
            delay(1500)
            return getFakeNetworkQualityResult()
        }
        
        // 真实模式：执行网络质量检测（ping测试）
        return try {
            val pingOutput = luciRepository.executeCommand("ping -c 3 -W 2 8.8.8.8")
            val latency = if (pingOutput?.contains("time=") == true) {
                val match = Regex("time=(\\d+)").find(pingOutput)
                match?.groupValues?.get(1)?.toFloatOrNull() ?: 50f
            } else 100f
            NetworkQualityResult(
                latency = latency.toInt(),
                jitter = (latency * 0.1).toInt(),
                packetLoss = if (pingOutput?.contains("0% packet loss") == true) 0f else 50f,
                downloadSpeed = 0,
                uploadSpeed = 0,
                overallScore = if (latency < 50) 90 else if (latency < 100) 70 else 50,
                quality = if (latency < 50) NetworkQuality.EXCELLENT else if (latency < 100) NetworkQuality.GOOD else NetworkQuality.POOR
            )
        } catch (e: Exception) {
            NetworkQualityResult(
                latency = 0, jitter = 0, packetLoss = 100f,
                downloadSpeed = 0, uploadSpeed = 0,
                overallScore = 0, quality = NetworkQuality.POOR
            )
        }
    }
    
    /**
     * 获取优化建议
     */
    suspend fun getOptimizationSuggestions(): List<DiagnosticSuggestion> {
        if (DebugMode.isDebugMode) {
            return getFakeSuggestions()
        }
        
        return emptyList()
    }
    
    /**
     * 自动修复问题
     */
    suspend fun autoFix(suggestionId: String): Boolean {
        if (DebugMode.isDebugMode) {
            delay(1000)
            return true
        }
        
        return false
    }
    
    /**
     * 假数据：全面体检结果
     */
    private fun getFakeDiagnosticResult(): FullDiagnosticResult {
        val items = listOf(
            DiagnosticItem(
                id = "system_uptime",
                name = "系统运行时间",
                category = DiagnosticCategory.SYSTEM,
                status = DiagnosticStatus.GOOD,
                message = "系统运行稳定",
                detail = "已运行 3天12小时45分",
                severity = DiagnosticSeverity.LOW
            ),
            DiagnosticItem(
                id = "system_load",
                name = "系统负载",
                category = DiagnosticCategory.SYSTEM,
                status = DiagnosticStatus.GOOD,
                message = "CPU负载正常",
                detail = "1分钟负载: 0.23, 5分钟: 0.18, 15分钟: 0.15",
                severity = DiagnosticSeverity.LOW
            ),
            DiagnosticItem(
                id = "memory_usage",
                name = "内存使用率",
                category = DiagnosticCategory.PERFORMANCE,
                status = DiagnosticStatus.WARNING,
                message = "内存使用率偏高",
                detail = "已使用 187MB / 512MB (36.5%)",
                severity = DiagnosticSeverity.MEDIUM
            ),
            DiagnosticItem(
                id = "storage_usage",
                name = "存储空间",
                category = DiagnosticCategory.STORAGE,
                status = DiagnosticStatus.GOOD,
                message = "存储空间充足",
                detail = "已使用 45MB / 128MB (35.2%)",
                severity = DiagnosticSeverity.LOW
            ),
            DiagnosticItem(
                id = "wan_connection",
                name = "WAN连接",
                category = DiagnosticCategory.NETWORK,
                status = DiagnosticStatus.GOOD,
                message = "WAN连接正常",
                detail = "IP: 192.168.1.100, 已连接 2天8小时",
                severity = DiagnosticSeverity.LOW
            ),
            DiagnosticItem(
                id = "dns_resolution",
                name = "DNS解析",
                category = DiagnosticCategory.NETWORK,
                status = DiagnosticStatus.GOOD,
                message = "DNS解析正常",
                detail = "主DNS: 114.114.114.114, 备用: 8.8.8.8",
                severity = DiagnosticSeverity.LOW
            ),
            DiagnosticItem(
                id = "wifi_signal",
                name = "WiFi信号",
                category = DiagnosticCategory.WIFI,
                status = DiagnosticStatus.WARNING,
                message = "部分区域信号较弱",
                detail = "建议调整路由器位置或添加中继",
                severity = DiagnosticSeverity.MEDIUM
            ),
            DiagnosticItem(
                id = "firewall_status",
                name = "防火墙状态",
                category = DiagnosticCategory.SECURITY,
                status = DiagnosticStatus.GOOD,
                message = "防火墙已启用",
                detail = "默认规则已配置，3条端口转发规则",
                severity = DiagnosticSeverity.LOW
            ),
            DiagnosticItem(
                id = "firmware_version",
                name = "固件版本",
                category = DiagnosticCategory.SYSTEM,
                status = DiagnosticStatus.WARNING,
                message = "有新版本可用",
                detail = "当前: 23.05.0, 最新: 23.05.2",
                severity = DiagnosticSeverity.MEDIUM
            ),
            DiagnosticItem(
                id = "online_devices",
                name = "在线设备",
                category = DiagnosticCategory.NETWORK,
                status = DiagnosticStatus.GOOD,
                message = "12台设备在线",
                detail = "有线: 3台, 无线: 9台",
                severity = DiagnosticSeverity.LOW
            )
        )
        
        val suggestions = listOf(
            DiagnosticSuggestion(
                id = "update_firmware",
                title = "升级到最新固件",
                description = "发现新固件版本 23.05.2，建议升级以获得最新功能和安全修复。",
                category = DiagnosticCategory.SYSTEM,
                priority = 1,
                canAutoFix = false,
                fixSteps = listOf(
                    "备份当前配置",
                    "下载最新固件",
                    "进入固件升级页面",
                    "上传固件并刷写"
                )
            ),
            DiagnosticSuggestion(
                id = "optimize_memory",
                title = "优化内存使用",
                description = "当前内存使用率偏高，建议关闭不必要的服务或插件。",
                category = DiagnosticCategory.PERFORMANCE,
                priority = 2,
                canAutoFix = true,
                fixSteps = listOf(
                    "检查已安装插件",
                    "禁用不常用的服务",
                    "清理系统缓存"
                )
            ),
            DiagnosticSuggestion(
                id = "wifi_position",
                title = "优化WiFi信号覆盖",
                description = "部分区域WiFi信号较弱，建议调整路由器位置。",
                category = DiagnosticCategory.WIFI,
                priority = 3,
                canAutoFix = false,
                fixSteps = listOf(
                    "将路由器放置在开阔位置",
                    "避免放在角落或柜子里",
                    "调整天线方向",
                    "考虑添加WiFi中继器"
                )
            ),
            DiagnosticSuggestion(
                id = "change_password",
                title = "定期更换管理密码",
                description = "为了安全，建议定期更换路由器管理密码。",
                category = DiagnosticCategory.SECURITY,
                priority = 4,
                canAutoFix = false,
                fixSteps = listOf(
                    "进入系统管理页面",
                    "修改管理密码",
                    "保存并重新登录"
                )
            )
        )
        
        // 计算总分
        var totalScore = 100
        for (item in items) {
            when (item.status) {
                DiagnosticStatus.WARNING -> totalScore -= 5
                DiagnosticStatus.ERROR -> totalScore -= 15
                else -> {}
            }
        }
        totalScore = totalScore.coerceIn(0, 100)
        
        val overallStatus = when {
            totalScore >= 90 -> DiagnosticStatus.GOOD
            totalScore >= 70 -> DiagnosticStatus.WARNING
            else -> DiagnosticStatus.ERROR
        }
        
        return FullDiagnosticResult(
            overallScore = totalScore,
            overallStatus = overallStatus,
            items = items,
            suggestions = suggestions,
            completedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 假数据：网络质量检测结果
     */
    private fun getFakeNetworkQualityResult(): NetworkQualityResult {
        return NetworkQualityResult(
            latency = 18,
            jitter = 3,
            packetLoss = 0.1f,
            downloadSpeed = 95 * 1024,  // 95MB/s
            uploadSpeed = 40 * 1024,    // 40MB/s
            overallScore = 92,
            quality = NetworkQuality.EXCELLENT
        )
    }
    
    /**
     * 假数据：优化建议
     */
    private fun getFakeSuggestions(): List<DiagnosticSuggestion> {
        return getFakeDiagnosticResult().suggestions
    }
    
    companion object {
        @Volatile
        private var instance: DiagnosticRepository? = null
        
        fun getInstance(context: Context): DiagnosticRepository {
            return instance ?: synchronized(this) {
                instance ?: DiagnosticRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
