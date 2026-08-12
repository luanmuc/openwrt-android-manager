package com.luanmuc.openwrtmanager.ui.ddns

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luanmuc.openwrtmanager.data.model.DdnsConfig
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiTheme
import com.luanmuc.openwrtmanager.ui.components.MiEmptyState
import com.luanmuc.openwrtmanager.ui.components.MiErrorState
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiLoadingState
import com.luanmuc.openwrtmanager.ui.components.MiTag
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.components.OfflineBanner

/**
 * DDNS页面 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DdnsScreen(
    onBack: () -> Unit = {},
    viewModel: DdnsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<DdnsConfig?>(null) }
    var showDeleteDialog by remember { mutableStateOf<DdnsConfig?>(null) }
    var editingConfig by remember { mutableStateOf(DdnsConfig()) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.success) {
        uiState.success?.let { success ->
            android.widget.Toast.makeText(context, success, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        topBar = {
            MiTopAppBar(
                title = "DDNS",
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
                    IconButton(onClick = { viewModel.loadDdnsConfig() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = MiTheme.TextSecondary
                        )
                    }
                    IconButton(onClick = {
                        editingConfig = DdnsConfig()
                        showAddDialog = true
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加",
                            tint = MiColors.Primary
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
            OfflineBanner(isOffline = !viewModel.isNetworkAvailable)

            if (uiState.isLoading) {
                MiLoadingState()
            } else if (uiState.error != null && uiState.ddnsConfigs.isEmpty()) {
                MiErrorState(
                    message = uiState.error ?: "加载失败",
                    onRetry = { viewModel.loadDdnsConfig() }
                )
            } else if (uiState.ddnsConfigs.isEmpty()) {
                MiEmptyState(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = MiTheme.TextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                    },
                    text = "暂无DDNS配置"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.ddnsConfigs, key = { it.name }) { config ->
                        DdnsCard(
                            config = config,
                            onEdit = {
                                editingConfig = config
                                showEditDialog = config
                            },
                            onDelete = { showDeleteDialog = config }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        DdnsEditDialog(
            title = "添加DDNS",
            config = editingConfig,
            onConfigChange = { editingConfig = it },
            onConfirm = {
                viewModel.addDdns(it)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showEditDialog != null) {
        DdnsEditDialog(
            title = "编辑DDNS",
            config = editingConfig,
            onConfigChange = { editingConfig = it },
            onConfirm = {
                val editName = showEditDialog?.name ?: return@DdnsEditDialog
                viewModel.updateDdns(editName, it)
                showEditDialog = null
            },
            onDismiss = { showEditDialog = null }
        )
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MiColors.Error)
            },
            title = { Text("删除DDNS配置", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除DDNS配置「${showDeleteDialog!!.name}」吗？") },
            confirmButton = {
                TextButton(onClick = {
                    val deleteName = showDeleteDialog?.name ?: return@DdnsEditDialog
                    viewModel.deleteDdns(deleteName)
                    showDeleteDialog = null
                }) {
                    Text("删除", color = MiColors.Error, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消", color = MiTheme.TextTertiary)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun DdnsCard(
    config: DdnsConfig,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
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
                        imageVector = Icons.Default.Dns,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                },
                gradient = MiColors.GradientCyan,
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = config.domain,
                    fontSize = 12.sp,
                    color = MiTheme.TextTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MiTag(
                        text = config.service,
                        backgroundColor = MiColors.Primary.copy(alpha = 0.1f),
                        textColor = MiColors.Primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (config.enabled) {
                        MiTag(
                            text = "已启用",
                            backgroundColor = MiColors.Success.copy(alpha = 0.1f),
                            textColor = MiColors.Success
                        )
                    } else {
                        MiTag(
                            text = "已禁用",
                            backgroundColor = MiTheme.TextTertiary.copy(alpha = 0.1f),
                            textColor = MiTheme.TextTertiary
                        )
                    }
                }
            }
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MiColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MiColors.Error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DdnsEditDialog(
    title: String,
    config: DdnsConfig,
    onConfigChange: (DdnsConfig) -> Unit,
    onConfirm: (DdnsConfig) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = config.service,
                    onValueChange = { onConfigChange(config.copy(service = it)) },
                    label = { Text("服务商") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.domain,
                    onValueChange = { onConfigChange(config.copy(domain = it)) },
                    label = { Text("域名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.username,
                    onValueChange = { onConfigChange(config.copy(username = it)) },
                    label = { Text("用户名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.password,
                    onValueChange = { onConfigChange(config.copy(password = it)) },
                    label = { Text("密码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = config.interfaceName,
                    onValueChange = { onConfigChange(config.copy(interfaceName = it)) },
                    label = { Text("网络接口") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(config) }) {
                Text("确定", color = MiColors.Primary, fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MiTheme.TextTertiary)
            }
        },
        containerColor = Color.White
    )
}
