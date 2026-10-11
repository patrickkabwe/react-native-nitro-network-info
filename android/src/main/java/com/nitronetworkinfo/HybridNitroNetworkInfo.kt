package com.nitronetworkinfo

import com.margelo.nitro.NitroModules
import com.margelo.nitro.nitronetworkinfo.ConnectionType
import com.margelo.nitro.nitronetworkinfo.HybridNitroNetworkInfoSpec
import com.margelo.nitro.nitronetworkinfo.NitroNetworkStatusInfo
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong


class HybridNitroNetworkInfo: HybridNitroNetworkInfoSpec(), NetworkInfoDelegate {
    private val context = NitroModules.applicationContext ?: throw Exception("Context is null")
    private val nitroNetworkInfoImpl = NitroNetworkInfoImpl(context, this)
    private val listeners = ConcurrentHashMap<Long, (NitroNetworkStatusInfo) -> Unit>()
    private val nextListenerId = AtomicLong(0)

    override val isConnected: Boolean
        get() = nitroNetworkInfoImpl.getIsConnection()
    override val connectionType: ConnectionType
        get() = nitroNetworkInfoImpl.currentStatus().connectionType

    /**
     * Adds a listener beside any others; the function returned removes only
     * this one. Monitoring runs while at least one listener is registered.
     */
    override fun addListener(listener: (NitroNetworkStatusInfo) -> Unit): () -> Unit {
        val id = nextListenerId.getAndIncrement()
        synchronized(listeners) {
            listeners[id] = listener
            nitroNetworkInfoImpl.start()
        }
        listener(nitroNetworkInfoImpl.currentStatus())
        return {
            synchronized(listeners) {
                if (listeners.remove(id) != null && listeners.isEmpty()) {
                    nitroNetworkInfoImpl.stop()
                }
            }
        }
    }

    override fun onNetworkInfoChanged(info: NitroNetworkStatusInfo) {
        listeners.values.forEach { it(info) }
    }
}
