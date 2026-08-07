package com.luanmuc.openwrtmanager.ui.webview

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.luanmuc.openwrtmanager.ui.components.MiColors
import com.luanmuc.openwrtmanager.ui.components.MiTopAppBar

/**
 * WebView插件容器页面
 * 用于显示LuCI Web界面的插件
 */
@Composable
fun WebViewPluginScreen(
    title: String,
    url: String,
    onBack: () -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }

    Scaffold(
        topBar = {
            MiTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* 刷新 */ }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 加载进度条
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MiColors.Primary,
                    trackColor = Color.Transparent
                )
            }

            // WebView
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MiColors.Background)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            // WebView设置
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }

                            // WebView客户端
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    // 注入移动端优化CSS
                                    view?.loadUrl("javascript:injectMobileCss()")
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    progress = newProgress / 100f
                                }
                            }

                            // 加载URL
                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        // 更新WebView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 加载状态
                if (isLoading && progress < 0.1f) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.Center),
                        color = MiColors.Primary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

/**
 * 移动端优化CSS
 * 注入到WebView中，优化LuCI在移动端的显示
 */
const val MOBILE_CSS = """
function injectMobileCss() {
    const css = `
        /* 移动端优化样式 */
        body {
            font-size: 14px !important;
            -webkit-text-size-adjust: 100%;
        }
        
        /* 顶部导航优化 */
        .main > .topbar {
            position: sticky;
            top: 0;
            z-index: 1000;
        }
        
        /* 菜单优化 */
        .main > .sidemenu {
            display: none;
        }
        
        .main > .maincontent {
            margin-left: 0 !important;
        }
        
        /* 卡片优化 */
        .cbi-section {
            margin: 8px !important;
            border-radius: 12px !important;
            overflow: hidden;
        }
        
        /* 表格优化 */
        table {
            font-size: 12px !important;
        }
        
        /* 按钮优化 */
        .btn, button, input[type="button"], input[type="submit"] {
            padding: 8px 16px !important;
            border-radius: 8px !important;
            font-size: 14px !important;
        }
        
        /* 输入框优化 */
        input[type="text"], input[type="password"], select, textarea {
            padding: 8px !important;
            border-radius: 8px !important;
            font-size: 14px !important;
        }
        
        /* 隐藏PC端元素 */
        .desktop-only {
            display: none !important;
        }
        
        /* 响应式表格 */
        @media (max-width: 600px) {
            table, thead, tbody, th, td, tr {
                display: block;
            }
            
            thead tr {
                position: absolute;
                top: -9999px;
                left: -9999px;
            }
            
            tr {
                margin-bottom: 10px;
                border: 1px solid #ccc;
                border-radius: 8px;
            }
            
            td {
                border: none;
                position: relative;
                padding-left: 50%;
            }
            
            td:before {
                content: attr(data-label);
                position: absolute;
                left: 6px;
                width: 45%;
                padding-right: 10px;
                white-space: nowrap;
                font-weight: bold;
            }
        }
    `;
    
    const style = document.createElement('style');
    style.textContent = css;
    document.head.appendChild(style);
}
"""
