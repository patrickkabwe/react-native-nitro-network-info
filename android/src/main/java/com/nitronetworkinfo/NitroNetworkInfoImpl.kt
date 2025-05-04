package com.nitronetworkinfo

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.margelo.nitro.nitronetworkinfo.ConnectionType
import com.margelo.nitro.nitronetworkinfo.NitroNetworkStatusInfo

interface NetworkInfoDelegate {
    fun onNetworkInfoChanged(info: NitroNetworkStatusInfo)
}

class NitroNetworkInfoImpl(val context: Context, val delegate: NetworkInfoDelegate? = null) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback =   object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network is available")
                val  ni = NitroNetworkStatusInfo(
                    isConnected = true,
                    connectionType = getConnectionType()
                )
                mainHandler.post {
                    delegate?.onNetworkInfoChanged(ni)
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.d(TAG, "Network is unavailable")
                val  ni = NitroNetworkStatusInfo(
                    isConnected = false,
                    connectionType = ConnectionType.UNKNOWN
                )
                mainHandler.post {
                    delegate?.onNetworkInfoChanged(ni)
                }
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network is lost")
                val  ni = NitroNetworkStatusInfo(
                    isConnected = false,
                    connectionType = ConnectionType.UNKNOWN
                )
                mainHandler.post {
                    delegate?.onNetworkInfoChanged(ni)
                }
            }
        }

        connectivityManager.registerNetworkCallback(
            request,
            networkCallback!!
        )
    }

    fun getIsConnection(): Boolean {
        val network = connectivityManager.activeNetwork
        val isConnected = connectivityManager.getNetworkInfo(network)?.isConnected == true
        return isConnected
    }

    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val connectionType = when (true) {
            (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) -> {
                ConnectionType.WIFI
            }

            (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true) -> {
                ConnectionType.CELLULAR
            }

            (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true) -> {
                ConnectionType.ETHERNET
            }

            else -> ConnectionType.UNKNOWN
        }
        return connectionType
    }

    fun unregister() {
        networkCallback?.let {
            connectivityManager.unregisterNetworkCallback(it)
        }
        networkCallback = null
    }

    companion object {
        const val TAG = "NitroNetworkInfo"
    }
}