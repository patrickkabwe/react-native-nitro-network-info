import { type HybridObject } from 'react-native-nitro-modules'
import type { ConnectionType, NetworkInfoListener } from '../types'

export interface NitroNetworkInfo extends HybridObject<{ ios: 'swift', android: 'kotlin' }> {
    readonly isConnected: boolean
    readonly connectionType: ConnectionType
    addListener(listener: NetworkInfoListener): () => void
}