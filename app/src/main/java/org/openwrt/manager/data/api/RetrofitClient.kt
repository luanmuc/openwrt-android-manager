package org.openwrt.manager.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端
 * 支持动态切换路由器地址
 */
object RetrofitClient {

    private var currentBaseUrl: String = ""
    private var retrofit: Retrofit? = null
    private var apiService: LuciApiService? = null

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    /**
     * 获取 API 服务实例
     * @param baseUrl 路由器基础地址，如 http://192.168.1.1
     */
    fun getApi(baseUrl: String): LuciApiService {
        if (retrofit == null || currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit!!.create(LuciApiService::class.java)
        }
        return apiService!!
    }

    /**
     * 重置客户端（切换路由器时调用）
     */
    fun reset() {
        retrofit = null
        apiService = null
        currentBaseUrl = ""
    }
}
