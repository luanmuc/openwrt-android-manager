package com.luanmuc.openwrtmanager.data.repository

import android.content.Context
import com.luanmuc.openwrtmanager.data.db.DatabaseHelper
import com.luanmuc.openwrtmanager.data.db.entity.RouterEntity
import com.luanmuc.openwrtmanager.util.EncryptionUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 路由器仓库 - 使用Room数据库实现
 * 管理路由器配置的增删改查
 */
class RouterRepository private constructor() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var appContext: Context

    /**
     * 初始化
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        databaseHelper = DatabaseHelper.getInstance(appContext)
    }

    /**
     * 获取所有路由器列表（Flow）
     */
    fun getAllRouters(): Flow<List<RouterEntity>> {
        return databaseHelper.routerDao.getAllRouters()
    }

    /**
     * 获取当前活动路由器（Flow）
     */
    fun getActiveRouter(): Flow<RouterEntity?> {
        return databaseHelper.routerDao.getActiveRouter()
    }

    /**
     * 根据ID获取路由器
     */
    suspend fun getRouterById(id: String): RouterEntity? {
        val router = databaseHelper.routerDao.getRouterById(id)
        // 解密密码
        router?.let {
            if (it.password.isNotEmpty()) {
                try {
                    it.password = EncryptionUtil.decrypt(it.password, "openwrt_manager_key")
                } catch (e: Exception) {
                    // 解密失败，保持原样
                }
            }
        }
        return router
    }

    /**
     * 添加路由器
     */
    suspend fun addRouter(router: RouterEntity) {
        // 加密密码
        val encryptedRouter = router.copy()
        if (encryptedRouter.password.isNotEmpty()) {
            try {
                encryptedRouter.password = EncryptionUtil.encrypt(encryptedRouter.password, "openwrt_manager_key")
            } catch (e: Exception) {
                // 加密失败，保持原样
            }
        }
        databaseHelper.routerDao.insertRouter(encryptedRouter)
    }

    /**
     * 更新路由器
     */
    suspend fun updateRouter(router: RouterEntity) {
        // 加密密码
        val encryptedRouter = router.copy()
        if (encryptedRouter.password.isNotEmpty()) {
            try {
                encryptedRouter.password = EncryptionUtil.encrypt(encryptedRouter.password, "openwrt_manager_key")
            } catch (e: Exception) {
                // 加密失败，保持原样
            }
        }
        databaseHelper.routerDao.updateRouter(encryptedRouter)
    }

    /**
     * 删除路由器
     */
    suspend fun deleteRouter(id: String) {
        databaseHelper.routerDao.deleteRouter(id)
    }

    /**
     * 设置当前活动路由器
     */
    suspend fun setActiveRouter(id: String) {
        databaseHelper.routerDao.setActiveRouter(id)
    }

    /**
     * 更新最后连接时间
     */
    suspend fun updateLastConnected(id: String, time: Long) {
        databaseHelper.routerDao.updateLastConnected(id, time)
    }

    /**
     * 获取所有分组
     */
    suspend fun getAllGroups(): List<String> {
        return databaseHelper.routerDao.getAllGroups()
    }

    companion object {
        @Volatile
        private var instance: RouterRepository? = null

        fun getInstance(context: Context? = null): RouterRepository {
            return instance ?: synchronized(this) {
                instance ?: RouterRepository().also {
                    instance = it
                    context?.let { ctx -> it.init(ctx) }
                }
            }
        }
    }
}
