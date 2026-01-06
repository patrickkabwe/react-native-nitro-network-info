package com.nitronetworkinfo

import com.margelo.nitro.NitroModules
import com.margelo.nitro.nitronetworkinfo.ConnectionType
import com.margelo.nitro.nitronetworkinfo.HybridNitroNetworkInfoSpec
import com.margelo.nitro.nitronetworkinfo.NitroNetworkStatusInfo


class HybridNitroNetworkInfo: HybridNitroNetworkInfoSpec(), NetworkInfoDelegate {
    private val context = NitroModules.applicationContext ?: throw Exception("Context is null")
    private val nitroNetworkInfoImpl = NitroNetworkInfoImpl(context, this)
    private var networkInfoStatusListener: ((NitroNetworkStatusInfo) -> Unit)? = null

    override val isConnected: Boolean
        get() = nitroNetworkInfoImpl.getIsConnection()
    override val connectionType: ConnectionType
        get() = nitroNetworkInfoImpl.getConnectionType()

    override fun addListener(listener: (NitroNetworkStatusInfo) -> Unit): () -> Unit {
        networkInfoStatusListener = listener
        nitroNetworkInfoImpl.start()
        listener(NitroNetworkStatusInfo(isConnected, connectionType))
        return {
            networkInfoStatusListener = null
            nitroNetworkInfoImpl.stop()
        }
    }

    override fun onNetworkInfoChanged(info: NitroNetworkStatusInfo) {
        networkInfoStatusListener?.invoke(info)
    }
}
