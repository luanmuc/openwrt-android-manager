package com.luanmuc.openwrtmanager.data.model

/**
 * 终端配置
 */
data class TerminalConfig(
    val fontSize: Int = 14,
    val fontFamily: String = "monospace",
    val theme: TerminalTheme = TerminalTheme.DARK,
    val scrollbackSize: Int = 1000,
    val bellEnabled: Boolean = true,
    val cursorStyle: CursorStyle = CursorStyle.BLOCK
)

/**
 * 终端主题
 */
enum class TerminalTheme(val displayName: String) {
    DARK("深色"),
    LIGHT("浅色"),
    SOLARIZED_DARK("Solarized 深色"),
    SOLARIZED_LIGHT("Solarized 浅色"),
    DRACULA("Dracula"),
    MONOKAI("Monokai")
}

/**
 * 光标样式
 */
enum class CursorStyle(val displayName: String) {
    BLOCK("块"),
    UNDERLINE("下划线"),
    BAR("竖线")
}

/**
 * 命令历史记录
 */
data class CommandHistory(
    val id: Long = 0,
    val command: String = "",
    val output: String = "",
    val exitCode: Int = 0,
    val timestamp: Long = 0,
    val duration: Long = 0
)

/**
 * 快捷命令
 */
data class QuickCommand(
    val id: String = "",
    val name: String = "",
    val command: String = "",
    val description: String = "",
    val category: String = "常用"
)
