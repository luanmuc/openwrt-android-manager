package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import com.google.gson.Gson
import com.luanmuc.openwrtmanager.data.db.DatabaseHelper
import com.luanmuc.openwrtmanager.data.db.dao.CacheDao
import com.luanmuc.openwrtmanager.data.db.entity.CacheEntity

/**
 * 缓存仓库
 * 提供统一的缓存读写接口
 * 支持缓存优先策略：先显示缓存，同时后台刷新
 */
class CacheRepository private constructor(
    private val cacheDao: CacheDao,
    private val gson: Gson = Gson()
) {
    // 缓存过期时间（默认5分钟）
    val defaultExpireTime = 5 * 60 * 1000L
    
    // 缓存大小限制
    val maxCacheSize = 50  // 最大缓存条目数
    val maxCacheAge = 24 * 60 * 60 * 1000L  // 最大缓存年龄（24小时）
    val maxTotalSizeBytes = 10 * 1024 * 1024L  // 最大总缓存大小：10MB

    /**
     * 获取缓存
     */
    suspend fun <T> getCache(
        key: String,
        routerId: String,
        type: Class<T>
    ): T? {
        val cache = cacheDao.getCache(key, routerId) ?: return null
        // 检查是否过期
        if (System.currentTimeMillis() - cache.timestamp > defaultExpireTime) {
            return null
        }
        return try {
            gson.fromJson(cache.value, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取缓存（带自定义过期时间）
     */
    suspend fun <T> getCache(
        key: String,
        routerId: String,
        type: Class<T>,
        expireTime: Long
    ): T? {
        val cache = cacheDao.getCache(key, routerId) ?: return null
        // 检查是否过期
        if (System.currentTimeMillis() - cache.timestamp > expireTime) {
            return null
        }
        return try {
            gson.fromJson(cache.value, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取缓存（不检查过期，用于离线模式）
     */
    suspend fun <T> getCacheEvenExpired(
        key: String,
        routerId: String,
        type: Class<T>
    ): T? {
        val cache = cacheDao.getCache(key, routerId) ?: return null
        return try {
            gson.fromJson(cache.value, type)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 检查缓存是否存在且未过期
     */
    suspend fun hasValidCache(
        key: String,
        routerId: String
    ): Boolean {
        val cache = cacheDao.getCache(key, routerId) ?: return false
        return System.currentTimeMillis() - cache.timestamp <= defaultExpireTime
    }

    /**
     * 检查缓存是否存在（包括过期的）
     */
    suspend fun hasCache(
        key: String,
        routerId: String
    ): Boolean {
        return cacheDao.getCache(key, routerId) != null
    }

    /**
     * 获取缓存时间戳
     */
    suspend fun getCacheTimestamp(
        key: String,
        routerId: String
    ): Long? {
        val cache = cacheDao.getCache(key, routerId) ?: return null
        return cache.timestamp
    }

    /**
     * 保存缓存
     */
    suspend fun <T> saveCache(
        key: String,
        routerId: String,
        type: String,
        data: T
    ) {
        try {
            val json = gson.toJson(data)
            val entity = CacheEntity(
                key = key,
                value = json,
                type = type,
                routerId = routerId
            )
            cacheDao.insertCache(entity)
            
            // 检查缓存大小，超过限制则清理
            val cacheCount = cacheDao.getCacheCount(routerId)
            val totalSize = getCacheSizeBytes(routerId)
            
            // 条目数超过限制，清理过期的
            if (cacheCount > maxCacheSize) {
                clearExpiredCache()
            }
            
            // 总大小超过限制，清理最旧的缓存
            if (totalSize > maxTotalSizeBytes) {
                clearOldestCache(routerId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 删除缓存
     */
    suspend fun deleteCache(key: String, routerId: String) {
        try {
            cacheDao.deleteCache(key, routerId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除某个路由器的所有缓存
     */
    suspend fun clearRouterCache(routerId: String) {
        try {
            cacheDao.clearRouterCache(routerId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除过期缓存
     */
    suspend fun clearExpiredCache() {
        try {
            val expireTime = System.currentTimeMillis() - defaultExpireTime
            cacheDao.clearExpiredCache(expireTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 清除最旧的缓存（直到总大小低于限制）
     */
    private suspend fun clearOldestCache(routerId: String) {
        try {
            // 循环清理最旧的缓存，直到总大小低于限制
            var currentSize = getCacheSizeBytes(routerId)
            while (currentSize > maxTotalSizeBytes * 0.8) {  // 清理到80%以下
                val oldest = cacheDao.getOldestCache(routerId)
                if (oldest != null) {
                    cacheDao.deleteCache(oldest.key, routerId)
                    currentSize = getCacheSizeBytes(routerId)
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 清除所有缓存
     */
    suspend fun clearAllCache() {
        try {
            cacheDao.clearAllCache()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 获取缓存大小（条目数）
     */
    suspend fun getCacheSize(routerId: String): Int {
        return try {
            cacheDao.getCacheCount(routerId)
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * 获取缓存总大小（字节数）
     */
    suspend fun getCacheSizeBytes(routerId: String): Long {
        return try {
            val caches = cacheDao.getAllCaches(routerId)
            var totalSize = 0L
            for (cache in caches) {
                totalSize += cache.value.toByteArray().size
            }
            totalSize
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 缓存统计信息
     */
    data class CacheStats(
        val count: Int = 0,
        val totalSizeBytes: Long = 0L
    )

    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStats(routerId: String = ""): CacheStats {
        return try {
            val caches = cacheDao.getAllCaches(routerId)
            var totalSize = 0L
            for (cache in caches) {
                totalSize += cache.value.toByteArray().size
            }
            CacheStats(
                count = caches.size,
                totalSizeBytes = totalSize
            )
        } catch (e: Exception) {
            CacheStats()
        }
    }

    /**
     * 缓存优先策略：先读缓存，再读网络，更新缓存
     * 注意：这是同步版本，只返回一个结果
     */
    suspend fun <T> cacheFirst(
        key: String,
        routerId: String,
        type: Class<T>,
        fetch: suspend () -> T
    ): T {
        // 先读缓存
        val cached = getCache(key, routerId, type)
        if (cached != null) {
            return cached
        }
        // 缓存没有，读网络
        val data = fetch()
        // 更新缓存
        saveCache(key, routerId, type.simpleName, data)
        return data
    }

    /**
     * 网络优先策略：先读网络，失败再读缓存
     */
    suspend fun <T> networkFirst(
        key: String,
        routerId: String,
        type: Class<T>,
        fetch: suspend () -> T
    ): Result<T> {
        return try {
            // 先读网络
            val data = fetch()
            // 更新缓存
            saveCache(key, routerId, type.simpleName, data)
            Result.success(data)
        } catch (e: Exception) {
            // 网络失败，读缓存
            val cached = getCacheEvenExpired(key, routerId, type)
            if (cached != null) {
                Result.success(cached)
            } else {
                Result.failure(e)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CacheRepository? = null

        fun getInstance(context: Context): CacheRepository {
            return INSTANCE ?: synchronized(this) {
                val databaseHelper = DatabaseHelper.getInstance(context)
                INSTANCE ?: CacheRepository(
                    cacheDao = databaseHelper.database.cacheDao()
                ).also { INSTANCE = it }
            }
        }

        // 缓存Key常量
        const val KEY_ROUTER_STATUS = "router_status"
        const val KEY_WAN_STATUS = "wan_status"
        const val KEY_ONLINE_DEVICES = "online_devices"
        const val KEY_INSTALLED_PACKAGES = "installed_packages"
        const val KEY_AVAILABLE_PACKAGES = "available_packages"
        const val KEY_SYSTEM_LOG = "system_log"
        const val KEY_PROCESS_LIST = "process_list"
        const val KEY_NETWORK_INTERFACES = "network_interfaces"
        const val KEY_WIFI_DEVICES = "wifi_devices"
        const val KEY_PORT_FORWARDS = "port_forwards"
        const val KEY_DDNS_CONFIGS = "ddns_configs"
        const val KEY_LAN_CONFIG = "lan_config"
        const val KEY_WAN_CONFIG = "wan_config"
        const val KEY_DHCP_CONFIG = "dhcp_config"
        const val KEY_SYSTEM_INFO = "system_info"
    }
}
