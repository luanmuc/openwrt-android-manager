package com.luanmuc.openwrtmanager.data.model

/**
 * 全面体检结果
 */
data class FullDiagnosticResult(
    val overallScore: Int = 100,
    val overallStatus: DiagnosticStatus = DiagnosticStatus.GOOD,
    val items: List<DiagnosticItem> = emptyList(),
    val suggestions: List<DiagnosticSuggestion> = emptyList(),
    val completedAt: Long = 0
)

/**
 * 诊断项
 */
data class DiagnosticItem(
    val id: String = "",
    val name: String = "",
    val category: DiagnosticCategory = DiagnosticCategory.SYSTEM,
    val status: DiagnosticStatus = DiagnosticStatus.GOOD,
    val message: String = "",
    val detail: String = "",
    val severity: DiagnosticSeverity = DiagnosticSeverity.LOW
)

/**
 * 诊断建议
 */
data class DiagnosticSuggestion(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: DiagnosticCategory = DiagnosticCategory.SYSTEM,
    val priority: Int = 0,
    val canAutoFix: Boolean = false,
    val fixSteps: List<String> = emptyList()
)

/**
 * 诊断状态
 */
enum class DiagnosticStatus(val displayName: String, val color: String) {
    GOOD("良好", "#00B578"),
    WARNING("警告", "#FF7D00"),
    ERROR("异常", "#F53F3F"),
    CHECKING("检测中", "#1677FF")
}

/**
 * 诊断分类
 */
enum class DiagnosticCategory(val displayName: String) {
    SYSTEM("系统"),
    NETWORK("网络"),
    WIFI("WiFi"),
    SECURITY("安全"),
    PERFORMANCE("性能"),
    STORAGE("存储")
}

/**
 * 诊断严重程度
 */
enum class DiagnosticSeverity(val displayName: String) {
    LOW("低"),
    MEDIUM("中"),
    HIGH("高"),
    CRITICAL("严重")
}

/**
 * 网络质量检测结果
 */
data class NetworkQualityResult(
    val latency: Int = 0,  // 延迟，ms
    val jitter: Int = 0,  // 抖动，ms
    val packetLoss: Float = 0f,  // 丢包率，%
    val downloadSpeed: Long = 0,  // 下载速度，KB/s
    val uploadSpeed: Long = 0,  // 上传速度，KB/s
    val overallScore: Int = 100,
    val quality: NetworkQuality = NetworkQuality.EXCELLENT
)

/**
 * 网络质量等级
 */
enum class NetworkQuality(val displayName: String, val minScore: Int) {
    EXCELLENT("优秀", 90),
    GOOD("良好", 70),
    FAIR("一般", 50),
    POOR("较差", 30),
    BAD("很差", 0)
}
