package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.luanmuc.openwrtmanager.data.model.CommandHistory
import com.luanmuc.openwrtmanager.data.model.CursorStyle
import com.luanmuc.openwrtmanager.data.model.QuickCommand
import com.luanmuc.openwrtmanager.data.model.TerminalConfig
import com.luanmuc.openwrtmanager.data.model.TerminalTheme
import com.luanmuc.openwrtmanager.util.DebugMode
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.delay

/**
 * 终端Repository
 * 负责终端配置、命令历史、快捷命令等功能
 */
class TerminalRepository private constructor(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("terminal_config", Context.MODE_PRIVATE)
    
    /**
     * 获取终端配置
     */
    fun getTerminalConfig(): TerminalConfig {
        return TerminalConfig(
            fontSize = prefs.getInt("fontSize", 14),
            fontFamily = prefs.getString("fontFamily", "monospace") ?: "monospace",
            theme = TerminalTheme.valueOf(prefs.getString("theme", TerminalTheme.DARK.name) ?: TerminalTheme.DARK.name),
            scrollbackSize = prefs.getInt("scrollbackSize", 1000),
            bellEnabled = prefs.getBoolean("bellEnabled", true),
            cursorStyle = CursorStyle.valueOf(prefs.getString("cursorStyle", CursorStyle.BLOCK.name) ?: CursorStyle.BLOCK.name)
        )
    }
    
    /**
     * 保存终端配置
     */
    fun saveTerminalConfig(config: TerminalConfig) {
        prefs.edit()
            .putInt("fontSize", config.fontSize)
            .putString("fontFamily", config.fontFamily)
            .putString("theme", config.theme.name)
            .putInt("scrollbackSize", config.scrollbackSize)
            .putBoolean("bellEnabled", config.bellEnabled)
            .putString("cursorStyle", config.cursorStyle.name)
            .apply()
    }
    
    /**
     * 获取命令历史记录
     */
    fun getCommandHistory(limit: Int = 50): List<CommandHistory> {
        if (DebugMode.isDebugMode) {
            return getFakeHistory()
        }
        
        val json = prefs.getString("command_history", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            val history = mutableListOf<CommandHistory>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                history.add(
                    CommandHistory(
                        id = obj.optLong("id", 0),
                        command = obj.optString("command", ""),
                        output = obj.optString("output", ""),
                        exitCode = obj.optInt("exitCode", 0),
                        timestamp = obj.optLong("timestamp", 0),
                        duration = obj.optLong("duration", 0)
                    )
                )
            }
            history.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 添加命令历史记录
     */
    fun addCommandHistory(history: CommandHistory) {
        val allHistory = getCommandHistory(100).toMutableList()
        allHistory.add(0, history)
        val limited = allHistory.take(100)
        
        val arr = JSONArray()
        for (h in limited) {
            arr.put(JSONObject().apply {
                put("id", h.id)
                put("command", h.command)
                put("output", h.output)
                put("exitCode", h.exitCode)
                put("timestamp", h.timestamp)
                put("duration", h.duration)
            })
        }
        prefs.edit().putString("command_history", arr.toString()).apply()
    }
    
    /**
     * 清空命令历史
     */
    fun clearCommandHistory() {
        prefs.edit().remove("command_history").apply()
    }
    
    /**
     * 获取快捷命令列表
     */
    fun getQuickCommands(): List<QuickCommand> {
        val json = prefs.getString("quick_commands", null) ?: return getDefaultQuickCommands()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                QuickCommand(
                    id = obj.optString("id", ""),
                    name = obj.optString("name", ""),
                    command = obj.optString("command", ""),
                    description = obj.optString("description", ""),
                    category = obj.optString("category", "常用")
                )
            }
        } catch (e: Exception) {
            getDefaultQuickCommands()
        }
    }
    
    /**
     * 获取默认快捷命令
     */
    private fun getDefaultQuickCommands(): List<QuickCommand> {
        return listOf(
            QuickCommand(
                id = "q1",
                name = "查看系统信息",
                command = "uname -a",
                description = "显示系统内核信息",
                category = "系统"
            ),
            QuickCommand(
                id = "q2",
                name = "查看内存使用",
                command = "free -h",
                description = "显示内存使用情况",
                category = "系统"
            ),
            QuickCommand(
                id = "q3",
                name = "查看磁盘空间",
                command = "df -h",
                description = "显示磁盘使用情况",
                category = "系统"
            ),
            QuickCommand(
                id = "q4",
                name = "查看网络连接",
                command = "netstat -tlnp",
                description = "显示监听端口",
                category = "网络"
            ),
            QuickCommand(
                id = "q5",
                name = "查看路由表",
                command = "route -n",
                description = "显示路由表",
                category = "网络"
            ),
            QuickCommand(
                id = "q6",
                name = "查看进程",
                command = "ps aux",
                description = "显示所有进程",
                category = "系统"
            ),
            QuickCommand(
                id = "q7",
                name = "重启路由器",
                command = "reboot",
                description = "重启路由器",
                category = "系统"
            ),
            QuickCommand(
                id = "q8",
                name = "查看CPU信息",
                command = "cat /proc/cpuinfo",
                description = "显示CPU信息",
                category = "系统"
            )
        )
    }
    
    /**
     * 保存快捷命令
     */
    fun saveQuickCommands(commands: List<QuickCommand>) {
        val arr = JSONArray()
        for (cmd in commands) {
            arr.put(JSONObject().apply {
                put("id", cmd.id)
                put("name", cmd.name)
                put("command", cmd.command)
                put("description", cmd.description)
                put("category", cmd.category)
            })
        }
        prefs.edit().putString("quick_commands", arr.toString()).apply()
    }
    
    /**
     * 执行命令（演示模式）
     */
    suspend fun executeCommand(command: String): CommandHistory {
        val startTime = System.currentTimeMillis()
        
        if (DebugMode.isDebugMode) {
            delay(500 + Math.random().toLong() * 500)
            val output = getFakeCommandOutput(command)
            val endTime = System.currentTimeMillis()
            return CommandHistory(
                id = endTime,
                command = command,
                output = output,
                exitCode = 0,
                timestamp = startTime,
                duration = endTime - startTime
            )
        }
        
        // 真实模式：通过SSH或Web终端执行
        return CommandHistory(
            id = startTime,
            command = command,
            output = "真实模式暂不支持",
            exitCode = -1,
            timestamp = startTime,
            duration = 0
        )
    }
    
    /**
     * 假数据：命令输出
     */
    private fun getFakeCommandOutput(command: String): String {
        return when {
            command.contains("uname", true) -> """
                Linux OpenWrt-Router 5.15.120 #0 SMP Mon Jan 15 10:30:00 2024 x86_64 GNU/Linux
            """.trimIndent()
            command.contains("free", true) -> """
                     total        used        free      shared  buff/cache   available
                Mem:       512000      187000      200000        5000      125000      300000
                Swap:           0           0           0
            """.trimIndent()
            command.contains("df", true) -> """
                Filesystem                Size      Used Available Use% Mounted on
                /dev/root               128.0M     45.0M     83.0M  35% /
                tmpfs                   256.0M     10.0M    246.0M   4% /tmp
                tmpfs                   512.0K         0    512.0K   0% /dev
            """.trimIndent()
            command.contains("netstat", true) -> """
                Active Internet connections (only servers)
                Proto Recv-Q Send-Q Local Address           Foreign Address         State
                tcp        0      0 0.0.0.0:22              0.0.0.0:*               LISTEN
                tcp        0      0 0.0.0.0:80              0.0.0.0:*               LISTEN
                tcp        0      0 0.0.0.0:443             0.0.0.0:*               LISTEN
                udp        0      0 0.0.0.0:53              0.0.0.0:*
            """.trimIndent()
            command.contains("ps", true) -> """
                PID USER      VSZ STAT COMMAND
                  1 root     1500 S    init
                  2 root        0 SW   [kthreadd]
                  3 root        0 SW   [ksoftirqd/0]
                123 root     2000 S    /sbin/netifd
                456 root     3000 S    /usr/sbin/uhttpd
                789 root     1000 S    /usr/sbin/dropbear
            """.trimIndent()
            else -> "命令执行完成\n$command"
        }
    }
    
    /**
     * 假数据：历史记录
     */
    private fun getFakeHistory(): List<CommandHistory> {
        val now = System.currentTimeMillis()
        return listOf(
            CommandHistory(
                id = now - 3600000,
                command = "uname -a",
                output = "Linux OpenWrt-Router 5.15.120 #0 SMP x86_64 GNU/Linux",
                exitCode = 0,
                timestamp = now - 3600000,
                duration = 100
            ),
            CommandHistory(
                id = now - 1800000,
                command = "df -h",
                output = "Filesystem Size Used Available Use% Mounted on\n/dev/root 128.0M 45.0M 83.0M 35% /",
                exitCode = 0,
                timestamp = now - 1800000,
                duration = 150
            ),
            CommandHistory(
                id = now - 600000,
                command = "free -h",
                output = "total used free shared buff/cache available\nMem: 512000 187000 200000 5000 125000 300000",
                exitCode = 0,
                timestamp = now - 600000,
                duration = 80
            )
        )
    }
    
    companion object {
        @Volatile
        private var instance: TerminalRepository? = null
        
        fun getInstance(context: Context): TerminalRepository {
            return instance ?: synchronized(this) {
                instance ?: TerminalRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
