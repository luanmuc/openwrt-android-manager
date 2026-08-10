package com.luanmuc.openwrtmanager.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luanmuc.openwrtmanager.ui.components.MiDimens
import com.luanmuc.openwrtmanager.ui.components.MiTheme
import java.text.SimpleDateFormat
import java.util.*

/**
 * 通知设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val routerStatusEnabled by viewModel.routerStatusEnabled.collectAsState()
    val firmwareEnabled by viewModel.firmwareEnabled.collectAsState()
    val networkAlertEnabled by viewModel.networkAlertEnabled.collectAsState()
    val deviceEventEnabled by viewModel.deviceEventEnabled.collectAsState()
    val notificationHistory by viewModel.notificationHistory.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("通知设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MiTheme.CardBackground,
                    titleContentColor = MiTheme.TextPrimary
                )
            )
        },
        containerColor = MiTheme.Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(MiDimens.horizontalPadding)
        ) {
            // 总开关
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MiDimens.cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MiTheme.CardBackground
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(MiDimens.cardPadding),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "通知总开关",
                                color = MiTheme.TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "开启后接收所有通知",
                                color = MiTheme.TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { viewModel.setNotificationEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MiTheme.Primary,
                                checkedTrackColor = MiTheme.Primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(MiDimens.itemSpacing))
            }
            
            // 通知渠道设置
            item {
                Text(
                    text = "通知渠道",
                    color = MiTheme.TextSecondary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MiDimens.cardRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MiTheme.CardBackground
                    )
                ) {
                    Column {
                        // 路由器状态
                        ChannelSwitchItem(
                            title = "路由器状态",
                            description = "路由器在线/离线状态通知",
                            checked = routerStatusEnabled,
                            enabled = notificationEnabled,
                            onCheckedChange = { viewModel.setRouterStatusEnabled(it) }
                        )
                        
                        Divider(color = MiTheme.Divider, thickness = 0.5.dp)
                        
                        // 固件更新
                        ChannelSwitchItem(
                            title = "固件更新",
                            description = "新固件版本发布通知",
                            checked = firmwareEnabled,
                            enabled = notificationEnabled,
                            onCheckedChange = { viewModel.setFirmwareEnabled(it) }
                        )
                        
                        Divider(color = MiTheme.Divider, thickness = 0.5.dp)
                        
                        // 网络告警
                        ChannelSwitchItem(
                            title = "网络告警",
                            description = "网络异常、断网等告警通知",
                            checked = networkAlertEnabled,
                            enabled = notificationEnabled,
                            onCheckedChange = { viewModel.setNetworkAlertEnabled(it) }
                        )
                        
                        Divider(color = MiTheme.Divider, thickness = 0.5.dp)
                        
                        // 设备事件
                        ChannelSwitchItem(
                            title = "设备事件",
                            description = "新设备上线、设备上下线通知",
                            checked = deviceEventEnabled,
                            enabled = notificationEnabled,
                            onCheckedChange = { viewModel.setDeviceEventEnabled(it) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(MiDimens.itemSpacing))
            }
            
            // 通知历史
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "通知历史",
                        color = MiTheme.TextSecondary,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    
                    if (notificationHistory.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "清空",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("清空")
                        }
                    }
                }
            }
            
            if (notificationHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无通知记录",
                            color = MiTheme.TextTertiary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(notificationHistory) { item ->
                    NotificationHistoryItemCard(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * 渠道开关项
 */
@Composable
private fun ChannelSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MiDimens.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) MiTheme.TextPrimary else MiTheme.TextTertiary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = if (enabled) MiTheme.TextSecondary else MiTheme.TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Switch(
            checked = checked && enabled,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MiTheme.Primary,
                checkedTrackColor = MiTheme.Primary.copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * 通知历史记录卡片
 */
@Composable
private fun NotificationHistoryItemCard(item: NotificationHistoryItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MiDimens.cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MiTheme.CardBackground
        )
    ) {
        Column(
            modifier = Modifier.padding(MiDimens.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    color = MiTheme.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTime(item.time),
                    color = MiTheme.TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.content,
                color = MiTheme.TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.type.displayName,
                color = MiTheme.Primary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/**
 * 格式化时间
 */
private fun formatTime(time: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(time))
}
