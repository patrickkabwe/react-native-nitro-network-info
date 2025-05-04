//
//  HybridNitroNetworkInfo.swift
//  NitroNetworkInfo
//
//  Created by Patrick Kabwe on 04/05/2025.
//

import Foundation

typealias NetworkInfoListener = (NitroNetworkStatusInfo) -> Void

class HybridNitroNetworkInfo: HybridNitroNetworkInfoSpec {
    private var nitrogenNetworkInfoImpl = NitroNetworkInfoImpl()
    private var networkInfoStatusListener: ((NitroNetworkStatusInfo) -> Void)? = nil

    var isConnected: Bool {
        get { nitrogenNetworkInfoImpl.getIsConnected() }
    }
    
    var connectionType: ConnectionType {
        get { nitrogenNetworkInfoImpl.getConnectionType() }
    }
    
    func addListener(listener: @escaping (NitroNetworkStatusInfo) -> Void) throws -> () -> Void {
        networkInfoStatusListener = listener
        
        DispatchQueue.main.async { [unowned self] in
            self.networkInfoStatusListener?(
                NitroNetworkStatusInfo(
                    isConnected: isConnected,
                    connectionType: connectionType
                )
            )
            
            self.nitrogenNetworkInfoImpl.delegate = self
        }

        return { [weak self] in
            self?.networkInfoStatusListener = nil
        }
    }
    
    deinit {
        self.nitrogenNetworkInfoImpl.unregister()
    }
}

extension HybridNitroNetworkInfo: NitroNetworkInfoDelegate {
    func nitroNetworkInfoDidChange(_ nitroNetworkInfo: NitroNetworkStatusInfo) {
        self.networkInfoStatusListener?(nitroNetworkInfo)
    }
}
