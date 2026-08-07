package org.openwrt.manager.ui.network

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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 网络设置页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LAN 设置", "WAN 设置", "DHCP 管理")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "网络设置",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadNetworkConfig() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = Color(0xFF1677FF)
                        )
                    }
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
        ) {
            // Tab 切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = {
                            Text(
                                title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = if (selectedTab == index) {
                            androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1677FF),
                                selectedLabelColor = Color.White
                            )
                        } else {
                            androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF86909C)
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1677FF))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "加载中...",
                        color = Color(0xFF86909C),
                        fontSize = 14.sp
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (selectedTab) {
                        0 -> LanSettings(viewModel = viewModel)
                        1 -> WanSettings(viewModel = viewModel)
                        2 -> DhcpSettings(viewModel = viewModel)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 保存按钮
                    Button(
                        onClick = {
                            when (selectedTab) {
                                0 -> viewModel.saveLanConfig()
                                1 -> viewModel.saveWanConfig()
                                2 -> viewModel.saveLanConfig()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !uiState.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1677FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            "保存配置",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 成功提示
                    uiState.success?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8FFEA)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                color = Color(0xFF00B42A),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // 错误提示
                    uiState.error?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF1F0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                color = Color(0xFFF53F3F),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LanSettings(viewModel: NetworkViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "LAN 口设置",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D2129)
            )

            OutlinedTextField(
                value = uiState.lanIp,
                onValueChange = { viewModel.updateLanIp(it) },
                label = {
                    Text(
                        "IP 地址",
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1677FF),
                    unfocusedBorderColor = Color(0xFFE5E6EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            OutlinedTextField(
                value = uiState.lanNetmask,
                onValueChange = { viewModel.updateLanNetmask(it) },
                label = {
                    Text(
                        "子网掩码",
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1677FF),
                    unfocusedBorderColor = Color(0xFFE5E6EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
        }
    }
}

@Composable
fun WanSettings(viewModel: NetworkViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "WAN 口设置",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D2129)
            )

            // 协议选择
            Text(
                text = "连接协议",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4E5969)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.wanProto == "dhcp",
                    onClick = { viewModel.updateWanProto("dhcp") },
                    label = {
                        Text(
                            "DHCP",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.wanProto == "dhcp") {
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1677FF),
                            selectedLabelColor = Color.White
                        )
                    } else {
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF2F3F5),
                            labelColor = Color(0xFF86909C)
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = uiState.wanProto == "static",
                    onClick = { viewModel.updateWanProto("static") },
                    label = {
                        Text(
                            "静态IP",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.wanProto == "static") {
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1677FF),
                            selectedLabelColor = Color.White
                        )
                    } else {
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF2F3F5),
                            labelColor = Color(0xFF86909C)
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = uiState.wanProto == "pppoe",
                    onClick = { viewModel.updateWanProto("pppoe") },
                    label = {
                        Text(
                            "PPPoE",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = if (uiState.wanProto == "pppoe") {
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1677FF),
                            selectedLabelColor = Color.White
                        )
                    } else {
                        androidx.compose.material3.FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFF2F3F5),
                            labelColor = Color(0xFF86909C)
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // 静态IP设置
            if (uiState.wanProto == "static") {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = uiState.wanIp,
                    onValueChange = { viewModel.updateWanIp(it) },
                    label = {
                        Text(
                            "IP 地址",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = uiState.wanNetmask,
                    onValueChange = { viewModel.updateWanNetmask(it) },
                    label = {
                        Text(
                            "子网掩码",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = uiState.wanGateway,
                    onValueChange = { viewModel.updateWanGateway(it) },
                    label = {
                        Text(
                            "网关",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = uiState.wanDns,
                    onValueChange = { viewModel.updateWanDns(it) },
                    label = {
                        Text(
                            "DNS 服务器",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // PPPoE设置
            if (uiState.wanProto == "pppoe") {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = uiState.wanUsername,
                    onValueChange = { viewModel.updateWanUsername(it) },
                    label = {
                        Text(
                            "宽带账号",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = uiState.wanPassword,
                    onValueChange = { viewModel.updateWanPassword(it) },
                    label = {
                        Text(
                            "宽带密码",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DhcpSettings(viewModel: NetworkViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "DHCP 服务器",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1D2129)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "启用 DHCP 服务器",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1D2129)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "自动分配IP地址给连接的设备",
                        fontSize = 12.sp,
                        color = Color(0xFF86909C)
                    )
                }
                Switch(
                    checked = uiState.lanDhcpEnabled,
                    onCheckedChange = { viewModel.updateLanDhcpEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1677FF),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFC9CDD4)
                    )
                )
            }

            if (uiState.lanDhcpEnabled) {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = uiState.lanDhcpStart,
                    onValueChange = { viewModel.updateLanDhcpStart(it) },
                    label = {
                        Text(
                            "地址池起始",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = uiState.lanDhcpLimit,
                    onValueChange = { viewModel.updateLanDhcpLimit(it) },
                    label = {
                        Text(
                            "地址池数量",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = uiState.lanDhcpLease,
                    onValueChange = { viewModel.updateLanDhcpLease(it) },
                    label = {
                        Text(
                            "租期",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            "如 12h, 7d",
                            color = Color(0xFF86909C)
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE5E6EB),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        }
    }
}
