package org.openwrt.manager.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openwrt.manager.data.model.Router
import org.openwrt.manager.util.EncryptionUtil

private val Context.dataStore by preferencesDataStore(name = "routers")

/**
 * 路由器数据仓库
 * 负责路由器信息的持久化存储
 */
class RouterRepository(private val context: Context) {

    private val gson = Gson()
    private val routersKey = stringPreferencesKey("routers_list")
    private val activeRouterKey = stringPreferencesKey("active_router_id")

    /**
     * 获取所有路由器列表
     */
    val routers: Flow<List<Router>> = context.dataStore.data.map { preferences ->
        val json = preferences[routersKey] ?: "[]"
        val type = object : TypeToken<List<Router>>() {}.type
        gson.fromJson(json, type)
    }

    /**
     * 获取当前活动的路由器ID
     */
    val activeRouterId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[activeRouterKey]
    }

    /**
     * 添加路由器
     */
    suspend fun addRouter(router: Router) {
        val currentList = getRoutersList().toMutableList()
        currentList.add(router)
        saveRoutersList(currentList)
    }

    /**
     * 删除路由器
     */
    suspend fun removeRouter(routerId: String) {
        val currentList = getRoutersList().filter { it.id != routerId }
        saveRoutersList(currentList)
    }

    /**
     * 更新路由器信息
     */
    suspend fun updateRouter(router: Router) {
        val currentList = getRoutersList().toMutableList()
        val index = currentList.indexOfFirst { it.id == router.id }
        if (index != -1) {
            currentList[index] = router
            saveRoutersList(currentList)
        }
    }

    /**
     * 设置当前活动路由器
     */
    suspend fun setActiveRouter(routerId: String?) {
        context.dataStore.edit { preferences ->
            if (routerId != null) {
                preferences[activeRouterKey] = routerId
            } else {
                preferences.remove(activeRouterKey)
            }
        }
    }

    /**
     * 根据ID获取路由器
     */
    suspend fun getRouterById(routerId: String): Router? {
        return getRoutersList().find { it.id == routerId }
    }

    private suspend fun getRoutersList(): List<Router> {
        val prefs = context.dataStore.data
        val json = prefs.toString()
        // 直接从DataStore读取
        val contextData = context.dataStore
        var result: List<Router> = emptyList()
        contextData.data.collect { preferences ->
            val jsonStr = preferences[routersKey] ?: "[]"
            val type = object : TypeToken<List<Router>>() {}.type
            result = gson.fromJson(jsonStr, type)
        }
        return result
    }

    private suspend fun saveRoutersList(routers: List<Router>) {
        val json = gson.toJson(routers)
        context.dataStore.edit { preferences ->
            preferences[routersKey] = json
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RouterRepository? = null

        fun getInstance(context: Context): RouterRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RouterRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
