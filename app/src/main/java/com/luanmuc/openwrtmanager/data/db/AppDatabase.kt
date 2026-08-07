package com.luanmuc.openwrtmanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luanmuc.openwrtmanager.data.db.dao.CacheDao
import com.luanmuc.openwrtmanager.data.db.dao.RouterDao
import com.luanmuc.openwrtmanager.data.db.entity.CacheEntity
import com.luanmuc.openwrtmanager.data.db.entity.RouterEntity

@Database(
    entities = [CacheEntity::class, RouterEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
    abstract fun routerDao(): RouterDao

    companion object {
        const val DATABASE_NAME = "openwrt_manager.db"
    }
}
