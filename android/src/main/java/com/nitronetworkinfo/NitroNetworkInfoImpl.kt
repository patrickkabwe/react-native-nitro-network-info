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
 * Watches every network that offers internet for as long as it exists.
 *
 * Reading the status never leaves the process: it is one volatile read of a
 * value the network callback keeps current. Asking ConnectivityManager on each
 * read would be two binder calls into system_server, synchronously on the JS
 * thread, every time JavaScript touched `isConnected`.
 *
 * The status is derived from the whole set of live networks, not from the one
 * that changed. A handover — Wi-Fi to cellular at a door — is a loss and a gain
 * moments apart, and `onLost` for the Wi-Fi often arrives while cellular is
 * already up; reporting that as "disconnected" was wrong.
 */
class NitroNetworkInfoImpl(context: Context, delegate: NetworkInfoDelegate? = null) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val delegateRef = WeakReference(delegate)

    // Written only on the connectivity callback thread, read from any thread.
    @Volatile
    var status: NitroNetworkStatusInfo = queryStatus()
        private set

    // Confined to the connectivity callback thread, which delivers every
    // callback below in order, so it needs no lock.
    private val networks = HashMap<Network, NetworkCapabilities?>()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // One binder call per network appearing, which is rare. Below API
            // 26 no onCapabilitiesChanged is guaranteed to follow.
            networks[network] = connectivityManager.getNetworkCapabilities(network)
            recompute()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            // Cellular signal strength alone fires this every few seconds; the
            // capabilities arrive with it, and recompute drops what changed
            // nothing anybody reads.
            networks[network] = networkCapabilities
            recompute()
        }

        override fun onLost(network: Network) {
            networks.remove(network)
            recompute()
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)
    }

    fun dispose() {
        mainHandler.removeCallbacksAndMessages(null)
        try {
            connectivityManager.unregisterNetworkCallback(callback)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Network callback already unregistered", e)
        }
    }

    private fun recompute() {
        val next = statusOf(networks.values)
        if (next == status) return
        status = next
        mainHandler.post { delegateRef.get()?.onNetworkInfoChanged(next) }
    }

    /** The starting status, asked once before the callback has reported. */
    private fun queryStatus(): NitroNetworkStatusInfo {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.takeIf { it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }
        return statusOf(listOfNotNull(capabilities))
    }

    companion object {
        const val TAG = "NitroNetworkInfo"

        /**
         * Connected while any internet network is up. The type is the one
         * Android itself prefers as the default — ethernet, then Wi-Fi, then
         * cellular — so no binder call is needed to ask which that is.
         */
        private fun statusOf(capabilities: Collection<NetworkCapabilities?>): NitroNetworkStatusInfo {
            if (capabilities.isEmpty()) {
                return NitroNetworkStatusInfo(isConnected = false, connectionType = ConnectionType.UNKNOWN)
            }
            val type = when {
                capabilities.any { it?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true } -> ConnectionType.ETHERNET
                capabilities.any { it?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true } -> ConnectionType.WIFI
                capabilities.any { it?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true } -> ConnectionType.CELLULAR
                else -> ConnectionType.UNKNOWN
            }
            return NitroNetworkStatusInfo(isConnected = true, connectionType = type)
        }
    }
}
