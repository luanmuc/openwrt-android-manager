package com.luanmuc.openwrtmanager.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luanmuc.openwrtmanager.R
import com.luanmuc.openwrtmanager.data.model.PackageInfo
import com.luanmuc.openwrtmanager.ui.components.MiButton
import com.luanmuc.openwrtmanager.ui.components.MiButtonType
import com.luanmuc.openwrtmanager.ui.components.MiCard
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiTheme
import com.luanmuc.openwrtmanager.ui.components.MiEmptyState
import com.luanmuc.openwrtmanager.ui.components.MiFeatureIcon
import com.luanmuc.openwrtmanager.ui.components.MiTextField
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.components.OfflineBanner

/**
 * 插件页 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsScreen(
    viewModel: PluginsViewModel = viewModel(),
    onPluginClick: ((String, String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.plugins_installed), stringResource(R.string.plugins_available))
    
    Scaffold(
        topBar = {
            MiTopAppBar(
                title = stringResource(R.string.plugins_title),
                actions = {
                    IconButton(onClick = { viewModel.loadPackages() }) {
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
            
            // 搜索框
            Box(modifier = Modifier.padding(16.dp)) {
                MiTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = "搜索插件...",
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MiTheme.TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
            
            // Tab栏
            TabRow(
                selectedTabIndex = selectedTab,
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
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 15.sp,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Medium
                            )
                        },
                        selectedContentColor = MiColors.Primary,
                        unselectedContentColor = MiTheme.TextTertiary
                    )
                }
            }
            
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MiColors.Primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "加载中...",
                        color = MiTheme.TextTertiary,
                        fontSize = 14.sp
                    )
                }
            } else {
                val packages = if (selectedTab == 0) {
                    uiState.installedPackages.filter {
                        it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                                it.description.contains(uiState.searchQuery, ignoreCase = true)
                    }
                } else {
                    uiState.availablePackages.filter {
                        it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                                it.description.contains(uiState.searchQuery, ignoreCase = true)
                    }
                }
                
                if (packages.isEmpty()) {
                    MiEmptyState(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = MiTheme.TextTertiary,
                                modifier = Modifier.size(40.dp)
                            )
                        },
                        text = if (selectedTab == 0) "暂无已安装插件" else "暂无可用插件"
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(packages, key = { it.name }) { plugin ->
                            PluginCard(
                                plugin = plugin,
                                isInstalled = selectedTab == 0,
                                isLoading = uiState.actionLoading == plugin.name,
                                onInstall = { viewModel.installPackage(plugin.name) },
                                onRemove = { viewModel.removePackage(plugin.name) },
                                onClick = {
                                    if (selectedTab == 0 && onPluginClick != null) {
                                        // 构造插件的LuCI页面URL
                                        val pluginName = plugin.name.removePrefix("luci-app-")
                                        val url = "/cgi-bin/luci/admin/" + pluginName.replace("-", "/")
                                        onPluginClick(url, plugin.description.ifEmpty { plugin.name })
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PluginCard(
    plugin: PackageInfo,
    isInstalled: Boolean,
    isLoading: Boolean,
    onInstall: () -> Unit,
    onRemove: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    MiCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && isInstalled) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
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
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                },
                gradient = if (isInstalled) MiColors.GradientBlue else MiColors.GradientOrange,
                size = 44.dp,
                iconSize = 22.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.description.ifEmpty { plugin.name },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MiTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plugin.name + if (plugin.version.isNotEmpty()) " v${plugin.version}" else "",
                    fontSize = 12.sp,
                    color = MiTheme.TextTertiary
                )
                if (plugin.size > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatSize(plugin.size),
                        fontSize = 12.sp,
                        color = MiTheme.TextTertiary
                    )
                }
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MiColors.Primary,
                    strokeWidth = 2.dp
                )
            } else if (isInstalled) {
                MiButton(
                    text = "卸载",
                    onClick = onRemove,
                    type = MiButtonType.Secondary,
                    modifier = Modifier.width(70.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                )
            } else {
                MiButton(
                    text = "安装",
                    onClick = onInstall,
                    type = MiButtonType.Primary,
                    modifier = Modifier.width(70.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1048576 -> String.format("%.1f MB", bytes / 1048576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
