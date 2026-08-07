package com.luanmuc.openwrtmanager.util

import com.luanmuc.openwrtmanager.data.model.*
import kotlinx.coroutines.delay

/**
 * 调试模式（演示模式）工具类
 * 所有调试相关代码集中在此文件，后期删除时直接删除此文件即可
 *
 * 使用方法：在"我的"页面连续点击版本号5次开启/关闭
 */
object DebugMode {
    // 调试模式开关
    var isDebugMode = false
        private set

    // 版本号点击计数（用于隐藏入口）
    var versionClickCount = 0

    /**
     * 切换调试模式
     */
    fun toggle() {
        isDebugMode = !isDebugMode
        versionClickCount = 0
    }

    /**
     * 点击版本号，连续点击5次切换调试模式
     * @return true表示触发了切换
     */
    fun onVersionClick(): Boolean {
        versionClickCount++
        if (versionClickCount >= 5) {
            toggle()
            return true
        }
        return false
    }

    // ==================== 假数据生成 ====================

    /**
     * 模拟延迟
     */
    suspend fun simulateDelay(delayMs: Long = 500) {
        delay(delayMs)
    }

    /**
     * 生成假的路由器状态
     */
    fun getFakeRouterStatus(): RouterStatus {
        return RouterStatus(
            hostname = "OpenWrt-Router",
            model = "Generic x86/64",
            firmware = "23.05.0",
            kernel = "5.15.120",
            uptime = 3 * 24 * 3600 + 12 * 3600 + 45 * 60, // 3天12小时45分
            cpuUsage = 23.5f,
            memoryTotal = 512 * 1024 * 1024L, // 512MB
            memoryUsed = 187 * 1024 * 1024L,  // 187MB
            storageTotal = 128 * 1024 * 1024L, // 128MB
            storageUsed = 45 * 1024 * 1024L,   // 45MB
            onlineDevices = 12,
            wanConnected = true,
            wanIp = "192.168.1.100",
            wanUptime = 2 * 24 * 3600 + 8 * 3600 // 2天8小时
        )
    }

    /**
     * 生成假的WAN状态
     */
    fun getFakeWanStatus(): NetworkInterface {
        return NetworkInterface(
            name = "wan",
            device = "eth0",
            proto = "dhcp",
            ipaddr = "192.168.1.100",
            netmask = "255.255.255.0",
            gateway = "192.168.1.1",
            dns = listOf("114.114.114.114", "8.8.8.8"),
            uptime = 2 * 24 * 3600 + 8 * 3600,
            rxBytes = 1567890123L, // ~1.5GB
            txBytes = 987654321L,  // ~987MB
            rxPackets = 1234567,
            txPackets = 987654,
            isUp = true,
            isConnected = true
        )
    }

    /**
     * 生成假的在线设备列表
     */
    fun getFakeOnlineDevices(): List<DeviceInfo> {
        return listOf(
            DeviceInfo(
                ip = "192.168.1.10",
                mac = "AA:BB:CC:DD:EE:01",
                hostname = "iPhone-15-Pro",
                vendor = "Apple Inc.",
                interfaceName = "wlan0",
                connectedTime = 86400,
                rxBytes = 567890123L,
                txBytes = 234567890L,
                signal = -45,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.11",
                mac = "AA:BB:CC:DD:EE:02",
                hostname = "MacBook-Pro",
                vendor = "Apple Inc.",
                interfaceName = "wlan0",
                connectedTime = 72000,
                rxBytes = 1234567890L,
                txBytes = 987654321L,
                signal = -52,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.12",
                mac = "AA:BB:CC:DD:EE:03",
                hostname = "Mi-14-Ultra",
                vendor = "Xiaomi Communications Co., Ltd.",
                interfaceName = "wlan0",
                connectedTime = 54000,
                rxBytes = 345678901L,
                txBytes = 123456789L,
                signal = -58,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.13",
                mac = "AA:BB:CC:DD:EE:04",
                hostname = "iPad-Pro",
                vendor = "Apple Inc.",
                interfaceName = "wlan1",
                connectedTime = 36000,
                rxBytes = 789012345L,
                txBytes = 456789012L,
                signal = -62,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.14",
                mac = "AA:BB:CC:DD:EE:05",
                hostname = "Desktop-PC",
                vendor = "Intel Corporate",
                interfaceName = "eth1",
                connectedTime = 259200,
                rxBytes = 2345678901L,
                txBytes = 1234567890L,
                signal = 0,
                isOnline = true,
                isBlocked = false,
                note = "台式机"
            ),
            DeviceInfo(
                ip = "192.168.1.15",
                mac = "AA:BB:CC:DD:EE:06",
                hostname = "Smart-TV",
                vendor = "LG Electronics",
                interfaceName = "wlan0",
                connectedTime = 172800,
                rxBytes = 1567890123L,
                txBytes = 123456789L,
                signal = -68,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.16",
                mac = "AA:BB:CC:DD:EE:07",
                hostname = "Switch-Lite",
                vendor = "Nintendo Co., Ltd.",
                interfaceName = "wlan0",
                connectedTime = 43200,
                rxBytes = 234567890L,
                txBytes = 56789012L,
                signal = -72,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.17",
                mac = "AA:BB:CC:DD:EE:08",
                hostname = "Smart-Speaker",
                vendor = "Amazon.com Inc.",
                interfaceName = "wlan0",
                connectedTime = 604800,
                rxBytes = 12345678L,
                txBytes = 3456789L,
                signal = -75,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.18",
                mac = "AA:BB:CC:DD:EE:09",
                hostname = "Smart-Bulb",
                vendor = "Philips Lighting",
                interfaceName = "wlan0",
                connectedTime = 864000,
                rxBytes = 1234567L,
                txBytes = 234567L,
                signal = -80,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.19",
                mac = "AA:BB:CC:DD:EE:10",
                hostname = "Security-Camera",
                vendor = "Hangzhou Hikvision Digital Technology",
                interfaceName = "eth2",
                connectedTime = 1209600,
                rxBytes = 3456789012L,
                txBytes = 123456789L,
                signal = 0,
                isOnline = true,
                isBlocked = false,
                note = "监控摄像头"
            ),
            DeviceInfo(
                ip = "192.168.1.20",
                mac = "AA:BB:CC:DD:EE:11",
                hostname = "Printer",
                vendor = "HP Inc.",
                interfaceName = "eth3",
                connectedTime = 2592000,
                rxBytes = 12345678L,
                txBytes = 5678901L,
                signal = 0,
                isOnline = true,
                isBlocked = false,
                note = ""
            ),
            DeviceInfo(
                ip = "192.168.1.21",
                mac = "AA:BB:CC:DD:EE:12",
                hostname = "NAS-Server",
                vendor = "Synology Inc.",
                interfaceName = "eth1",
                connectedTime = 5184000,
                rxBytes = 5678901234L,
                txBytes = 3456789012L,
                signal = 0,
                isOnline = true,
                isBlocked = false,
                note = "群晖NAS"
            )
        )
    }

    /**
     * 生成假的已安装插件列表
     */
    fun getFakeInstalledPackages(): List<PackageInfo> {
        return listOf(
            PackageInfo(
                name = "luci-i18n-base-zh-cn",
                version = "23.05.0",
                description = "LuCI Chinese (Simplified) Translation - Base",
                size = 262144L, // 256KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-opkg",
                version = "23.05.0",
                description = "LuCI OPKG Package Manager",
                size = 65536L, // 64KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-firewall",
                version = "23.05.0",
                description = "LuCI Firewall Configuration",
                size = 131072L, // 128KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-ddns",
                version = "2.4.0",
                description = "LuCI Dynamic DNS Configuration",
                size = 98304L, // 96KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-upnp",
                version = "23.05.0",
                description = "LuCI UPnP Configuration",
                size = 49152L, // 48KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-wol",
                version = "23.05.0",
                description = "LuCI Wake-on-LAN",
                size = 32768L, // 32KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-sqm",
                version = "1.6.0",
                description = "LuCI SQM QoS",
                size = 81920L, // 80KB
                installed = true
            ),
            PackageInfo(
                name = "luci-app-statistics",
                version = "23.05.0",
                description = "LuCI Statistics",
                size = 163840L, // 160KB
                installed = true
            )
        )
    }

    /**
     * 生成假的可用插件列表
     */
    fun getFakeAvailablePackages(): List<PackageInfo> {
        return listOf(
            PackageInfo(
                name = "luci-app-aria2",
                version = "1.36.0",
                description = "LuCI Aria2 Download Manager",
                size = 131072L, // 128KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-transmission",
                version = "4.0.0",
                description = "LuCI Transmission BitTorrent Client",
                size = 163840L, // 160KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-samba4",
                version = "4.18.0",
                description = "LuCI Samba4 Network Shares",
                size = 98304L, // 96KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-minidlna",
                version = "1.3.0",
                description = "LuCI MiniDLNA Media Server",
                size = 65536L, // 64KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-openvpn",
                version = "2.5.0",
                description = "LuCI OpenVPN Configuration",
                size = 196608L, // 192KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-wireguard",
                version = "1.0.0",
                description = "LuCI WireGuard VPN",
                size = 81920L, // 80KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-mwan3",
                version = "2.11.0",
                description = "LuCI Multi-WAN Load Balancing",
                size = 147456L, // 144KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-snmpd",
                version = "5.9.0",
                description = "LuCI SNMP Daemon",
                size = 73728L, // 72KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-nlbwmon",
                version = "2023.09.12",
                description = "LuCI Network Bandwidth Monitor",
                size = 57344L, // 56KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-ntpc",
                version = "23.05.0",
                description = "LuCI NTP Time Synchronization",
                size = 40960L, // 40KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-uhttpd",
                version = "2023.06.25",
                description = "LuCI uHTTPd Web Server",
                size = 49152L, // 48KB
                installed = false
            ),
            PackageInfo(
                name = "luci-app-acl",
                version = "23.05.0",
                description = "LuCI Access Control Lists",
                size = 65536L, // 64KB
                installed = false
            )
        )
    }

    /**
     * 生成假的端口转发规则
     */
    fun getFakePortForwards(): List<PortForwardRule> {
        return listOf(
            PortForwardRule(
                name = "HTTP_Server",
                proto = "tcp",
                src = "wan",
                srcPort = "8080",
                dest = "lan",
                destIp = "192.168.1.10",
                destPort = "80",
                enabled = true
            ),
            PortForwardRule(
                name = "SSH_Access",
                proto = "tcp",
                src = "wan",
                srcPort = "2222",
                dest = "lan",
                destIp = "192.168.1.14",
                destPort = "22",
                enabled = true
            ),
            PortForwardRule(
                name = "RDP_Desktop",
                proto = "tcp",
                src = "wan",
                srcPort = "3389",
                dest = "lan",
                destIp = "192.168.1.15",
                destPort = "3389",
                enabled = false
            )
        )
    }

    /**
     * 生成假的DDNS配置
     */
    fun getFakeDdnsConfigs(): List<DdnsConfig> {
        return listOf(
            DdnsConfig(
                name = "myddns",
                service = "no-ip.com",
                domain = "myhome.ddns.net",
                username = "user@example.com",
                password = "",
                enabled = true
            ),
            DdnsConfig(
                name = "aliyun",
                service = "aliyun.com",
                domain = "home.example.com",
                username = "access_key",
                password = "",
                enabled = false
            )
        )
    }

    /**
     * 生成假的系统日志
     */
    fun getFakeSystemLog(): List<LogEntry> {
        return listOf(
            LogEntry(time = "Aug  7 10:00:01", level = "info", facility = "system", message = "System started"),
            LogEntry(time = "Aug  7 10:00:02", level = "info", facility = "kernel", message = "Loading kernel modules..."),
            LogEntry(time = "Aug  7 10:00:03", level = "info", facility = "kernel", message = "eth0: link up, 1000Mbps full-duplex"),
            LogEntry(time = "Aug  7 10:00:05", level = "info", facility = "network", message = "wan: DHCP lease obtained, IP 192.168.1.100"),
            LogEntry(time = "Aug  7 10:00:10", level = "info", facility = "dns", message = "dnsmasq: started, version 2.89"),
            LogEntry(time = "Aug  7 10:00:15", level = "info", facility = "firewall", message = "firewall: started"),
            LogEntry(time = "Aug  7 10:01:23", level = "info", facility = "wifi", message = "wlan0: AP-STA-CONNECTED AA:BB:CC:DD:EE:01"),
            LogEntry(time = "Aug  7 10:02:45", level = "warn", facility = "wifi", message = "wlan0: AP-STA-DISCONNECTED AA:BB:CC:DD:EE:02"),
            LogEntry(time = "Aug  7 10:05:30", level = "info", facility = "wifi", message = "wlan0: AP-STA-CONNECTED AA:BB:CC:DD:EE:02"),
            LogEntry(time = "Aug  7 10:10:00", level = "info", facility = "ntp", message = "ntpd: time synchronized with 114.114.114.114"),
            LogEntry(time = "Aug  7 10:15:22", level = "info", facility = "wifi", message = "wlan1: AP-STA-CONNECTED AA:BB:CC:DD:EE:04"),
            LogEntry(time = "Aug  7 10:20:15", level = "warn", facility = "dns", message = "dnsmasq: possible DNS-rebind attack detected"),
            LogEntry(time = "Aug  7 10:30:00", level = "info", facility = "cron", message = "cron: running scheduled tasks"),
            LogEntry(time = "Aug  7 10:45:33", level = "info", facility = "wifi", message = "wlan0: AP-STA-CONNECTED AA:BB:CC:DD:EE:07"),
            LogEntry(time = "Aug  7 11:00:00", level = "info", facility = "system", message = "System uptime: 3 days, 12 hours, 45 minutes")
        )
    }

    /**
     * 生成假的进程列表
     */
    fun getFakeProcessList(): List<ProcessInfo> {
        return listOf(
            ProcessInfo(pid = 1, name = "init", cpu = 0.0f, memory = 0.5f, vsz = 1024L),
            ProcessInfo(pid = 2, name = "kthreadd", cpu = 0.0f, memory = 0.0f, vsz = 0L),
            ProcessInfo(pid = 123, name = "netifd", cpu = 0.2f, memory = 1.2f, vsz = 2048L),
            ProcessInfo(pid = 234, name = "uhttpd", cpu = 0.1f, memory = 0.8f, vsz = 1536L),
            ProcessInfo(pid = 345, name = "dnsmasq", cpu = 0.3f, memory = 1.5f, vsz = 3072L),
            ProcessInfo(pid = 456, name = "hostapd", cpu = 0.5f, memory = 2.1f, vsz = 4096L),
            ProcessInfo(pid = 567, name = "wpa_supplicant", cpu = 0.1f, memory = 0.9f, vsz = 1792L),
            ProcessInfo(pid = 678, name = "crond", cpu = 0.0f, memory = 0.3f, vsz = 512L),
            ProcessInfo(pid = 789, name = "syslogd", cpu = 0.0f, memory = 0.4f, vsz = 768L),
            ProcessInfo(pid = 890, name = "klogd", cpu = 0.0f, memory = 0.2f, vsz = 384L),
            ProcessInfo(pid = 1024, name = "dropbear", cpu = 0.0f, memory = 0.6f, vsz = 1152L),
            ProcessInfo(pid = 2048, name = "ubusd", cpu = 0.1f, memory = 0.7f, vsz = 1280L),
            ProcessInfo(pid = 3072, name = "rpcd", cpu = 0.2f, memory = 1.0f, vsz = 2048L),
            ProcessInfo(pid = 4096, name = "odhcpd", cpu = 0.1f, memory = 0.8f, vsz = 1536L),
            ProcessInfo(pid = 5120, name = "firewall", cpu = 0.0f, memory = 0.5f, vsz = 1024L)
        )
    }
}
