
export type ConnectionType = 'unknown' | 'ethernet' | 'wifi' | 'cellular'

export type NitroNetworkStatusInfo = {
    isConnected: boolean
    connectionType: ConnectionType
}


export type NetworkInfoListener = (networkInfo: NitroNetworkStatusInfo) => void