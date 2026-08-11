package com.luanmuc.openwrtmanager.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Retrofit 客户端
 * 支持动态切换路由器地址，支持自签名证书
 */
object RetrofitClient {
    private var currentBaseUrl: String = ""
    private var retrofit: Retrofit? = null
    private var apiService: LuciApiService? = null

    private val trustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
    }

    private val trustAllCerts = arrayOf<TrustManager>(trustManager)

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * 获取 API 服务实例
     * @param baseUrl 路由器基础地址，如 http://192.168.1.1
     */
    fun getApi(baseUrl: String): LuciApiService {
        if (retrofit == null || currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            val newRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit = newRetrofit
            apiService = newRetrofit.create(LuciApiService::class.java)
        }
        return apiService ?: error("ApiService initialization failed")
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