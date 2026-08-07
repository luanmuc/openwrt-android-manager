package com.luanmuc.openwrtmanager.util

/**
 * 网络诊断工具
 * 智能检测网络问题并给出修复建议
 */
object NetworkDiagnostic {

    /**
     * 诊断结果
     */
    data class DiagnosticResult(
        val isHealthy: Boolean,
        val issues: List<DiagnosticIssue>,
        val suggestions: List<String>
    )

    /**
     * 诊断问题
     */
    data class DiagnosticIssue(
        val type: IssueType,
        val severity: Severity,
        val description: String,
        val suggestion: String
    )

    /**
     * 问题类型
     */
    enum class IssueType {
        CONNECTION,
        DNS,
        LATENCY,
        PACKET_LOSS,
        WIFI_SIGNAL,
        CPU_USAGE,
        MEMORY_USAGE,
        STORAGE_USAGE,
        UPTIME
    }

    /**
     * 严重程度
     */
    enum class Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * 运行完整诊断
     */
    suspend fun runFullDiagnostic(
        routerStatus: com.luanmuc.openwrtmanager.data.model.RouterStatus?
    ): DiagnosticResult {
        val issues = mutableListOf<DiagnosticIssue>()
        val suggestions = mutableListOf<String>()

        if (routerStatus == null) {
            issues.add(
                DiagnosticIssue(
                    type = IssueType.CONNECTION,
                    severity = Severity.CRITICAL,
                    description = "无法连接到路由器",
                    suggestion = "请检查路由器地址、用户名和密码是否正确"
                )
            )
            return DiagnosticResult(false, issues, suggestions)
        }

        // 检查CPU使用率
        if (routerStatus.cpuUsage > 90f) {
            issues.add(
                DiagnosticIssue(
                    type = IssueType.CPU_USAGE,
                    severity = Severity.WARNING,
                    description = "CPU使用率过高: ${String.format("%.1f%%", routerStatus.cpuUsage)}",
                    suggestion = "建议检查是否有进程占用过多CPU资源，可尝试重启路由器"
                )
            )
            suggestions.add("CPU使用率过高，可能影响网络性能")
        }

        // 检查内存使用率
        val memoryUsage = if (routerStatus.memoryTotal > 0) {
            (routerStatus.memoryUsed.toFloat() / routerStatus.memoryTotal.toFloat()) * 100
        } else 0f

        if (memoryUsage > 90f) {
            issues.add(
                DiagnosticIssue(
                    type = IssueType.MEMORY_USAGE,
                    severity = Severity.WARNING,
                    description = "内存使用率过高: ${String.format("%.1f%%", memoryUsage)}",
                    suggestion = "建议关闭不必要的服务或插件，释放内存"
                )
            )
            suggestions.add("内存使用率过高，可能导致设备运行缓慢")
        }

        // 检查存储使用率
        val storageUsage = if (routerStatus.storageTotal > 0) {
            (routerStatus.storageUsed.toFloat() / routerStatus.storageTotal.toFloat()) * 100
        } else 0f

        if (storageUsage > 90f) {
            issues.add(
                DiagnosticIssue(
                    type = IssueType.STORAGE_USAGE,
                    severity = Severity.WARNING,
                    description = "存储空间不足: ${String.format("%.1f%%", storageUsage)}",
                    suggestion = "建议清理不必要的文件和日志，释放存储空间"
                )
            )
            suggestions.add("存储空间不足，可能影响系统正常运行")
        }

        // 检查运行时间
        if (routerStatus.uptime > 30 * 24 * 60 * 60) { // 30天
            issues.add(
                DiagnosticIssue(
                    type = IssueType.UPTIME,
                    severity = Severity.INFO,
                    description = "设备已连续运行超过30天",
                    suggestion = "建议定期重启路由器，以保持最佳性能"
                )
            )
            suggestions.add("设备运行时间较长，建议定期重启")
        }

        // 检查WAN连接
        if (!routerStatus.wanConnected) {
            issues.add(
                DiagnosticIssue(
                    type = IssueType.CONNECTION,
                    severity = Severity.ERROR,
                    description = "WAN口未连接",
                    suggestion = "请检查WAN口网线是否插好，以及宽带是否正常"
                )
            )
            suggestions.add("WAN连接异常，无法访问互联网")
        }

        val isHealthy = issues.none { it.severity == Severity.ERROR || it.severity == Severity.CRITICAL }

        if (isHealthy && issues.isEmpty()) {
            suggestions.add("网络状态良好，一切正常")
        }

        return DiagnosticResult(isHealthy, issues, suggestions)
    }

    /**
     * 快速诊断（只检查关键指标）
     */
    fun quickDiagnostic(
        cpuUsage: Float,
        memoryUsage: Float,
        wanConnected: Boolean
    ): Boolean {
        if (!wanConnected) return false
        if (cpuUsage > 95f) return false
        if (memoryUsage > 95f) return false
        return true
    }
}
