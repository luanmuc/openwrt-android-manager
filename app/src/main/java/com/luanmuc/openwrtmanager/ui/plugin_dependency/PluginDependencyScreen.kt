package com.luanmuc.openwrtmanager.ui.plugin_dependency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar
import com.luanmuc.openwrtmanager.ui.components.PluginDependencyScreen as PluginDependencyContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginDependencyScreen(
    onBack: () -> Unit,
    viewModel: PluginDependencyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MiTopAppBar(
                title = "插件依赖检测",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(16.dp)
                )
            } else {
                PluginDependencyContent(
                    pluginStatuses = uiState.pluginStatuses,
                    onInstall = { status, onProgress ->
                        val index = uiState.pluginStatuses.indexOf(status)
                        if (index >= 0) {
                            viewModel.installPlugin(index)
                        }
                    },
                    onRefresh = { viewModel.checkDependencies() }
                )
            }
        }
    }
}
