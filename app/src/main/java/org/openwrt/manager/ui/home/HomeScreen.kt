package org.openwrt.manager.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.openwrt.manager.R

/**
 * 首页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddRouter: () -> Unit,
    onNavigateToDevices: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    if (uiState.hasRouter) {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                    }
                }
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
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun EmptyRouterView(
    onAddRouter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Router,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.home_no_router),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "添加你的第一台 OpenWrt 路由器",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddRouter,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.home_add_router))
        }
    }
}

@Composable
fun HomeContent(
    uiState: HomeViewModel.HomeUiState,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RouterStatusCard(
            uiState = uiState,
            viewModel = viewModel
        )

        if (uiState.routerStatus != null) {
            SystemInfoCard(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        QuickActionsCard(
            onReboot = { viewModel.reboot() },
            onShutdown = { },
            onWifi = { },
            onClients = { }
        )
    }
}

@Composable
fun RouterStatusCard(
    uiState: HomeViewModel.HomeUiState,
    viewModel: HomeViewModel
) {
    val status = uiState.routerStatus
    val router = uiState.activeRouter

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = router?.name?.ifEmpty { null } ?: status?.hostname ?: "OpenWrt路由器",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = router?.address ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    val isOnline = status != null
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                if (isOnline) stringResource(R.string.home_status_online)
                                else stringResource(R.string.home_status_offline)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                if (isOnline) Icons.Default.CheckCircle
                                else Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            leadingIconContentColor = if (isOnline)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    )
                }
            }

            if (status != null) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        icon = Icons.Default.Schedule,
                        label = stringResource(R.string.home_uptime),
                        value = viewModel.formatUptime(status.uptime)
                    )
                    StatItem(
                        icon = Icons.Default.Memory,
                        label = stringResource(R.string.home_memory),
                        value = viewModel.formatBytes(status.memoryFree) + " / " +
                                viewModel.formatBytes(status.memoryTotal)
                    )
                }
            }

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SystemInfoCard(
    uiState: HomeViewModel.HomeUiState,
    viewModel: HomeViewModel
) {
    val status = uiState.routerStatus ?: return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "系统信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "主机名", value = status.hostname)
            InfoRow(label = "设备型号", value = status.model)
            InfoRow(label = "固件版本", value = status.firmware)
            InfoRow(label = "内核版本", value = status.kernel)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_load) + ": " +
                        status.loadAverage.joinToString(" / ") { "%.2f".format(it) },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuickActionsCard(
    onReboot: () -> Unit,
    onShutdown: () -> Unit,
    onWifi: () -> Unit,
    onClients: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_quick_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickActionButton(
                    icon = Icons.Default.RestartAlt,
                    label = stringResource(R.string.home_reboot),
                    onClick = onReboot
                )
                QuickActionButton(
                    icon = Icons.Default.PowerSettingsNew,
                    label = stringResource(R.string.home_shutdown),
                    onClick = onShutdown
                )
                QuickActionButton(
                    icon = Icons.Default.Wifi,
                    label = stringResource(R.string.home_wifi),
                    onClick = onWifi
                )
                QuickActionButton(
                    icon = Icons.Default.Devices,
                    label = stringResource(R.string.home_clients),
                    onClick = onClients
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledTonalIconButton(onClick = onClick) {
            Icon(icon, contentDescription = label)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
