package com.luanmuc.openwrtmanager.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luanmuc.openwrtmanager.data.model.DeviceInfo
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiEmptyState
import com.luanmuc.openwrtmanager.ui.components.MiErrorState
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiLoadingState
import com.luanmuc.openwrtmanager.ui.components.MiTag
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar

/**
 * 在线设备页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineDevicesScreen(
    onBack: () -> Unit,
    viewModel: OnlineDevicesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            MiTopAppBar(
                title = "在线设备",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MiColors.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadDevices() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MiColors.TextSecondary
                        )
                    }
                }
            )
        },
        containerColor = MiColors.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 统计卡片
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        title = "在线设备",
                        value = "${uiState.devices.size}",
                        color = MiColors.Primary
                    )
                    StatItem(
                        title = "有线连接",
                        value = "${uiState.devices.count { !it.interfaceName.contains("wlan", ignoreCase = true) }}",
                        color = MiColors.Success
                    )
                    StatItem(
                        title = "无线连接",
                        value = "${uiState.devices.count { it.interfaceName.contains("wlan", ignoreCase = true) }}",
                        color = MiColors.Warning
                    )
                }
            }
            
            if (uiState.isLoading) {
                MiLoadingState()
            } else if (uiState.error != null && uiState.devices.isEmpty()) {
                MiErrorState(
                    message = uiState.error ?: "加载失败",
                    onRetry = { viewModel.loadDevices() }
                )
            } else if (uiState.devices.isEmpty()) {
                MiEmptyState(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                            tint = MiColors.TextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    text = "暂无在线设备"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.devices, key = { it.mac }) { device ->
                        DeviceCard(device = device)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = MiColors.TextTertiary
        )
    }
}

@Composable
fun DeviceCard(device: DeviceInfo) {
    MiCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiFeatureIcon(
                icon = {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                gradient = if (device.interfaceName.contains("wlan", ignoreCase = true)) MiColors.GradientOrange else MiColors.GradientBlue,
                size = 44.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.hostname.ifEmpty { device.ip },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = device.ip,
                    fontSize = 12.sp,
                    color = MiColors.TextTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isWifi = device.interfaceName.contains("wlan", ignoreCase = true)
                    MiTag(
                        text = if (isWifi) "WiFi" else "有线",
                        backgroundColor = if (isWifi) MiColors.Warning.copy(alpha = 0.1f) else MiColors.Primary.copy(alpha = 0.1f),
                        textColor = if (isWifi) MiColors.Warning else MiColors.Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = device.mac,
                        fontSize = 11.sp,
                        color = MiColors.TextTertiary
                    )
                }
            }
        }
    }
}
