package com.luanmuc.openwrtmanager.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 数据库助手
 * 管理数据库实例的单例
 */
class DatabaseHelper private constructor(context: Context) {

    /**
     * 从版本1迁移到版本2
     * 版本1可能只有routers表，版本2添加了cache表
     * 如果cache表已存在则跳过创建
     */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `cache` (
                    `key` TEXT NOT NULL,
                    `value` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `routerId` TEXT NOT NULL,
                    `timestamp` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`key`)
                )
            """.trimIndent())
        }
    }

    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME
    )
    .addMigrations(MIGRATION_1_2)
    .build()

    companion object {
        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context).also { INSTANCE = it }
            }
        }
    }
}
