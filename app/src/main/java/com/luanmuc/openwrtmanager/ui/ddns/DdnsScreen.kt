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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
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
                        DdnsCard(config = config)
                    }
                }
            }
        }
    }
}

@Composable
fun DdnsCard(config: DdnsConfig) {
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
        }
    }
}
