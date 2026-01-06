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
import java.lang.ref.WeakReference

interface NetworkInfoDelegate {
    fun onNetworkInfoChanged(info: NitroNetworkStatusInfo)
}

class NitroNetworkInfoImpl(context: Context, delegate: NetworkInfoDelegate? = null) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val delegateRef = WeakReference(delegate)

    @Synchronized
    fun start() {
        if (networkCallback != null) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network is available")
                emit(NitroNetworkStatusInfo(
                    isConnected = true,
                    connectionType = getConnectionType()
                ))
            }

            override fun onLost(network: Network) {
                Log.d(TAG, "Network is lost")
                emit(NitroNetworkStatusInfo(
                    isConnected = false,
                    connectionType = ConnectionType.UNKNOWN
                ))
            }
        }
        networkCallback?.let {
            connectivityManager.registerNetworkCallback(request, it)
        }
    }

    fun emit(info: NitroNetworkStatusInfo){
        mainHandler.post {
            delegateRef.get()?.onNetworkInfoChanged(info)
        }
    }

    fun getIsConnection(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        val connectionType = when (true) {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                ConnectionType.WIFI
            }

            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                ConnectionType.CELLULAR
            }

            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                ConnectionType.ETHERNET
            }

            else -> ConnectionType.UNKNOWN
        }
        return connectionType
    }

    fun stop() {
        mainHandler.removeCallbacksAndMessages(null)

        networkCallback?.let {
            try { connectivityManager.unregisterNetworkCallback(it) }
            catch (e: IllegalArgumentException) {
                Log.w(TAG, "Network callback already unregistered", e)
            }
        }
        networkCallback = null
    }

    companion object {
        const val TAG = "NitroNetworkInfo"
    }
}