package com.luanmuc.openwrtmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.luanmuc.openwrtmanager.data.db.entity.RouterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterDao {
    @Query("SELECT * FROM routers ORDER BY isActive DESC, lastConnected DESC")
    fun getAllRouters(): Flow<List<RouterEntity>>

    @Query("SELECT * FROM routers WHERE id = :id")
    suspend fun getRouterById(id: Long): RouterEntity?

    @Query("SELECT * FROM routers WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveRouter(): RouterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: RouterEntity): Long

    @Update
    suspend fun updateRouter(router: RouterEntity)

    @Query("DELETE FROM routers WHERE id = :id")
    suspend fun deleteRouter(id: Long)

    @Query("UPDATE routers SET isActive = 0")
    suspend fun clearActiveRouter()

    @Query("UPDATE routers SET isActive = 1 WHERE id = :id")
    suspend fun setActiveRouter(id: Long)

    @Query("UPDATE routers SET lastConnected = :timestamp WHERE id = :id")
    suspend fun updateLastConnected(id: Long, timestamp: Long)

    @Query("SELECT DISTINCT `group` FROM routers ORDER BY `group`")
    suspend fun getAllGroups(): List<String>
}
