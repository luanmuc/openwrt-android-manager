package com.luanmuc.openwrtmanager.util

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * JSON工具类
 * 提供JSON解析和构建的便捷方法
 */
object JsonUtils {
    
    private const val TAG = "JsonUtils"
    
    /**
     * 安全获取JSONObject
     */
    fun getJSONObject(json: JSONObject?, key: String): JSONObject? {
        return try {
            json?.getJSONObject(key)
        } catch (e: JSONException) {
            Log.e(TAG, "获取JSONObject失败: $key", e)
            null
        }
    }
    
    /**
     * 安全获取JSONArray
     */
    fun getJSONArray(json: JSONObject?, key: String): JSONArray? {
        return try {
            json?.getJSONArray(key)
        } catch (e: JSONException) {
            Log.e(TAG, "获取JSONArray失败: $key", e)
            null
        }
    }
    
    /**
     * 安全获取String
     */
    fun getString(json: JSONObject?, key: String, defaultValue: String = ""): String {
        return try {
            json?.getString(key) ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }
    
    /**
     * 安全获取Int
     */
    fun getInt(json: JSONObject?, key: String, defaultValue: Int = 0): Int {
        return try {
            json?.getInt(key) ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }
    
    /**
     * 安全获取Long
     */
    fun getLong(json: JSONObject?, key: String, defaultValue: Long = 0L): Long {
        return try {
            json?.getLong(key) ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }
    
    /**
     * 安全获取Double
     */
    fun getDouble(json: JSONObject?, key: String, defaultValue: Double = 0.0): Double {
        return try {
            json?.getDouble(key) ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }
    
    /**
     * 安全获取Boolean
     */
    fun getBoolean(json: JSONObject?, key: String, defaultValue: Boolean = false): Boolean {
        return try {
            json?.getBoolean(key) ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }
    
    /**
     * 安全获取String数组
     */
    fun getStringArray(json: JSONObject?, key: String): List<String> {
        return try {
            val array = json?.getJSONArray(key) ?: return emptyList()
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: JSONException) {
            emptyList()
        }
    }
    
    /**
     * 安全获取Int数组
     */
    fun getIntArray(json: JSONObject?, key: String): List<Int> {
        return try {
            val array = json?.getJSONArray(key) ?: return emptyList()
            val list = mutableListOf<Int>()
            for (i in 0 until array.length()) {
                list.add(array.getInt(i))
            }
            list
        } catch (e: JSONException) {
            emptyList()
        }
    }
    
    /**
     * 安全获取JSONObject数组
     */
    fun getJSONObjectArray(json: JSONObject?, key: String): List<JSONObject> {
        return try {
            val array = json?.getJSONArray(key) ?: return emptyList()
            val list = mutableListOf<JSONObject>()
            for (i in 0 until array.length()) {
                list.add(array.getJSONObject(i))
            }
            list
        } catch (e: JSONException) {
            emptyList()
        }
    }
    
    /**
     * 解析JSON字符串为JSONObject
     */
    fun parseJsonObject(jsonString: String): JSONObject? {
        return try {
            JSONObject(jsonString)
        } catch (e: JSONException) {
            Log.e(TAG, "解析JSON失败", e)
            null
        }
    }
    
    /**
     * 解析JSON字符串为JSONArray
     */
    fun parseJsonArray(jsonString: String): JSONArray? {
        return try {
            JSONArray(jsonString)
        } catch (e: JSONException) {
            Log.e(TAG, "解析JSONArray失败", e)
            null
        }
    }
    
    /**
     * 检查是否是有效的JSON
     */
    fun isValidJson(jsonString: String): Boolean {
        return try {
            JSONObject(jsonString)
            true
        } catch (e: JSONException) {
            try {
                JSONArray(jsonString)
                true
            } catch (e2: JSONException) {
                false
            }
        }
    }
    
    /**
     * 检查是否是有效的JSONObject
     */
    fun isValidJsonObject(jsonString: String): Boolean {
        return try {
            JSONObject(jsonString)
            true
        } catch (e: JSONException) {
            false
        }
    }
    
    /**
     * 检查是否是有效的JSONArray
     */
    fun isValidJsonArray(jsonString: String): Boolean {
        return try {
            JSONArray(jsonString)
            true
        } catch (e: JSONException) {
            false
        }
    }
    
    /**
     * 构建JSONObject
     */
    fun buildJsonObject(vararg pairs: Pair<String, Any?>): JSONObject {
        val json = JSONObject()
        for ((key, value) in pairs) {
            try {
                when (value) {
                    null -> json.put(key, JSONObject.NULL)
                    is String -> json.put(key, value)
                    is Int -> json.put(key, value)
                    is Long -> json.put(key, value)
                    is Double -> json.put(key, value)
                    is Boolean -> json.put(key, value)
                    is JSONObject -> json.put(key, value)
                    is JSONArray -> json.put(key, value)
                    else -> json.put(key, value.toString())
                }
            } catch (e: JSONException) {
                Log.e(TAG, "构建JSON失败: $key", e)
            }
        }
        return json
    }
    
    /**
     * 构建JSONArray
     */
    fun buildJsonArray(vararg items: Any?): JSONArray {
        val array = JSONArray()
        for (item in items) {
            try {
                when (item) {
                    null -> array.put(JSONObject.NULL)
                    is String -> array.put(item)
                    is Int -> array.put(item)
                    is Long -> array.put(item)
                    is Double -> array.put(item)
                    is Boolean -> array.put(item)
                    is JSONObject -> array.put(item)
                    is JSONArray -> array.put(item)
                    else -> array.put(item.toString())
                }
            } catch (e: JSONException) {
                Log.e(TAG, "构建JSONArray失败", e)
            }
        }
        return array
    }
    
    /**
     * List<String>转JSONArray
     */
    fun stringListToJsonArray(list: List<String>): JSONArray {
        val array = JSONArray()
        for (item in list) {
            array.put(item)
        }
        return array
    }
    
    /**
     * JSONArray转List<String>
     */
    fun jsonArrayToStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            try {
                list.add(array.getString(i))
            } catch (e: JSONException) {
                // 跳过无效项
            }
        }
        return list
    }
    
    /**
     * 获取JSONArray长度
     */
    fun getJsonArrayLength(array: JSONArray?): Int {
        return array?.length() ?: 0
    }
    
    /**
     * 安全获取JSONArray中的JSONObject
     */
    fun getJSONObjectFromArray(array: JSONArray?, index: Int): JSONObject? {
        return try {
            array?.getJSONObject(index)
        } catch (e: JSONException) {
            null
        }
    }
    
    /**
     * 安全获取JSONArray中的String
     */
    fun getStringFromArray(array: JSONArray?, index: Int, defaultValue: String = ""): String {
        return try {
            array?.getString(index) ?: defaultValue
        } catch (e: JSONException) {
            defaultValue
        }
    }
    
    /**
     * 合并两个JSONObject
     */
    fun mergeJsonObjects(json1: JSONObject, json2: JSONObject): JSONObject {
        val result = JSONObject(json1.toString())
        val keys = json2.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                result.put(key, json2.get(key))
            } catch (e: JSONException) {
                Log.e(TAG, "合并JSON失败: $key", e)
            }
        }
        return result
    }
    
    /**
     * 获取所有key
     */
    fun getKeys(json: JSONObject?): List<String> {
        if (json == null) return emptyList()
        val keys = mutableListOf<String>()
        val iterator = json.keys()
        while (iterator.hasNext()) {
            keys.add(iterator.next())
        }
        return keys
    }
    
    /**
     * 检查是否包含key
     */
    fun hasKey(json: JSONObject?, key: String): Boolean {
        return json?.has(key) ?: false
    }
    
    /**
     * 检查key是否为null
     */
    fun isNull(json: JSONObject?, key: String): Boolean {
        return try {
            json?.isNull(key) ?: true
        } catch (e: JSONException) {
            true
        }
    }
    
    /**
     * 格式化JSON字符串（美化）
     */
    fun formatJson(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            json.toString(2)
        } catch (e: JSONException) {
            try {
                val array = JSONArray(jsonString)
                array.toString(2)
            } catch (e2: JSONException) {
                jsonString
            }
        }
    }
    
    /**
     * 压缩JSON字符串
     */
    fun compactJson(jsonString: String): String {
        return try {
            if (isValidJsonObject(jsonString)) {
                JSONObject(jsonString).toString()
            } else if (isValidJsonArray(jsonString)) {
                JSONArray(jsonString).toString()
            } else {
                jsonString
            }
        } catch (e: JSONException) {
            jsonString
        }
    }
    
    /**
     * 从Map构建JSONObject
     */
    fun mapToJsonObject(map: Map<String, Any?>): JSONObject {
        val json = JSONObject()
        for ((key, value) in map) {
            try {
                when (value) {
                    null -> json.put(key, JSONObject.NULL)
                    is String -> json.put(key, value)
                    is Int -> json.put(key, value)
                    is Long -> json.put(key, value)
                    is Double -> json.put(key, value)
                    is Boolean -> json.put(key, value)
                    is Map<*, *> -> json.put(key, mapToJsonObject(value as Map<String, Any?>))
                    is List<*> -> json.put(key, listToJsonArray(value))
                    else -> json.put(key, value.toString())
                }
            } catch (e: JSONException) {
                Log.e(TAG, "Map转JSON失败: $key", e)
            }
        }
        return json
    }
    
    /**
     * 从List构建JSONArray
     */
    fun listToJsonArray(list: List<*>): JSONArray {
        val array = JSONArray()
        for (item in list) {
            try {
                when (item) {
                    null -> array.put(JSONObject.NULL)
                    is String -> array.put(item)
                    is Int -> array.put(item)
                    is Long -> array.put(item)
                    is Double -> array.put(item)
                    is Boolean -> array.put(item)
                    is Map<*, *> -> array.put(mapToJsonObject(item as Map<String, Any?>))
                    is List<*> -> array.put(listToJsonArray(item))
                    else -> array.put(item.toString())
                }
            } catch (e: JSONException) {
                Log.e(TAG, "List转JSONArray失败", e)
            }
        }
        return array
    }
    
    /**
     * JSONObject转Map
     */
    fun jsonObjectToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            try {
                val value = json.get(key)
                map[key] = when {
                    value is JSONObject -> jsonObjectToMap(value)
                    value is JSONArray -> jsonArrayToList(value)
                    value == JSONObject.NULL -> null
                    else -> value
                }
            } catch (e: JSONException) {
                map[key] = null
            }
        }
        return map
    }
    
    /**
     * JSONArray转List
     */
    fun jsonArrayToList(array: JSONArray): List<Any?> {
        val list = mutableListOf<Any?>()
        for (i in 0 until array.length()) {
            try {
                val value = array.get(i)
                list.add(
                    when {
                        value is JSONObject -> jsonObjectToMap(value)
                        value is JSONArray -> jsonArrayToList(value)
                        value == JSONObject.NULL -> null
                        else -> value
                    }
                )
            } catch (e: JSONException) {
                list.add(null)
            }
        }
        return list
    }
}