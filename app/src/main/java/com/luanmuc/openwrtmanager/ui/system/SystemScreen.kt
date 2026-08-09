package com.luanmuc.openwrtmanager.ui.system

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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luanmuc.openwrtmanager.data.model.LogEntry
import com.luanmuc.openwrtmanager.data.model.ProcessInfo
import com.luanmuc.openwrtmanager.ui.components.MiButton
import com.luanmuc.openwrtmanager.ui.components.MiButtonType
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.LineChart
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiEmptyState
import com.luanmuc.openwrtmanager.ui.components.MiErrorState
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiLinearProgress
import com.luanmuc.openwrtmanager.ui.components.MiLoadingState
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.components.OfflineBanner

/**
 * 系统管理页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    onBack: () -> Unit = {},
    viewModel: SystemViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("系统日志", "进程管理")
    
    Scaffold(
        topBar = {
            MiTopAppBar(
                title = "系统管理",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MiTheme.TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.selectedTab == 0) {
                                viewModel.loadLogs()
                            } else {
                                viewModel.loadProcesses()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MiTheme.TextSecondary
                        )
                    }
                }
            )
        },
        containerColor = MiTheme.Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
                        // 离线提示条
            OfflineBanner(isOffline = !viewModel.isNetworkAvailable)
            
            // 系统状态概览
            SystemStatusOverview(
                cpuUsage = uiState.cpuUsage,
                memoryUsage = uiState.memoryUsage,
                cpuHistory = uiState.cpuHistory,
                memoryHistory = uiState.memoryHistory
            )
            
            // Tab栏
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MiTheme.Background,
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(MiTheme.Divider)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Text(
                                title,
                                fontSize = 15.sp,
                                fontWeight = if (uiState.selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        selectedContentColor = MiColors.Primary,
                        unselectedContentColor = MiTheme.TextTertiary
                    )
                }
            }
            
            if (uiState.selectedTab == 0) {
                LogsContent(
                    logs = uiState.logs,
                    isLoading = uiState.isLoadingLogs,
                    error = uiState.error,
                    onRetry = { viewModel.loadLogs() }
                )
            } else {
                ProcessesContent(
                    processes = uiState.processes,
                    isLoading = uiState.isLoadingProcesses,
                    error = uiState.error,
                    onRetry = { viewModel.loadProcesses() },
                    onKillProcess = { viewModel.killProcess(it) }
                )
            }
        }
    }
}

@Composable
fun LogsContent(
    logs: List<LogEntry>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit
) {
    if (isLoading) {
        MiLoadingState()
    } else if (error != null && logs.isEmpty()) {
        MiErrorState(message = error, onRetry = onRetry)
    } else {
        MiCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            backgroundColor = Color(0xFF1D2129)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs) { log ->
                    Text(
                        text = "${log.time} ${log.message}",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFC9CDD4),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProcessesContent(
    processes: List<ProcessInfo>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onKillProcess: (Int) -> Unit
) {
    var showKillDialog by remember { mutableStateOf<ProcessInfo?>(null) }
    
    if (isLoading) {
        MiLoadingState()
    } else if (error != null && processes.isEmpty()) {
        MiErrorState(message = error, onRetry = onRetry)
    } else if (processes.isEmpty()) {
        MiEmptyState(
            icon = {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = MiTheme.TextTertiary,
                    modifier = Modifier.size(40.dp)
                )
            },
            text = "暂无进程信息"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(processes, key = { it.pid }) { process ->
                ProcessCard(
                    process = process,
                    onKill = { showKillDialog = process }
                )
            }
        }
    }
    
    showKillDialog?.let { process ->
        AlertDialog(
            onDismissRequest = { showKillDialog = null },
            icon = {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = null,
                    tint = MiColors.Error
                )
            },
            title = {
                Text(
                    "结束进程",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("确定要结束进程「${process.name}」(PID: ${process.pid}) 吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onKillProcess(process.pid)
                        showKillDialog = null
                    }
                ) {
                    Text(
                        "结束",
                        color = MiColors.Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showKillDialog = null }) {
                    Text(
                        "取消",
                        color = MiTheme.TextTertiary
                    )
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun ProcessCard(
    process: ProcessInfo,
    onKill: () -> Unit
) {
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
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                gradient = MiColors.GradientBlue,
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = process.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PID: ${process.pid} | 用户: ${process.user}",
                    fontSize = 12.sp,
                    color = MiTheme.TextTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiLinearProgress(
                        progress = process.cpu / 100f,
                        color = MiColors.Primary,
                        modifier = Modifier.weight(1f),
                        height = 6.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${process.cpu}%",
                        fontSize = 11.sp,
                        color = MiColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiLinearProgress(
                        progress = process.memory / 100f,
                        color = MiColors.Purple,
                        modifier = Modifier.weight(1f),
                        height = 6.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${process.memory}%",
                        fontSize = 11.sp,
                        color = MiColors.Purple,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            MiButton(
                text = "结束",
                onClick = onKill,
                type = MiButtonType.Secondary,
                modifier = Modifier.width(60.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}


/**
 * 系统状态概览 - CPU和内存使用率
 */
@Composable
fun SystemStatusOverview(
    cpuUsage: Float,
    memoryUsage: Float,
    cpuHistory: List<Float>,
    memoryHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CPU使用率
            MiCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MiColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CPU",
                            fontSize = 13.sp,
                            color = MiTheme.TextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${cpuUsage.toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiTheme.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LineChart(
                        data = cpuHistory,
                        color = MiColors.Primary,
                        showGrid = false,
                        
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            }
            
            // 内存使用率
            MiCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MiColors.Success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "内存",
                            fontSize = 13.sp,
                            color = MiTheme.TextSecondary
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${memoryUsage.toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MiTheme.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LineChart(
                        data = memoryHistory,
                        color = MiColors.Success,
                        showGrid = false,
                        
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    )
                }
            }
        }
    }
}
