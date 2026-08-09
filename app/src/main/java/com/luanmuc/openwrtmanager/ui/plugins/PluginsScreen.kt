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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.luanmuc.openwrtmanager.ui.components.MiLinearProgress
import com.luanmuc.openwrtmanager.ui.components.MiListItem
import com.luanmuc.openwrtmanager.ui.components.MiTextField
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.components.OfflineBanner
import kotlinx.coroutines.launch

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
    val tabs = listOf("已安装", "可用", "全部")
    var showSortMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MiTopAppBar(
                title = stringResource(R.string.plugins_title),
                actions = {
                    IconButton(onClick = { showSortMenu = !showSortMenu }) {
                        Icon(
                            Icons.Default.Sort,
                            contentDescription = "排序",
                            tint = MiTheme.TextSecondary
                        )
                    }
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
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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

            // 分类筛选栏
            CategoryFilterBar(
                selectedCategory = uiState.selectedCategory,
                categories = viewModel.getAvailableCategories(),
                onCategorySelected = { viewModel.setCategory(it) }
            )

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

            // 排序方式选择（简单实现）
            if (showSortMenu) {
                MiCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            "排序方式",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiTheme.TextPrimary
                        )
                        PluginsViewModel.SortType.values().forEach { sortType ->
                            MiListItem(
                                title = sortType.displayName,
                                onClick = {
                                    viewModel.setSortType(sortType)
                                    showSortMenu = false
                                },
                                trailing = {
                                    if (uiState.sortType == sortType) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MiColors.Primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
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
                val packages = when (selectedTab) {
                    0 -> viewModel.getFilteredInstalled()
                    1 -> viewModel.getFilteredAvailable()
                    2 -> (viewModel.getFilteredInstalled() + viewModel.getFilteredAvailable()).distinctBy { it.name }
                    else -> emptyList()
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
                        text = when (selectedTab) {
                            0 -> "暂无已安装插件"
                            1 -> "暂无可用插件"
                            else -> "暂无插件"
                        }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(packages, key = { it.name }) { plugin ->
                            val isInstalled = uiState.installedPackages.any { it.name == plugin.name }
                            PluginCard(
                                plugin = plugin,
                                isInstalled = isInstalled,
                                isLoading = uiState.actionLoading == plugin.name,
                                installProgress = uiState.installProgress[plugin.name] ?: 0,
                                onInstall = { viewModel.installPackage(plugin.name) },
                                onRemove = { viewModel.removePackage(plugin.name) },
                                onClick = {
                                    viewModel.showPackageDetail(plugin)
                                    scope.launch { sheetState.show() }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 插件详情页
        if (uiState.showDetail && uiState.selectedPackage != null) {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = {
                    viewModel.hidePackageDetail()
                },
                containerColor = MiTheme.CardBackground
            ) {
                PluginDetailSheet(
                    plugin = uiState.selectedPackage!!,
                    isInstalled = uiState.installedPackages.any { it.name == uiState.selectedPackage!!.name },
                    isLoading = uiState.actionLoading == uiState.selectedPackage!!.name,
                    onInstall = { viewModel.installPackage(uiState.selectedPackage!!.name) },
                    onRemove = { viewModel.removePackage(uiState.selectedPackage!!.name) },
                    onOpen = {
                        val pluginName = uiState.selectedPackage!!.name.removePrefix("luci-app-")
                        val url = "/cgi-bin/luci/admin/" + pluginName.replace("-", "/")
                        onPluginClick?.invoke(url, uiState.selectedPackage!!.description.ifEmpty { uiState.selectedPackage!!.name })
                        scope.launch { sheetState.hide() }
                    }
                )
            }
        }
    }
}

/**
 * 分类筛选栏
 */
@Composable
fun CategoryFilterBar(
    selectedCategory: PluginsViewModel.PluginCategory,
    categories: List<PluginsViewModel.PluginCategory>,
    onCategorySelected: (PluginsViewModel.PluginCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MiColors.Primary else MiTheme.CardBackground
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = category.displayName,
                    fontSize = 13.sp,
                    color = if (isSelected) Color.White else MiTheme.TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PluginCard(
    plugin: PackageInfo,
    isInstalled: Boolean,
    isLoading: Boolean,
    installProgress: Int = 0,
    onInstall: () -> Unit,
    onRemove: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    MiCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
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
                    if (plugin.category.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "分类: ${plugin.category}",
                            fontSize = 11.sp,
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

            // 安装进度条
            if (isLoading && installProgress > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                MiLinearProgress(
                    progress = installProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "安装中... $installProgress%",
                    fontSize = 11.sp,
                    color = MiTheme.TextTertiary
                )
            }

            // 大小显示
            if (plugin.size > 0 && !isLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "大小: ${formatSize(plugin.size)}",
                    fontSize = 11.sp,
                    color = MiTheme.TextTertiary
                )
            }
        }
    }
}

/**
 * 插件详情页
 */
@Composable
fun PluginDetailSheet(
    plugin: PackageInfo,
    isInstalled: Boolean,
    isLoading: Boolean,
    onInstall: () -> Unit,
    onRemove: () -> Unit,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // 标题行
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiFeatureIcon(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                },
                gradient = if (isInstalled) MiColors.GradientBlue else MiColors.GradientOrange,
                size = 56.dp,
                iconSize = 28.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.description.ifEmpty { plugin.name },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MiTheme.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plugin.name,
                    fontSize = 13.sp,
                    color = MiTheme.TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 基本信息
        MiCard {
            Column {
                DetailItem("版本", if (plugin.version.isNotEmpty()) plugin.version else "未知")
                DetailItem("大小", if (plugin.size > 0) formatSize(plugin.size) else "未知")
                DetailItem("分类", if (plugin.category.isNotEmpty()) plugin.category else "其他")
                DetailItem("状态", if (isInstalled) "已安装" else "未安装")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 插件描述
        if (plugin.description.isNotEmpty()) {
            Text(
                text = "插件介绍",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            MiCard {
                Text(
                    text = plugin.description,
                    modifier = Modifier.padding(14.dp),
                    fontSize = 14.sp,
                    color = MiTheme.TextSecondary,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 依赖信息
        if (plugin.depends.isNotEmpty()) {
            Text(
                text = "依赖包",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiTheme.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            MiCard {
                Column(modifier = Modifier.padding(14.dp)) {
                    plugin.depends.forEach { dep ->
                        Text(
                            text = "• $dep",
                            fontSize = 13.sp,
                            color = MiTheme.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 操作按钮
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = MiColors.Primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "处理中...",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                color = MiTheme.TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        } else if (isInstalled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MiButton(
                    text = "打开插件",
                    onClick = onOpen,
                    type = MiButtonType.Primary,
                    modifier = Modifier.weight(1f)
                )
                MiButton(
                    text = "卸载",
                    onClick = onRemove,
                    type = MiButtonType.Secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            MiButton(
                text = "安装插件",
                onClick = onInstall,
                type = MiButtonType.Primary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * 详情项
 */
@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MiTheme.TextSecondary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MiTheme.TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1048576 -> String.format("%.1f MB", bytes / 1048576.0)
        bytes >= 1024 -> String.format("%.1f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
