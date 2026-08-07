package com.luanmuc.openwrtmanager.data.repository

import com.google.gson.Gson
import com.luanmuc.openwrtmanager.data.db.dao.CacheDao
import com.luanmuc.openwrtmanager.data.db.entity.CacheEntity

/**
 * 缓存仓库
 * 提供统一的缓存读写接口
 */
class CacheRepository(
    private val cacheDao: CacheDao,
    private val gson: Gson = Gson()
) {
    // 缓存过期时间（默认5分钟）
    private val defaultExpireTime = 5 * 60 * 1000L

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
     * 保存缓存
     */
    suspend fun <T> saveCache(
        key: String,
        routerId: String,
        type: String,
        data: T
    ) {
        val json = gson.toJson(data)
        val entity = CacheEntity(
            key = key,
            value = json,
            type = type,
            routerId = routerId
        )
        cacheDao.insertCache(entity)
    }

    /**
     * 删除缓存
     */
    suspend fun deleteCache(key: String, routerId: String) {
        cacheDao.deleteCache(key, routerId)
    }

    /**
     * 清除某个路由器的所有缓存
     */
    suspend fun clearRouterCache(routerId: String) {
        cacheDao.clearRouterCache(routerId)
    }

    /**
     * 清除过期缓存
     */
    suspend fun clearExpiredCache() {
        val expireTime = System.currentTimeMillis() - defaultExpireTime
        cacheDao.clearExpiredCache(expireTime)
    }

    /**
     * 缓存优先策略：先读缓存，再读网络，更新缓存
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
    ): T {
        return try {
            // 先读网络
            val data = fetch()
            // 更新缓存
            saveCache(key, routerId, type.simpleName, data)
            data
        } catch (e: Exception) {
            // 网络失败，读缓存
            val cached = getCache(key, routerId, type)
            cached ?: throw e
        }
    }

    companion object {
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
    }
}
