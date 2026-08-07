package com.luanmuc.openwrtmanager.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 通用缓存实体
 * 用于缓存各种API响应数据
 */
@Entity(tableName = "cache")
data class CacheEntity(
    @PrimaryKey
    val key: String,
    val value: String,
    val type: String,
    val routerId: String,
    val timestamp: Long = System.currentTimeMillis()
)
