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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

/**
 * 系统管理页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemScreen(
    viewModel: SystemViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("系统日志", "进程管理")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "系统管理",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
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
            // Tab栏
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = Color(0xFFF5F7FA),
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE5E6EB))
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
                                fontWeight = if (uiState.selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Color(0xFF1677FF),
                        unselectedContentColor = Color(0xFF86909C)
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
    } else if (error != null && logs.isEmpty()) {
        ErrorState(error = error, onRetry = onRetry)
    } else {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1D2129)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(logs) { log ->
                    LogItem(log = log)
                }
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry) {
    Text(
        text = "${log.time} ${log.message}",
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFC9CDD4),
        modifier = Modifier.padding(vertical = 2.dp)
    )
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
    } else if (error != null && processes.isEmpty()) {
        ErrorState(error = error, onRetry = onRetry)
    } else if (processes.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFF2F3F5),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = Color(0xFF86909C)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无进程信息",
                color = Color(0xFF86909C),
                fontSize = 14.sp
            )
        }
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
                    tint = Color(0xFFF53F3F)
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
                        color = Color(0xFFF53F3F),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showKillDialog = null }) {
                    Text(
                        "取消",
                        color = Color(0xFF86909C)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 进程图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color(0xFFE8F3FF),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = null,
                    tint = Color(0xFF1677FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = process.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D2129)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "PID: ${process.pid} | 用户: ${process.user}",
                    fontSize = 12.sp,
                    color = Color(0xFF86909C)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    // CPU进度条
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                color = Color(0xFFF2F3F5),
                                shape = RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(process.cpu.coerceIn(0f, 100f) / 100f)
                                .height(6.dp)
                                .background(
                                    color = Color(0xFF1677FF),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${process.cpu}%",
                        fontSize = 11.sp,
                        color = Color(0xFF1677FF),
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    // 内存进度条
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                color = Color(0xFFF2F3F5),
                                shape = RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(process.memory.coerceIn(0f, 100f) / 100f)
                                .height(6.dp)
                                .background(
                                    color = Color(0xFF722ED1),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${process.memory}%",
                        fontSize = 11.sp,
                        color = Color(0xFF722ED1),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = onKill) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "结束进程",
                    tint = Color(0xFFF53F3F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = Color(0xFFFFF1F0),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFF53F3F)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            fontSize = 14.sp,
            color = Color(0xFFF53F3F)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1677FF)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                "重试",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
