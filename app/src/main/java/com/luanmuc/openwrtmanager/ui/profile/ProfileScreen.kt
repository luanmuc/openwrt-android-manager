package com.luanmuc.openwrtmanager.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luanmuc.openwrtmanager.R
import com.luanmuc.openwrtmanager.data.repository.CacheRepository
import com.luanmuc.openwrtmanager.data.repository.SettingsRepository
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiListItem
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.theme.ThemeManager
import com.luanmuc.openwrtmanager.ui.theme.ThemeMode
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.launch
import android.widget.Toast

/**
 * 我的页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSystem: () -> Unit = {},
    onNavigateToNetwork: () -> Unit = {},
    onNavigateToWifi: () -> Unit = {},
    onNavigateToFirewall: () -> Unit = {},
    onNavigateToDdns: () -> Unit = {},
    onNavigateToAdvanced: () -> Unit = {},
    onDebugModeToggled: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = remember { SettingsRepository.getInstance(context) }
    val themeManager = remember { ThemeManager.getInstance(context) }
    val cacheRepository = remember { CacheRepository.getInstance(context) }

    // 对话框状态
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showRefreshIntervalDialog by remember { mutableStateOf(false) }
    var showConnectionTimeoutDialog by remember { mutableStateOf(false) }
    var showRetryCountDialog by remember { mutableStateOf(false) }
    var showTrafficUnitDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    // 缓存大小状态
    var cacheSizeText by remember { mutableStateOf("计算中...") }

    // 计算缓存大小
    LaunchedEffect(Unit) {
        try {
            val size = cacheRepository.getCacheSizeBytes("")
            cacheSizeText = formatSize(size)
        } catch (e: Exception) {
            cacheSizeText = "0 B"
        }
    }

    Scaffold(
        topBar = {
            MiTopAppBar(
                title = stringResource(R.string.profile_title)
            )
        },
        containerColor = MiColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            UserInfoCard()
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 功能工具
            SectionTitle(title = "功能工具")
            ToolsGrid(
                onNavigateToSystem = onNavigateToSystem,
                onNavigateToNetwork = onNavigateToNetwork,
                onNavigateToWifi = onNavigateToWifi,
                onNavigateToFirewall = onNavigateToFirewall,
                onNavigateToDdns = onNavigateToDdns,
                onNavigateToAdvanced = onNavigateToAdvanced
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 通用设置
            SectionTitle(title = "通用设置")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "主题设置",
                        subtitle = getThemeModeName(themeManager.currentMode),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MiColors.Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showThemeDialog = true }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "语言设置",
                        subtitle = settings.language.displayName,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MiColors.Purple,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showLanguageDialog = true }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "清除缓存",
                        subtitle = cacheSizeText,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MiColors.Error,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showClearCacheDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 刷新设置
            SectionTitle(title = "刷新设置")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "自动刷新",
                        subtitle = if (settings.autoRefreshEnabled) "已开启" else "已关闭",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MiColors.Success,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.autoRefreshEnabled,
                                onCheckedChange = {
                                    settings.autoRefreshEnabled = it
                                }
                            )
                        }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "刷新间隔",
                        subtitle = getRefreshIntervalName(settings.refreshInterval),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = MiColors.Warning,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showRefreshIntervalDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 连接设置
            SectionTitle(title = "连接设置")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "连接超时",
                        subtitle = "${settings.connectionTimeout} 秒",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = MiColors.Cyan,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showConnectionTimeoutDialog = true }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "重试次数",
                        subtitle = "${settings.retryCount} 次",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MiColors.Orange,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showRetryCountDialog = true }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "HTTPS自动检测",
                        subtitle = if (settings.httpsAutoDetect) "已开启" else "已关闭",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MiColors.Success,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.httpsAutoDetect,
                                onCheckedChange = {
                                    settings.httpsAutoDetect = it
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 通知设置
            SectionTitle(title = "通知设置")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "推送通知",
                        subtitle = if (settings.pushNotificationEnabled) "已开启" else "已关闭",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MiColors.Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.pushNotificationEnabled,
                                onCheckedChange = {
                                    settings.pushNotificationEnabled = it
                                }
                            )
                        }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "离线提醒",
                        subtitle = if (settings.offlineAlertEnabled) "已开启" else "已关闭",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = MiColors.Warning,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.offlineAlertEnabled,
                                onCheckedChange = {
                                    settings.offlineAlertEnabled = it
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 其他设置
            SectionTitle(title = "其他设置")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "流量单位",
                        subtitle = settings.trafficUnit.displayName,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = MiColors.Purple,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showTrafficUnitDialog = true }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "状态栏显示",
                        subtitle = if (settings.statusBarEnabled) "已开启" else "已关闭",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MiColors.Cyan,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        trailing = {
                            Switch(
                                checked = settings.statusBarEnabled,
                                onCheckedChange = {
                                    settings.statusBarEnabled = it
                                }
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AboutCard(onDebugModeToggled = onDebugModeToggled)
        }
    }

    // 主题设置对话框
    if (showThemeDialog) {
        SelectionDialog(
            title = "主题设置",
            options = listOf("浅色模式", "深色模式", "跟随系统"),
            selectedIndex = when (themeManager.currentMode) {
                ThemeMode.LIGHT -> 0
                ThemeMode.DARK -> 1
                ThemeMode.SYSTEM -> 2
            },
            onDismiss = { showThemeDialog = false },
            onSelect = { index ->
                val mode = when (index) {
                    0 -> ThemeMode.LIGHT
                    1 -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
                themeManager.setThemeMode(mode)
                showThemeDialog = false
                Toast.makeText(context, "主题已切换", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 语言设置对话框
    if (showLanguageDialog) {
        SelectionDialog(
            title = "语言设置",
            options = com.luanmuc.openwrtmanager.data.repository.Language.values().map { it.displayName },
            selectedIndex = com.luanmuc.openwrtmanager.data.repository.Language.values().indexOf(settings.language),
            onDismiss = { showLanguageDialog = false },
            onSelect = { index ->
                settings.language = com.luanmuc.openwrtmanager.data.repository.Language.values()[index]
                showLanguageDialog = false
                Toast.makeText(context, "语言已设置", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 刷新间隔对话框
    if (showRefreshIntervalDialog) {
        val intervals = listOf(5, 10, 30, 60, 300)
        val names = listOf("5秒", "10秒", "30秒", "1分钟", "5分钟")
        SelectionDialog(
            title = "刷新间隔",
            options = names,
            selectedIndex = intervals.indexOf(settings.refreshInterval),
            onDismiss = { showRefreshIntervalDialog = false },
            onSelect = { index ->
                settings.refreshInterval = intervals[index]
                showRefreshIntervalDialog = false
                Toast.makeText(context, "刷新间隔已设置", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 连接超时对话框
    if (showConnectionTimeoutDialog) {
        val timeouts = listOf(5, 10, 30, 60)
        val names = listOf("5秒", "10秒", "30秒", "1分钟")
        SelectionDialog(
            title = "连接超时",
            options = names,
            selectedIndex = timeouts.indexOf(settings.connectionTimeout),
            onDismiss = { showConnectionTimeoutDialog = false },
            onSelect = { index ->
                settings.connectionTimeout = timeouts[index]
                showConnectionTimeoutDialog = false
                Toast.makeText(context, "连接超时已设置", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 重试次数对话框
    if (showRetryCountDialog) {
        val counts = listOf(1, 2, 3, 5)
        val names = listOf("1次", "2次", "3次", "5次")
        SelectionDialog(
            title = "重试次数",
            options = names,
            selectedIndex = counts.indexOf(settings.retryCount),
            onDismiss = { showRetryCountDialog = false },
            onSelect = { index ->
                settings.retryCount = counts[index]
                showRetryCountDialog = false
                Toast.makeText(context, "重试次数已设置", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 流量单位对话框
    if (showTrafficUnitDialog) {
        val units = com.luanmuc.openwrtmanager.data.repository.TrafficUnit.values()
        SelectionDialog(
            title = "流量单位",
            options = units.map { it.displayName },
            selectedIndex = units.indexOf(settings.trafficUnit),
            onDismiss = { showTrafficUnitDialog = false },
            onSelect = { index ->
                settings.trafficUnit = units[index]
                showTrafficUnitDialog = false
                Toast.makeText(context, "流量单位已设置", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 清除缓存确认对话框
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("清除缓存") },
            text = { Text("确定要清除所有缓存数据吗？清除后需要重新从路由器获取数据。") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            cacheRepository.clearAllCache()
                            cacheSizeText = "0 B"
                            Toast.makeText(context, "缓存已清除", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "清除失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    showClearCacheDialog = false
                }) {
                    Text("确定", color = MiColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = MiColors.TextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ToolsGrid(
    onNavigateToSystem: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToWifi: () -> Unit,
    onNavigateToFirewall: () -> Unit,
    onNavigateToDdns: () -> Unit,
    onNavigateToAdvanced: () -> Unit
) {
    MiCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ToolItem(
                    icon = Icons.Default.BugReport,
                    title = "系统管理",
                    gradient = MiColors.GradientBlue,
                    onClick = onNavigateToSystem
                )
                ToolItem(
                    icon = Icons.Default.SignalCellularAlt,
                    title = "网络设置",
                    gradient = MiColors.GradientGreen,
                    onClick = onNavigateToNetwork
                )
                ToolItem(
                    icon = Icons.Default.Wifi,
                    title = "WiFi设置",
                    gradient = MiColors.GradientOrange,
                    onClick = onNavigateToWifi
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ToolItem(
                    icon = Icons.Default.Security,
                    title = "防火墙",
                    gradient = MiColors.GradientRed,
                    onClick = onNavigateToFirewall
                )
                ToolItem(
                    icon = Icons.Default.Dns,
                    title = "DDNS",
                    gradient = MiColors.GradientCyan,
                    onClick = onNavigateToDdns
                )
                ToolItem(
                    icon = Icons.Default.Memory,
                    title = "高级功能",
                    gradient = MiColors.GradientPurple,
                    onClick = onNavigateToAdvanced
                )
            }
        }
    }
}

@Composable
fun ToolItem(
    icon: ImageVector,
    title: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        MiFeatureIcon(
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            },
            gradient = gradient,
            size = 52.dp,
            iconSize = 26.dp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = MiColors.TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun UserInfoCard() {
    MiCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiFeatureIcon(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                },
                gradient = MiColors.GradientBlue,
                size = 56.dp,
                iconSize = 28.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OpenWrt 用户",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "管理你的 OpenWrt 设备",
                    fontSize = 13.sp,
                    color = MiColors.TextTertiary
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MiColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AboutCard(
    onDebugModeToggled: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var clickCount by remember { mutableIntStateOf(0) }
    
    MiCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MiFeatureIcon(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                },
                gradient = MiColors.GradientBlue,
                size = 48.dp,
                iconSize = 24.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "OpenWrt 管家",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "版本 2.3.0-dev",
                fontSize = 13.sp,
                color = MiColors.TextTertiary,
                modifier = Modifier.clickable {
                    clickCount++
                    if (clickCount >= 5) {
                        clickCount = 0
                        val enabled = !DebugMode.isDebugMode
                        DebugMode.toggle()
                        onDebugModeToggled(enabled)
                        Toast.makeText(
                            context,
                            if (enabled) "演示模式已开启" else "演示模式已关闭",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
            if (DebugMode.isDebugMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔧 演示模式已开启",
                    fontSize = 12.sp,
                    color = MiColors.Warning,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MiDivider(indent: Dp = 0.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .height(0.5.dp)
            .background(MiColors.Divider)
    )
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option,
                            fontSize = 15.sp,
                            color = MiColors.TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun getThemeModeName(mode: ThemeMode): String {
    return when (mode) {
        ThemeMode.LIGHT -> "浅色模式"
        ThemeMode.DARK -> "深色模式"
        ThemeMode.SYSTEM -> "跟随系统"
    }
}

private fun getRefreshIntervalName(seconds: Int): String {
    return when (seconds) {
        5 -> "5秒"
        10 -> "10秒"
        30 -> "30秒"
        60 -> "1分钟"
        300 -> "5分钟"
        else -> "$seconds 秒"
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
