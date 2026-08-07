package org.openwrt.manager.ui.plugins

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.tabIndicatorOffset
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import org.openwrt.manager.R
import org.openwrt.manager.data.model.PackageInfo

/**
 * 插件页 - 小米路由器风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsScreen(
    viewModel: PluginsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.plugins_installed), stringResource(R.string.plugins_available))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.plugins_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPackages() }) {
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
            // 搜索框
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {
                    Text(
                        "搜索插件...",
                        color = Color(0xFF86909C)
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF86909C)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1677FF),
                    unfocusedBorderColor = Color(0xFFE5E6EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Tab栏
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF5F7FA),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .width(40.dp),
                        color = Color(0xFF1677FF),
                        height = 3.dp
                    )
                },
                divider = {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFE5E6EB))
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
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Color(0xFF1677FF),
                        unselectedContentColor = Color(0xFF86909C)
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color(0xFFF2F3F5),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color(0xFF86909C)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "暂无已安装插件" else "暂无可用插件",
                            color = Color(0xFF86909C),
                            fontSize = 14.sp
                        )
                    }
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
                                onRemove = { viewModel.removePackage(plugin.name) }
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
    onRemove: () -> Unit
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
            // 插件图标
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = if (isInstalled) Color(0xFFE8F3FF) else Color(0xFFF2F3F5),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null,
                    tint = if (isInstalled) Color(0xFF1677FF) else Color(0xFF86909C),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.description.ifEmpty { plugin.name },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1D2129)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plugin.name + if (plugin.version.isNotEmpty()) " v${plugin.version}" else "",
                    fontSize = 12.sp,
                    color = Color(0xFF86909C)
                )
                if (plugin.size > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatSize(plugin.size),
                        fontSize = 12.sp,
                        color = Color(0xFF86909C)
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFF1677FF),
                    strokeWidth = 2.dp
                )
            } else if (isInstalled) {
                FilledTonalButton(
                    onClick = onRemove,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFF2F3F5),
                        contentColor = Color(0xFF4E5969)
                    )
                ) {
                    Text(
                        "卸载",
                        fontSize = 13.sp
                    )
                }
            } else {
                Button(
                    onClick = onInstall,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1677FF)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        "安装",
                        fontSize = 13.sp
                    )
                }
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
