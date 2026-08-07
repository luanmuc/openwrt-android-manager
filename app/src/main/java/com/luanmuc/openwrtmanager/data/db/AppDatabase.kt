package com.luanmuc.openwrtmanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.luanmuc.openwrtmanager.data.db.dao.CacheDao
import com.luanmuc.openwrtmanager.data.db.entity.CacheEntity

@Database(
    entities = [CacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao

    companion object {
        const val DATABASE_NAME = "openwrt_manager.db"
    }
}
