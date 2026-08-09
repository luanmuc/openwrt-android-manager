package com.luanmuc.openwrtmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luanmuc.openwrtmanager.data.db.entity.CacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheDao {
    @Query("SELECT * FROM cache WHERE `key` = :key AND routerId = :routerId")
    suspend fun getCache(key: String, routerId: String): CacheEntity?

    @Query("SELECT * FROM cache WHERE type = :type AND routerId = :routerId")
    suspend fun getCacheByType(type: String, routerId: String): List<CacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: CacheEntity)

    @Query("DELETE FROM cache WHERE `key` = :key AND routerId = :routerId")
    suspend fun deleteCache(key: String, routerId: String)

    @Query("DELETE FROM cache WHERE routerId = :routerId")
    suspend fun clearRouterCache(routerId: String)

    @Query("DELETE FROM cache WHERE timestamp < :expireTime")
    suspend fun clearExpiredCache(expireTime: Long)
    
    @Query("SELECT COUNT(*) FROM cache WHERE routerId = :routerId")
    suspend fun getCacheCount(routerId: String): Int
    
    @Query("SELECT * FROM cache WHERE routerId = :routerId")
    suspend fun getAllCaches(routerId: String): List<CacheEntity>
    
    @Query("SELECT * FROM cache WHERE routerId = :routerId ORDER BY timestamp ASC LIMIT 1")
    suspend fun getOldestCache(routerId: String): CacheEntity?
    
    @Query("DELETE FROM cache")
    suspend fun clearAllCache()
}
