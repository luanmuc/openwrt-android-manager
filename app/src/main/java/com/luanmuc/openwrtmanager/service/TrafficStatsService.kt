package com.luanmuc.openwrtmanager.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.luanmuc.openwrtmanager.util.DebugMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 流量统计服务
 * 后台持续统计流量数据
 */
class TrafficStatsService : Service() {
    
    companion object {
        private const val TAG = "TrafficStatsService"
        private const val STATS_INTERVAL = 30 * 1000L // 30秒统计一次
        
        fun start(context: Context) {
            try {
                val intent = Intent(context, TrafficStatsService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "启动服务失败", e)
            }
        }
        
        fun stop(context: Context) {
            try {
                val intent = Intent(context, TrafficStatsService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "停止服务失败", e)
            }
        }
    }
    
    private var statsJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "服务创建")
        startStats()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "服务启动")
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "服务销毁")
        stopStats()
    }
    
    /**
     * 开始流量统计
     */
    private fun startStats() {
        stopStats()
        
        statsJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    recordTrafficPoint()
                } catch (e: Exception) {
                    Log.e(TAG, "记录流量数据失败", e)
                }
                delay(STATS_INTERVAL)
            }
        }
    }
    
    /**
     * 停止流量统计
     */
    private fun stopStats() {
        statsJob?.cancel()
        statsJob = null
    }
    
    /**
     * 记录流量数据点
     */
    private suspend fun recordTrafficPoint() {
        try {
            Log.d(TAG, "记录流量数据点")
            
            if (DebugMode.isDebugMode) {
                // 演示模式：模拟流量数据
                recordDemoTraffic()
            } else {
                // 真实模式：从路由器获取流量数据并记录
                recordRealTraffic()
            }
        } catch (e: Exception) {
            Log.e(TAG, "记录流量数据点失败", e)
        }
    }
    
    /**
     * 演示模式：记录模拟流量数据
     */
    private fun recordDemoTraffic() {
        try {
            // 模拟下载和上传速度
            val downloadSpeed = (Math.random() * 10 * 1024 * 1024).toLong() // 0-10 MB/s
            val uploadSpeed = (Math.random() * 2 * 1024 * 1024).toLong() // 0-2 MB/s
            
            Log.d(TAG, "演示模式流量记录: 下载=${formatSpeed(downloadSpeed)}, 上传=${formatSpeed(uploadSpeed)}")
            
            // 这里应该调用TrafficRepository.recordTrafficPoint()
            // 暂时只记录日志，后续完善
        } catch (e: Exception) {
            Log.e(TAG, "记录演示流量数据失败", e)
        }
    }
    
    /**
     * 真实模式：从路由器获取流量数据并记录
     */
    private suspend fun recordRealTraffic() {
        try {
            Log.d(TAG, "真实模式：从路由器获取流量数据")
            // 这里应该调用LuciRepository获取流量数据
            // 然后调用TrafficRepository.recordTrafficPoint()
            // 暂时留空，后续完善真实API对接
        } catch (e: Exception) {
            Log.e(TAG, "记录真实流量数据失败", e)
        }
    }
    
    /**
     * 格式化速度显示
     */
    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond >= 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSecond / (1024.0 * 1024.0))
            bytesPerSecond >= 1024 -> String.format("%.2f KB/s", bytesPerSecond / 1024.0)
            else -> "$bytesPerSecond B/s"
        }
    }
}