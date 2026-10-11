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
        get() = nitroNetworkInfoImpl.status.isConnected
    override val connectionType: ConnectionType
        get() = nitroNetworkInfoImpl.status.connectionType

    /**
     * Adds a listener beside any others; the function returned removes only
     * this one. The listener is called once with the current status, then on
     * every change.
     */
    override fun addListener(listener: (NitroNetworkStatusInfo) -> Unit): () -> Unit {
        val id = nextListenerId.getAndIncrement()
        listeners[id] = listener
        listener(nitroNetworkInfoImpl.status)
        return { listeners.remove(id) }
    }

    override fun onNetworkInfoChanged(info: NitroNetworkStatusInfo) {
        listeners.values.forEach { it(info) }
    }

    override fun dispose() {
        nitroNetworkInfoImpl.dispose()
        super.dispose()
    }
}
