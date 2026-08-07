package com.luanmuc.openwrtmanager.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 网络状态监控
 */
object NetworkMonitor {
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkType = MutableStateFlow(NetworkType.NONE)
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private var callback: ConnectivityManager.NetworkCallback? = null
    private var connectivityManager: ConnectivityManager? = null

    enum class NetworkType {
        NONE, WIFI, CELLULAR, ETHERNET, OTHER
    }

    fun init(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager = cm

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                updateNetworkState(cm, network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                _isConnected.value = false
                _networkType.value = NetworkType.NONE
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                updateNetworkState(cm, network)
            }
        }

        callback = networkCallback

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, networkCallback)

        // 初始化当前状态
        val activeNetwork = cm.activeNetwork
        if (activeNetwork != null) {
            updateNetworkState(cm, activeNetwork)
        } else {
            _isConnected.value = false
        }
    }

    private fun updateNetworkState(cm: ConnectivityManager, network: Network) {
        val capabilities = cm.getNetworkCapabilities(network) ?: return
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        _isConnected.value = hasInternet

        _networkType.value = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }
    }

    fun destroy() {
        callback?.let {
            connectivityManager?.unregisterNetworkCallback(it)
        }
        callback = null
        connectivityManager = null
    }
}
