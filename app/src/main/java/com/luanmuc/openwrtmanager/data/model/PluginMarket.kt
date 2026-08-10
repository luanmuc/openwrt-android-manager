package com.luanmuc.openwrtmanager.data.model

/**
 * 插件分类（更细致的分类）
 */
data class PluginCategory(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val description: String = "",
    val pluginCount: Int = 0,
    val parentId: String? = null
)

/**
 * 热门插件排行
 */
data class HotPlugin(
    val name: String = "",
    val displayName: String = "",
    val description: String = "",
    val icon: String = "",
    val category: String = "",
    val downloadCount: Int = 0,
    val rating: Float = 0f,
    val isNew: Boolean = false,
    val isHot: Boolean = false
)

/**
 * 插件详情（增强版）
 */
data class PluginDetail(
    val name: String = "",
    val displayName: String = "",
    val description: String = "",
    val longDescription: String = "",
    val icon: String = "",
    val category: String = "",
    val version: String = "",
    val size: Long = 0,
    val author: String = "",
    val homepage: String = "",
    val license: String = "",
    val screenshots: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val downloadCount: Int = 0,
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val lastUpdated: Long = 0,
    val isInstalled: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val latestVersion: String = ""
)

/**
 * 插件评论
 */
data class PluginReview(
    val id: String = "",
    val userName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val date: Long = 0,
    val helpfulCount: Int = 0
)
