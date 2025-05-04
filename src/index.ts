import { NitroModules } from 'react-native-nitro-modules'
import type { NitroNetworkInfo as NitroNetworkInfoSpec } from './specs/nitro-network-info.nitro'
export type * from './types'

const NitroNetworkInfo =
    NitroModules.createHybridObject<NitroNetworkInfoSpec>('NitroNetworkInfo')

export default NitroNetworkInfo
