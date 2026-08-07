package org.openwrt.manager.ui.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * 网络设置页面
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
                title = { Text("网络设置") },
                actions = {
                    IconButton(onClick = { viewModel.loadNetworkConfig() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
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
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("加载中...")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> LanSettings(viewModel = viewModel)
                        1 -> WanSettings(viewModel = viewModel)
                        2 -> DhcpSettings(viewModel = viewModel)
                    }

                    // 保存按钮
                    Button(
                        onClick = {
                            when (selectedTab) {
                                0 -> viewModel.saveLanConfig()
                                1 -> viewModel.saveWanConfig()
                                2 -> viewModel.saveLanConfig()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("保存配置")
                    }

                    // 成功提示
                    uiState.success?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // 错误提示
                    uiState.error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanSettings(viewModel: NetworkViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "LAN 口设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = uiState.lanIp,
                onValueChange = { viewModel.updateLanIp(it) },
                label = { Text("IP 地址") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.lanNetmask,
                onValueChange = { viewModel.updateLanNetmask(it) },
                label = { Text("子网掩码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
fun WanSettings(viewModel: NetworkViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WAN 口设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 协议选择
            Text(
                text = "连接协议",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.wanProto == "dhcp",
                    onClick = { viewModel.updateWanProto("dhcp") },
                    label = { Text("DHCP") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.wanProto == "static",
                    onClick = { viewModel.updateWanProto("static") },
                    label = { Text("静态IP") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = uiState.wanProto == "pppoe",
                    onClick = { viewModel.updateWanProto("pppoe") },
                    label = { Text("PPPoE") },
                    modifier = Modifier.weight(1f)
                )
            }

            // 静态IP设置
            if (uiState.wanProto == "static") {
                OutlinedTextField(
                    value = uiState.wanIp,
                    onValueChange = { viewModel.updateWanIp(it) },
                    label = { Text("IP 地址") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.wanNetmask,
                    onValueChange = { viewModel.updateWanNetmask(it) },
                    label = { Text("子网掩码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.wanGateway,
                    onValueChange = { viewModel.updateWanGateway(it) },
                    label = { Text("网关") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.wanDns,
                    onValueChange = { viewModel.updateWanDns(it) },
                    label = { Text("DNS 服务器") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // PPPoE设置
            if (uiState.wanProto == "pppoe") {
                OutlinedTextField(
                    value = uiState.wanUsername,
                    onValueChange = { viewModel.updateWanUsername(it) },
                    label = { Text("宽带账号") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.wanPassword,
                    onValueChange = { viewModel.updateWanPassword(it) },
                    label = { Text("宽带密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun DhcpSettings(viewModel: NetworkViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "DHCP 服务器",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "启用 DHCP 服务器",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.lanDhcpEnabled,
                    onCheckedChange = { viewModel.updateLanDhcpEnabled(it) }
                )
            }

            if (uiState.lanDhcpEnabled) {
                OutlinedTextField(
                    value = uiState.lanDhcpStart,
                    onValueChange = { viewModel.updateLanDhcpStart(it) },
                    label = { Text("地址池起始") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.lanDhcpLimit,
                    onValueChange = { viewModel.updateLanDhcpLimit(it) },
                    label = { Text("地址池数量") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.lanDhcpLease,
                    onValueChange = { viewModel.updateLanDhcpLease(it) },
                    label = { Text("租期") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("如 12h, 7d") }
                )
            }
        }
    }
}
