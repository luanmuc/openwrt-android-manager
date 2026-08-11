package com.luanmuc.openwrtmanager.ui.advanced

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Code
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiTheme
import com.luanmuc.openwrtmanager.ui.components.MiDivider
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiListItem
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.components.OfflineBanner

/**
 * 高级功能页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedScreen(
    onBack: () -> Unit = {},
    onNavigateToSystem: () -> Unit = {},
    onNavigateToDiagnostic: () -> Unit = {},
    onOpenWebView: (String, String) -> Unit = { _, _ -> },
    viewModel: AdvancedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRebootDialog by remember { mutableStateOf(false) }
    var showShutdownDialog by remember { mutableStateOf(false) }
    
    // 错误提示
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    
    // 成功提示
    LaunchedEffect(uiState.actionSuccess) {
        if (uiState.actionSuccess) {
            android.widget.Toast.makeText(context, "操作成功", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            MiTopAppBar(
                title = "高级功能",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MiTheme.TextPrimary
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
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
                        // 离线提示条
            OfflineBanner(isOffline = !viewModel.isNetworkAvailable)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 系统操作
            SectionTitle(title = "系统操作")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "重启路由器",
                        subtitle = "重新启动设备",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MiColors.Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showRebootDialog = true }
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "关闭路由器",
                        subtitle = "关闭设备电源",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = MiColors.Error,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = { showShutdownDialog = true }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 系统信息
            SectionTitle(title = "系统信息")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "系统信息",
                        subtitle = "固件版本、内核版本",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MiColors.Success,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = onNavigateToSystem
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "存储状态",
                        subtitle = "磁盘使用情况",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MiColors.Warning,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = onNavigateToSystem
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "进程管理",
                        subtitle = "查看运行中的进程",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = MiColors.Purple,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            onOpenWebView("/cgi-bin/luci/admin/status/processes", "进程管理")
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 诊断工具
            SectionTitle(title = "诊断工具")
            MiCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    MiListItem(
                        title = "网络诊断",
                        subtitle = "Ping、Traceroute",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MiColors.Cyan,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = onNavigateToDiagnostic
                    )
                    MiDivider(indent = 60.dp)
                    MiListItem(
                        title = "终端命令",
                        subtitle = "在浏览器中使用命令行（需安装ttyd插件）",
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MiColors.Orange,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        onClick = {
                            onOpenWebView("/cgi-bin/luci/admin/services/ttyd", "终端命令")
                        }
                    )
                }
            }
        }
    }
    
    // 重启确认对话框
    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            icon = {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MiColors.Primary
                )
            },
            title = {
                Text(
                    "重启路由器",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("确定要重启路由器吗？重启期间网络将中断。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reboot()
                        showRebootDialog = false
                    }
                ) {
                    Text(
                        "重启",
                        color = MiColors.Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text(
                        "取消",
                        color = MiTheme.TextTertiary
                    )
                }
            },
            containerColor = Color.White
        )
    }
    
    // 关机确认对话框
    if (showShutdownDialog) {
        AlertDialog(
            onDismissRequest = { showShutdownDialog = false },
            icon = {
                Icon(
                    Icons.Default.PowerSettingsNew,
                    contentDescription = null,
                    tint = MiColors.Error
                )
            },
            title = {
                Text(
                    "关闭路由器",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("确定要关闭路由器吗？关闭后需要手动开机。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.shutdown()
                        showShutdownDialog = false
                    }
                ) {
                    Text(
                        "关闭",
                        color = MiColors.Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showShutdownDialog = false }) {
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
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = MiTheme.TextSecondary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
