package org.openwrt.manager.ui.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.openwrt.manager.R

/**
 * 首页 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddRouter: () -> Unit,
    onNavigateToDevices: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToWifi: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToSystem: () -> Unit,
    onNavigateToAdvanced: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "OpenWrt 管家",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (!uiState.hasRouter) {
            EmptyRouterView(
                onAddRouter = onAddRouter,
                modifier = Modifier.padding(padding)
            )
        } else {
            HomeContent(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToDevices = onNavigateToDevices,
                onNavigateToPlugins = onNavigateToPlugins,
                onNavigateToWifi = onNavigateToWifi,
                onNavigateToNetwork = onNavigateToNetwork,
                onNavigateToSystem = onNavigateToSystem,
                onNavigateToAdvanced = onNavigateToAdvanced,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

/**
 * 空路由器视图
 */
@Composable
fun EmptyRouterView(
    onAddRouter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1677FF),
                            Color(0xFF4096FF)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Router,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "还没有添加路由器",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "添加你的 OpenWrt 路由器，开始智能管理",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = onAddRouter,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text(
                text = "添加路由器",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 首页内容 - 小米路由器风格
 */
@Composable
fun HomeContent(
    uiState: HomeViewModel.HomeUiState,
    viewModel: HomeViewModel,
    onNavigateToDevices: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToWifi: () -> Unit,
    onNavigateToNetwork: () -> Unit,
    onNavigateToSystem: () -> Unit,
    onNavigateToAdvanced: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 路由器状态大卡片
        RouterStatusCard(
            uiState = uiState,
            onRefresh = { viewModel.refresh() }
        )

        // 网速监控卡片
        NetworkSpeedCard(
            uiState = uiState
        )

        // 常用功能网格
        QuickFunctionsGrid(
            onNavigateToDevices = onNavigateToDevices,
            onNavigateToWifi = onNavigateToWifi,
            onNavigateToPlugins = onNavigateToPlugins,
            onNavigateToNetwork = onNavigateToNetwork
        )

        // 更多功能
        MoreFunctionsSection(
            onNavigateToSystem = onNavigateToSystem,
            onNavigateToAdvanced = onNavigateToAdvanced
        )
    }
}

/**
 * 路由器状态大卡片 - 小米风格
 */
@Composable
fun RouterStatusCard(
    uiState: HomeViewModel.HomeUiState,
    onRefresh: () -> Unit
) {
    val status = uiState.routerStatus
    val isOnline = status != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isOnline) {
                            listOf(
                                Color(0xFF1677FF),
                                Color(0xFF4096FF),
                                Color(0xFF69B1FF)
                            )
                        } else {
                            listOf(
                                Color(0xFF8C8C8C),
                                Color(0xFFBFBFBF)
                            )
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                // 顶部：路由器名称和状态
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 路由器图标
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Router,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.activeRouter?.name ?: "OpenWrt 路由器",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isOnline) "在线" else "离线",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    // 状态指示灯
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOnline) Color(0xFF52C41A)
                                else Color(0xFFFF4D4F)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 底部：关键数据
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatusItem(
                        icon = Icons.Default.Devices,
                        label = "在线设备",
                        value = "${status?.onlineDevices ?: 0}",
                        unit = "台"
                    )
                    StatusItem(
                        icon = Icons.Default.Wifi,
                        label = "WiFi",
                        value = if (isOnline) "正常" else "关闭",
                        unit = ""
                    )
                    StatusItem(
                        icon = Icons.Default.Speed,
                        label = "CPU",
                        value = "${((status?.cpuUsage ?: 0f) * 100).toInt()}",
                        unit = "%"
                    )
                }
            }

            // 加载中遮罩
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

/**
 * 状态数据项
 */
@Composable
fun StatusItem(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

/**
 * 网速监控卡片
 */
@Composable
fun NetworkSpeedCard(
    uiState: HomeViewModel.HomeUiState
) {
    val status = uiState.routerStatus

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "网络状态",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (status?.wanConnected == true) "已连接" else "未连接",
                    fontSize = 14.sp,
                    color = if (status?.wanConnected == true)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // 下载
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "下载",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "0.0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "MB/s",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // 上传
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "上传",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "0.0",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF52C41A)
                    )
                    Text(
                        text = "MB/s",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!status?.wanIp.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WAN IP",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = status?.wanIp ?: "",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 常用功能网格
 */
@Composable
fun QuickFunctionsGrid(
    onNavigateToDevices: () -> Unit,
    onNavigateToWifi: () -> Unit,
    onNavigateToPlugins: () -> Unit,
    onNavigateToNetwork: () -> Unit
) {
    Column {
        Text(
            text = "常用功能",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    FunctionItem(
                        icon = Icons.Default.Devices,
                        label = "在线设备",
                        color = Color(0xFF1677FF),
                        onClick = onNavigateToDevices
                    )
                    FunctionItem(
                        icon = Icons.Default.Wifi,
                        label = "WiFi 设置",
                        color = Color(0xFF13C2C2),
                        onClick = onNavigateToWifi
                    )
                    FunctionItem(
                        icon = Icons.Default.Settings,
                        label = "插件管理",
                        color = Color(0xFF722ED1),
                        onClick = onNavigateToPlugins
                    )
                    FunctionItem(
                        icon = Icons.Default.Router,
                        label = "网络设置",
                        color = Color(0xFFFA8C16),
                        onClick = onNavigateToNetwork
                    )
                }
            }
        }
    }
}

/**
 * 功能项
 */
@Composable
fun FunctionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 更多功能区域
 */
@Composable
fun MoreFunctionsSection(
    onNavigateToSystem: () -> Unit,
    onNavigateToAdvanced: () -> Unit
) {
    Column {
        Text(
            text = "更多功能",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                MoreFunctionItem(
                    icon = Icons.Default.Memory,
                    label = "系统管理",
                    subtitle = "日志、进程、系统信息",
                    color = Color(0xFF1677FF),
                    onClick = onNavigateToSystem
                )
                androidx.compose.material3.Divider(modifier = Modifier.padding(horizontal = 16.dp))
                MoreFunctionItem(
                    icon = Icons.Default.Settings,
                    label = "高级功能",
                    subtitle = "更多高级设置选项",
                    color = Color(0xFF722ED1),
                    onClick = onNavigateToAdvanced
                )
            }
        }
    }
}

/**
 * 更多功能项
 */
@Composable
fun MoreFunctionItem(
    icon: ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = color
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.Refresh, // 用chevron_right代替
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
