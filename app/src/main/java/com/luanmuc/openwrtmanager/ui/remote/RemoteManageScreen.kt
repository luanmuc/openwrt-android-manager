package com.luanmuc.openwrtmanager.ui.remote

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiTheme
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.util.DebounceUtils

/**
 * 远程管理配置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteManageScreen(
    onBack: () -> Unit = {},
    remoteEnabled: Boolean = false,
    remoteAddress: String = "",
    remotePort: Int = 443,
    remoteProtocol: String = "https",
    autoSwitchRemote: Boolean = true,
    isRemoteMode: Boolean = false,
    onRemoteEnabledChange: (Boolean) -> Unit = {},
    onRemoteAddressChange: (String) -> Unit = {},
    onRemotePortChange: (Int) -> Unit = {},
    onRemoteProtocolChange: (String) -> Unit = {},
    onAutoSwitchRemoteChange: (Boolean) -> Unit = {},
    onForceRemoteMode: (Boolean) -> Unit = {},
    onTestConnection: () -> Unit = {}
) {
    val context = LocalContext.current
    var showProtocolDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MiTopAppBar(
                title = "远程管理",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MiTheme.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.HelpOutline,
                            contentDescription = "帮助",
                            tint = MiTheme.TextPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // 远程模式状态卡片
            RemoteStatusCard(
                isRemoteMode = isRemoteMode,
                remoteEnabled = remoteEnabled,
                onForceRemoteMode = onForceRemoteMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 远程管理开关
            RemoteSettingCard(
                title = "启用远程管理",
                description = "开启后可在外网通过域名访问路由器",
                icon = Icons.Filled.Wifi,
                gradient = MiColors.GradientBlue
            ) {
                Switch(
                    checked = remoteEnabled,
                    onCheckedChange = onRemoteEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MiTheme.Primary,
                        checkedTrackColor = MiTheme.Primary.copy(alpha = 0.3f)
                    )
                )
            }

            if (remoteEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                // 远程地址配置
                RemoteConfigCard(
                    remoteAddress = remoteAddress,
                    remotePort = remotePort,
                    remoteProtocol = remoteProtocol,
                    onRemoteAddressChange = onRemoteAddressChange,
                    onRemotePortChange = onRemotePortChange,
                    onProtocolClick = { showProtocolDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 自动切换开关
                RemoteSettingCard(
                    title = "自动切换远程模式",
                    description = "检测到不在局域网时自动使用远程地址",
                    icon = Icons.Filled.SwapHoriz,
                    gradient = MiColors.GradientGreen
                ) {
                    Switch(
                        checked = autoSwitchRemote,
                        onCheckedChange = onAutoSwitchRemoteChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MiTheme.Primary,
                            checkedTrackColor = MiTheme.Primary.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 测试连接按钮
                Button(
                    onClick = {
                        if (DebounceUtils.canClick()) {
                            onTestConnection()
                            Toast.makeText(context, "正在测试远程连接...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MiTheme.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("测试远程连接", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 方案说明
            SectionTitle(title = "技术方案")

            Spacer(modifier = Modifier.height(8.dp))

            // DDNS方案卡片
            SolutionCard(
                title = "方案一：DDNS + 端口映射",
                description = "适合有公网IP的用户，配置简单",
                icon = Icons.Filled.Dns,
                gradient = MiColors.GradientBlue,
                isRecommended = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 内网穿透方案卡片
            SolutionCard(
                title = "方案二：内网穿透",
                description = "适合无公网IP，需要frp/ngrok服务器",
                icon = Icons.Filled.VpnLock,
                gradient = MiColors.GradientPurple,
                isRecommended = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // VPN方案卡片
            SolutionCard(
                title = "方案三：VPN接入",
                description = "最安全，需要配置WireGuard/OpenVPN",
                icon = Icons.Filled.Security,
                gradient = MiColors.GradientGreen,
                isRecommended = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 路由器配置指引
            SectionTitle(title = "路由器配置指引")

            Spacer(modifier = Modifier.height(8.dp))

            GuideStepCard(
                step = 1,
                title = "配置DDNS",
                description = "在路由器服务中配置DDNS客户端（如花生壳、阿里云DDNS）"
            )

            GuideStepCard(
                step = 2,
                title = "开放端口",
                description = "在防火墙中开放LuCI的HTTPS端口（默认443）到公网"
            )

            GuideStepCard(
                step = 3,
                title = "启用HTTPS",
                description = "建议启用HTTPS并配置SSL证书，确保传输安全"
            )

            GuideStepCard(
                step = 4,
                title = "配置APP",
                description = "在上方填写DDNS域名和端口，测试连接成功后保存"
            )
        }
    }

    // 协议选择对话框
    if (showProtocolDialog) {
        AlertDialog(
            onDismissRequest = { showProtocolDialog = false },
            title = { Text("选择协议") },
            text = {
                Column {
                    listOf("https", "http").forEach { protocol ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onRemoteProtocolChange(protocol)
                                    showProtocolDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = remoteProtocol == protocol,
                                onClick = {
                                    onRemoteProtocolChange(protocol)
                                    showProtocolDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(protocol.uppercase())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProtocolDialog = false }) {
                    Text("取消")
                }
            },
            containerColor = MiTheme.CardBackground,
            titleContentColor = MiTheme.TextPrimary,
            textContentColor = MiTheme.TextSecondary
        )
    }

    // 帮助对话框
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("远程管理说明") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("远程管理允许您在外网通过互联网管理路由器。")
                    Text("推荐使用DDNS + 端口映射方案：")
                    Text("1. 确保路由器有公网IP")
                    Text("2. 配置DDNS动态域名解析")
                    Text("3. 在防火墙开放LuCI端口")
                    Text("4. 建议使用HTTPS确保安全")
                    Text("注意：开放端口到公网有安全风险，请使用强密码并定期更新。")
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("我知道了")
                }
            },
            containerColor = MiTheme.CardBackground,
            titleContentColor = MiTheme.TextPrimary,
            textContentColor = MiTheme.TextSecondary
        )
    }
}

@Composable
private fun RemoteStatusCard(
    isRemoteMode: Boolean,
    remoteEnabled: Boolean,
    onForceRemoteMode: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRemoteMode) MiTheme.Success.copy(alpha = 0.1f) else MiTheme.CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isRemoteMode) MiTheme.Success.copy(alpha = 0.2f)
                            else MiTheme.TextTertiary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRemoteMode) Icons.Filled.CheckCircle else Icons.Filled.Wifi,
                        contentDescription = null,
                        tint = if (isRemoteMode) MiTheme.Success else MiTheme.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRemoteMode) "当前：远程模式" else "当前：本地模式",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiTheme.TextPrimary
                    )
                    Text(
                        text = if (isRemoteMode) "通过互联网连接路由器" else "通过局域网连接路由器",
                        fontSize = 13.sp,
                        color = MiTheme.TextSecondary
                    )
                }
            }

            if (remoteEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MiTheme.Divider)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "强制使用远程模式",
                        fontSize = 14.sp,
                        color = MiTheme.TextSecondary
                    )
                    Switch(
                        checked = isRemoteMode,
                        onCheckedChange = onForceRemoteMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MiTheme.Primary,
                            checkedTrackColor = MiTheme.Primary.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteSettingCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MiTheme.CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiTheme.TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MiTheme.TextSecondary
                )
            }
            content()
        }
    }
}

@Composable
private fun RemoteConfigCard(
    remoteAddress: String,
    remotePort: Int,
    remoteProtocol: String,
    onRemoteAddressChange: (String) -> Unit,
    onRemotePortChange: (Int) -> Unit,
    onProtocolClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MiTheme.CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "远程访问配置",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MiTheme.TextPrimary
            )

            // 协议选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "协议",
                    fontSize = 14.sp,
                    color = MiTheme.TextSecondary,
                    modifier = Modifier.width(60.dp)
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onProtocolClick),
                    shape = RoundedCornerShape(10.dp),
                    color = MiTheme.Background
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = remoteProtocol.uppercase(),
                            fontSize = 14.sp,
                            color = MiTheme.TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MiTheme.TextSecondary
                        )
                    }
                }
            }

            // 远程地址
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "域名/IP",
                    fontSize = 14.sp,
                    color = MiTheme.TextSecondary,
                    modifier = Modifier.width(60.dp)
                )
                OutlinedTextField(
                    value = remoteAddress,
                    onValueChange = onRemoteAddressChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("example.com", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MiTheme.Primary,
                        unfocusedBorderColor = MiTheme.Divider
                    )
                )
            }

            // 端口
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "端口",
                    fontSize = 14.sp,
                    color = MiTheme.TextSecondary,
                    modifier = Modifier.width(60.dp)
                )
                OutlinedTextField(
                    value = remotePort.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { onRemotePortChange(it) }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("443", fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MiTheme.Primary,
                        unfocusedBorderColor = MiTheme.Divider
                    )
                )
            }

            // 访问地址预览
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MiTheme.Background
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Link,
                        contentDescription = null,
                        tint = MiTheme.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${remoteProtocol}://${remoteAddress.ifEmpty { "example.com" }}:${remotePort}",
                        fontSize = 13.sp,
                        color = MiTheme.TextSecondary,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MiTheme.TextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun SolutionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    isRecommended: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MiTheme.CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MiTheme.TextPrimary
                    )
                    if (isRecommended) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MiTheme.Success.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "推荐",
                                fontSize = 10.sp,
                                color = MiTheme.Success,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MiTheme.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun GuideStepCard(
    step: Int,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MiTheme.CardBackground)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MiTheme.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = step.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiTheme.Primary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MiTheme.TextPrimary
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MiTheme.TextSecondary
                )
            }
        }
    }
}
