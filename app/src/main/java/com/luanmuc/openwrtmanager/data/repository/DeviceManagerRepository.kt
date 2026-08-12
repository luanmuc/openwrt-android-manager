package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.luanmuc.openwrtmanager.data.model.DeviceEventType
import com.luanmuc.openwrtmanager.data.model.DeviceGroup
import com.luanmuc.openwrtmanager.data.model.DeviceHistory
import com.luanmuc.openwrtmanager.data.model.DeviceNote
import com.luanmuc.openwrtmanager.data.model.SpeedLimitConfig
import com.luanmuc.openwrtmanager.util.DebugMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * 设备管理Repository
 * 负责设备备注、分组、限速、拉黑、历史记录等功能
 */
class DeviceManagerRepository private constructor(private val context: Context) {
    private val luciRepository = LuciRepository.getInstance()
    
    private val prefs: SharedPreferences = context.getSharedPreferences("device_manager", Context.MODE_PRIVATE)
    
    /**
     * 获取设备备注
     */
    fun getDeviceNote(mac: String): DeviceNote? {
        val json = prefs.getString("note_$mac", null) ?: return null
        return try {
            val obj = JSONObject(json)
            DeviceNote(
                mac = obj.optString("mac", mac),
                noteName = obj.optString("noteName", ""),
                group = obj.optString("group", "默认分组"),
                tags = obj.optJSONArray("tags")?.let { arr ->
                    List(arr.length()) { i -> arr.getString(i) }
                } ?: emptyList(),
                isBlocked = obj.optBoolean("isBlocked", false),
                speedLimit = obj.optInt("speedLimit", 0),
                createdAt = obj.optLong("createdAt", 0),
                updatedAt = obj.optLong("updatedAt", 0)
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 保存设备备注
     */
    fun saveDeviceNote(note: DeviceNote) {
        val obj = JSONObject().apply {
            put("mac", note.mac)
            put("noteName", note.noteName)
            put("group", note.group)
            put("tags", JSONArray(note.tags))
            put("isBlocked", note.isBlocked)
            put("speedLimit", note.speedLimit)
            put("createdAt", note.createdAt)
            put("updatedAt", System.currentTimeMillis())
        }
        prefs.edit().putString("note_${note.mac}", obj.toString()).apply()
    }
    
    /**
     * 获取所有设备分组
     */
    fun getDeviceGroups(): List<DeviceGroup> {
        val json = prefs.getString("groups", null) ?: return getDefaultGroups()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                DeviceGroup(
                    id = obj.optString("id", ""),
                    name = obj.optString("name", ""),
                    deviceCount = obj.optInt("deviceCount", 0),
                    color = obj.optString("color", "#1677FF")
                )
            }
        } catch (e: Exception) {
            getDefaultGroups()
        }
    }
    
    /**
     * 获取默认分组
     */
    private fun getDefaultGroups(): List<DeviceGroup> {
        return listOf(
            DeviceGroup(id = "default", name = "默认分组", deviceCount = 0, color = "#1677FF"),
            DeviceGroup(id = "family", name = "家人设备", deviceCount = 0, color = "#00B578"),
            DeviceGroup(id = "work", name = "工作设备", deviceCount = 0, color = "#FF7D00"),
            DeviceGroup(id = "guest", name = "访客设备", deviceCount = 0, color = "#86909C")
        )
    }
    
    /**
     * 保存设备分组
     */
    fun saveDeviceGroups(groups: List<DeviceGroup>) {
        val arr = JSONArray()
        for (group in groups) {
            arr.put(JSONObject().apply {
                put("id", group.id)
                put("name", group.name)
                put("deviceCount", group.deviceCount)
                put("color", group.color)
            })
        }
        prefs.edit().putString("groups", arr.toString()).apply()
    }
    
    /**
     * 获取设备历史记录
     */
    fun getDeviceHistory(mac: String? = null, limit: Int = 50): List<DeviceHistory> {
        if (DebugMode.isDebugMode) {
            return getFakeDeviceHistory()
        }
        
        val allHistory = mutableListOf<DeviceHistory>()
        // 从prefs中加载历史记录
        val json = prefs.getString("history", null) ?: return emptyList()
        
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val history = DeviceHistory(
                    mac = obj.optString("mac", ""),
                    ip = obj.optString("ip", ""),
                    hostname = obj.optString("hostname", ""),
                    eventType = DeviceEventType.valueOf(obj.optString("eventType", "ONLINE")),
                    timestamp = obj.optLong("timestamp", 0)
                )
                if (mac == null || history.mac == mac) {
                    allHistory.add(history)
                }
            }
        } catch (e: Exception) {
            // 忽略
        }
        
        return allHistory.take(limit)
    }
    
    /**
     * 添加设备历史记录
     */
    fun addDeviceHistory(history: DeviceHistory) {
        val allHistory = getDeviceHistory().toMutableList()
        allHistory.add(0, history)
        // 只保留最近200条
        val limited = allHistory.take(200)
        
        val arr = JSONArray()
        for (h in limited) {
            arr.put(JSONObject().apply {
                put("mac", h.mac)
                put("ip", h.ip)
                put("hostname", h.hostname)
                put("eventType", h.eventType.name)
                put("timestamp", h.timestamp)
            })
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }
    
    /**
     * 拉黑设备
     */
    suspend fun blockDevice(mac: String): Boolean {
        if (DebugMode.isDebugMode) {
            // 演示模式：直接修改本地备注
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(isBlocked = true))
            addDeviceHistory(DeviceHistory(
                mac = mac,
                eventType = DeviceEventType.BLOCKED,
                timestamp = System.currentTimeMillis()
            ))
            return true
        }
        
        // 真实模式：调用防火墙规则拉黑设备
        return try {
            val success = luciRepository.blockDevice(mac)
            if (success) {
                val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
                saveDeviceNote(note.copy(isBlocked = true))
                addDeviceHistory(DeviceHistory(
                    mac = mac,
                    eventType = DeviceEventType.BLOCKED,
                    timestamp = System.currentTimeMillis()
                ))
            }
            success
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 解除拉黑
     */
    suspend fun unblockDevice(mac: String): Boolean {
        if (DebugMode.isDebugMode) {
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(isBlocked = false))
            addDeviceHistory(DeviceHistory(
                mac = mac,
                eventType = DeviceEventType.UNBLOCKED,
                timestamp = System.currentTimeMillis()
            ))
            return true
        }
        
        return try {
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(isBlocked = false))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 设置设备限速
     */
    suspend fun setSpeedLimit(mac: String, uploadLimit: Int, downloadLimit: Int): Boolean {
        if (DebugMode.isDebugMode) {
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(speedLimit = downloadLimit))
            addDeviceHistory(DeviceHistory(
                mac = mac,
                eventType = DeviceEventType.SPEED_LIMITED,
                timestamp = System.currentTimeMillis()
            ))
            return true
        }
        
        return try {
            // 通过tc命令实现真实限速
            val iface = "br-lan"
            val cmd = if (downloadLimit > 0) {
                "tc qdisc add dev $iface root handle 1: htb default 10 2>/dev/null; " +
                "tc class add dev $iface parent 1: classid 1:10 htb rate ${downloadLimit}kbit 2>/dev/null; " +
                "tc filter add dev $iface protocol ip parent 1: prio 1 u32 match ip src ${getDeviceIp(mac) ?: "0.0.0.0"} flowid 1:10 2>/dev/null"
            } else {
                "tc qdisc del dev $iface root 2>/dev/null"
            }
            luciRepository.executeCommand(cmd)
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(speedLimit = downloadLimit))
            addDeviceHistory(DeviceHistory(
                mac = mac,
                eventType = DeviceEventType.SPEED_LIMITED,
                timestamp = System.currentTimeMillis()
            ))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 取消限速
     */
    suspend fun removeSpeedLimit(mac: String): Boolean {
        if (DebugMode.isDebugMode) {
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(speedLimit = 0))
            addDeviceHistory(DeviceHistory(
                mac = mac,
                eventType = DeviceEventType.SPEED_UNLIMITED,
                timestamp = System.currentTimeMillis()
            ))
            return true
        }
        
        return try {
            // 通过tc命令取消限速
            luciRepository.executeCommand("tc qdisc del dev br-lan root 2>/dev/null")
            val note = getDeviceNote(mac) ?: DeviceNote(mac = mac)
            saveDeviceNote(note.copy(speedLimit = 0))
            addDeviceHistory(DeviceHistory(
                mac = mac,
                eventType = DeviceEventType.SPEED_UNLIMITED,
                timestamp = System.currentTimeMillis()
            ))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 假数据：设备历史记录
     */
    private fun getFakeDeviceHistory(): List<DeviceHistory> {
        val now = System.currentTimeMillis()
        return listOf(
            DeviceHistory(
                mac = "AA:BB:CC:DD:EE:01",
                ip = "192.168.1.100",
                hostname = "iPhone-15-Pro",
                eventType = DeviceEventType.ONLINE,
                timestamp = now - 10 * 60 * 1000
            ),
            DeviceHistory(
                mac = "AA:BB:CC:DD:EE:02",
                ip = "192.168.1.101",
                hostname = "MacBook-Pro",
                eventType = DeviceEventType.ONLINE,
                timestamp = now - 30 * 60 * 1000
            ),
            DeviceHistory(
                mac = "AA:BB:CC:DD:EE:03",
                ip = "192.168.1.102",
                hostname = "Mi-14-Ultra",
                eventType = DeviceEventType.OFFLINE,
                timestamp = now - 1 * 60 * 60 * 1000
            ),
            DeviceHistory(
                mac = "AA:BB:CC:DD:EE:04",
                ip = "192.168.1.103",
                hostname = "Smart-TV",
                eventType = DeviceEventType.ONLINE,
                timestamp = now - 2 * 60 * 60 * 1000
            ),
            DeviceHistory(
                mac = "AA:BB:CC:DD:EE:05",
                ip = "192.168.1.104",
                hostname = "iPad-Pro",
                eventType = DeviceEventType.SPEED_LIMITED,
                timestamp = now - 3 * 60 * 60 * 1000
            )
        )
    }
    
    companion object {
        @Volatile
        private var instance: DeviceManagerRepository? = null
        
        fun getInstance(context: Context): DeviceManagerRepository {
            return instance ?: synchronized(this) {
                instance ?: DeviceManagerRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
