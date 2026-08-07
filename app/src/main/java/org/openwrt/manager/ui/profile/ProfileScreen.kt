package org.openwrt.manager.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openwrt.manager.R

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
    onNavigateToAdvanced: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F7FA),
                    titleContentColor = Color(0xFF1D2129)
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            UserInfoCard()

            Spacer(modifier = Modifier.height(20.dp))

            // 功能工具
            Text(
                text = "功能工具",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4E5969),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

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
            Text(
                text = stringResource(R.string.profile_settings),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4E5969),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SettingsList()

            Spacer(modifier = Modifier.height(20.dp))

            AboutCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
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
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolItem(
                icon = Icons.Default.BugReport,
                title = "系统管理",
                subtitle = "日志/进程",
                iconBg = Color(0xFFE8F3FF),
                iconColor = Color(0xFF1677FF),
                onClick = onNavigateToSystem,
                modifier = Modifier.weight(1f)
            )
            ToolItem(
                icon = Icons.Default.SignalCellularAlt,
                title = "网络设置",
                subtitle = "LAN/WAN/DHCP",
                iconBg = Color(0xFFE8FFEA),
                iconColor = Color(0xFF00B42A),
                onClick = onNavigateToNetwork,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolItem(
                icon = Icons.Default.Wifi,
                title = "WiFi设置",
                subtitle = "2.4G/5G/访客",
                iconBg = Color(0xFFFFF0E6),
                iconColor = Color(0xFFFF7D00),
                onClick = onNavigateToWifi,
                modifier = Modifier.weight(1f)
            )
            ToolItem(
                icon = Icons.Default.Security,
                title = "防火墙",
                subtitle = "端口转发/DMZ",
                iconBg = Color(0xFFF5E8FF),
                iconColor = Color(0xFF722ED1),
                onClick = onNavigateToFirewall,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolItem(
                icon = Icons.Default.Dns,
                title = "DDNS",
                subtitle = "动态域名",
                iconBg = Color(0xFFE6F7FF),
                iconColor = Color(0xFF36CFC9),
                onClick = onNavigateToDdns,
                modifier = Modifier.weight(1f)
            )
            ToolItem(
                icon = Icons.Default.Memory,
                title = "高级功能",
                subtitle = "SSH/文件管理",
                iconBg = Color(0xFFFFF1F0),
                iconColor = Color(0xFFF53F3F),
                onClick = onNavigateToAdvanced,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ToolItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = iconBg,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D2129)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF86909C)
            )
        }
    }
}

@Composable
fun UserInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 用户头像
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1677FF),
                                Color(0xFF4096FF)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "OpenWrt 用户",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D2129)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "管理你的 OpenWrt 设备",
                    fontSize = 13.sp,
                    color = Color(0xFF86909C)
                )
            }
        }
    }
}

@Composable
fun SettingsList() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column {
            SettingItem(
                icon = Icons.Default.Palette,
                title = "主题设置",
                subtitle = "深色模式、动态颜色",
                iconBg = Color(0xFFE8F3FF),
                iconColor = Color(0xFF1677FF)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFF2F3F5))
                    .padding(start = 56.dp)
            )
            SettingItem(
                icon = Icons.Default.Security,
                title = "安全设置",
                subtitle = "密码、生物识别",
                iconBg = Color(0xFFE8FFEA),
                iconColor = Color(0xFF00B42A)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFF2F3F5))
                    .padding(start = 56.dp)
            )
            SettingItem(
                icon = Icons.Default.Notifications,
                title = "通知设置",
                subtitle = "推送通知、告警",
                iconBg = Color(0xFFFFF0E6),
                iconColor = Color(0xFFFF7D00)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color(0xFFF2F3F5))
                    .padding(start = 56.dp)
            )
            SettingItem(
                icon = Icons.Default.Language,
                title = "语言设置",
                subtitle = "简体中文",
                iconBg = Color(0xFFF5E8FF),
                iconColor = Color(0xFF722ED1)
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBg: Color,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = iconBg,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1D2129)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF86909C)
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFC9CDD4),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun AboutCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1677FF),
                                Color(0xFF4096FF)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D2129)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.profile_version) + " 2.1.0",
                fontSize = 13.sp,
                color = Color(0xFF86909C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "基于 LuCI ubus API 开发",
                fontSize = 12.sp,
                color = Color(0xFFC9CDD4)
            )
        }
    }
}
