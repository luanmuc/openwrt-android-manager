package org.openwrt.manager.ui.plugins

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.openwrt.manager.R

/**
 * 插件页 ViewModel
 */
class PluginsViewModel : androidx.lifecycle.ViewModel() {
    // 占位数据，后续接入真实API
    val installedPlugins = listOf(
        PluginItem("luci-i18n-base-zh-cn", "中文语言包", "23.05.0", true),
        PluginItem("luci-app-firewall", "防火墙", "23.05.0", true),
        PluginItem("luci-app-opkg", "软件包管理", "23.05.0", true),
        PluginItem("luci-app-upnp", "UPnP", "23.05.0", true)
    )

    val availablePlugins = listOf(
        PluginItem("luci-app-ddns", "动态DNS", "23.05.0", false),
        PluginItem("luci-app-wol", "网络唤醒", "23.05.0", false),
        PluginItem("luci-app-sqm", "SQM QoS", "23.05.0", false),
        PluginItem("luci-app-openvpn", "OpenVPN", "23.05.0", false),
        PluginItem("luci-app-wireguard", "WireGuard", "23.05.0", false),
        PluginItem("luci-app-mwan3", "多WAN负载均衡", "23.05.0", false)
    )
}

data class PluginItem(
    val name: String,
    val description: String,
    val version: String,
    val installed: Boolean
)

/**
 * 插件页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginsScreen(
    viewModel: PluginsViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.plugins_installed), stringResource(R.string.plugins_available))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.plugins_title)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val plugins = if (selectedTab == 0) viewModel.installedPlugins
            else viewModel.availablePlugins

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(plugins) { plugin ->
                    PluginCard(plugin)
                }
            }
        }
    }
}

@Composable
fun PluginCard(plugin: PluginItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Extension,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plugin.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = plugin.name + " v" + plugin.version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (plugin.installed) {
                FilledTonalButton(onClick = { /* 卸载 */ }) {
                    Text("卸载")
                }
            } else {
                Button(onClick = { /* 安装 */ }) {
                    Text("安装")
                }
            }
        }
    }
}
