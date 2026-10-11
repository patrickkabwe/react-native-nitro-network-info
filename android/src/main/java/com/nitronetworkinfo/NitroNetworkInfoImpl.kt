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

/**
 * Tracks every network that offers internet, not just the one that changed.
 *
 * A handover — Wi-Fi to cellular at a door — is a loss and a gain moments
 * apart, and `onLost` for the Wi-Fi often arrives while cellular is already
 * up. Reporting "disconnected" on every `onLost` told apps the phone was
 * offline when it was not. So each callback only updates the set of live
 * networks, the status is derived from that set and the active network, and
 * a status is emitted only when it actually differs from the last one.
 */
class NitroNetworkInfoImpl(context: Context, delegate: NetworkInfoDelegate? = null) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val delegateRef = WeakReference(delegate)
    private val networks = mutableSetOf<Network>()
    private var lastEmitted: NitroNetworkStatusInfo? = null

    @Synchronized
    fun start() {
        if (networkCallback != null) return

        lastEmitted = currentStatus()

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                synchronized(this@NitroNetworkInfoImpl) { networks.add(network) }
                emitIfChanged()
            }

            override fun onLost(network: Network) {
                synchronized(this@NitroNetworkInfoImpl) { networks.remove(network) }
                emitIfChanged()
            }

            // Wi-Fi ↔ cellular as the default network, without either being lost.
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                emitIfChanged()
            }
        }
        networkCallback = callback
        connectivityManager.registerNetworkCallback(request, callback)
    }

    @Synchronized
    fun stop() {
        mainHandler.removeCallbacksAndMessages(null)

        networkCallback?.let {
            try { connectivityManager.unregisterNetworkCallback(it) }
            catch (e: IllegalArgumentException) {
                Log.w(TAG, "Network callback already unregistered", e)
            }
        }
        networkCallback = null
        networks.clear()
        lastEmitted = null
    }

    /** Connected while any internet network is up, even if not yet the active one. */
    @Synchronized
    fun getIsConnection(): Boolean {
        return networks.isNotEmpty() || activeHasInternet()
    }

    @Synchronized
    fun getConnectionType(): ConnectionType {
        val active = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val capabilities = active ?: networks.firstNotNullOfOrNull { connectivityManager.getNetworkCapabilities(it) }
        return when {
            capabilities == null -> ConnectionType.UNKNOWN
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.UNKNOWN
        }
    }

    @Synchronized
    fun currentStatus(): NitroNetworkStatusInfo {
        val connected = getIsConnection()
        return NitroNetworkStatusInfo(
            isConnected = connected,
            connectionType = if (connected) getConnectionType() else ConnectionType.UNKNOWN
        )
    }

    private fun emitIfChanged() {
        val status: NitroNetworkStatusInfo
        synchronized(this) {
            if (networkCallback == null) return
            status = currentStatus()
            if (status == lastEmitted) return
            lastEmitted = status
        }
        mainHandler.post {
            delegateRef.get()?.onNetworkInfoChanged(status)
        }
    }

    private fun activeHasInternet(): Boolean {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    companion object {
        const val TAG = "NitroNetworkInfo"
    }
}
