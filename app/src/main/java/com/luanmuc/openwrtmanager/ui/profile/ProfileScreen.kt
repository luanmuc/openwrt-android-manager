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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.luanmuc.openwrtmanager.util.DebugMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luanmuc.openwrtmanager.R
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiListItem
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar

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
            
            // 设置列表
            SectionTitle(title = stringResource(R.string.profile_settings))
            SettingsList()
            
            Spacer(modifier = Modifier.height(20.dp))
            
            AboutCard(onDebugModeToggled = onDebugModeToggled)
        }
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
fun SettingsList() {
    MiCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column {
            MiListItem(
                title = "主题设置",
                subtitle = "深色模式、动态颜色",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MiColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {}
            )
            MiDivider(indent = 60.dp)
            MiListItem(
                title = "安全设置",
                subtitle = "密码、生物识别",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MiColors.Success,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {}
            )
            MiDivider(indent = 60.dp)
            MiListItem(
                title = "通知设置",
                subtitle = "推送通知、告警",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MiColors.Warning,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {}
            )
            MiDivider(indent = 60.dp)
            MiListItem(
                title = "语言设置",
                subtitle = "简体中文",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MiColors.Purple,
                        modifier = Modifier.size(22.dp)
                    )
                },
                onClick = {}
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
                text = "版本 2.2.0",
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
