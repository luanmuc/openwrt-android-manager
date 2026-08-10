package com.luanmuc.openwrtmanager.data.model

/**
 * 备份记录
 */
data class BackupRecord(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdAt: Long = 0,
    val size: Long = 0,
    val routerName: String = "",
    val firmwareVersion: String = "",
    val backupType: BackupType = BackupType.FULL,
    val filePath: String = "",
    val isEncrypted: Boolean = false
)

/**
 * 备份类型
 */
enum class BackupType(val displayName: String) {
    FULL("完整备份"),
    SYSTEM("系统配置"),
    NETWORK("网络配置"),
    FIREWALL("防火墙配置"),
    WIFI("WiFi配置"),
    CUSTOM("自定义备份")
}

/**
 * 恢复进度
 */
data class RestoreProgress(
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val currentStepName: String = "",
    val percentage: Int = 0,
    val isCompleted: Boolean = false,
    val error: String? = null
)
