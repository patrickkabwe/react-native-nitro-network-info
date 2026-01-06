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
        nitrogenNetworkInfoImpl.start()
        DispatchQueue.main.async { [weak self] in
            guard let self = self else { return }
            
            self.networkInfoStatusListener?(
                NitroNetworkStatusInfo(
                    isConnected: self.isConnected,
                    connectionType: self.connectionType
                )
            )
            
            self.nitrogenNetworkInfoImpl.delegate = self
        }

        return { [weak self] in
            self?.networkInfoStatusListener = nil
            self?.nitrogenNetworkInfoImpl.stop()
        }
    }
    
    deinit {
        self.nitrogenNetworkInfoImpl.stop()
    }
}

extension HybridNitroNetworkInfo: NitroNetworkInfoDelegate {
    func nitroNetworkInfoDidChange(_ nitroNetworkInfo: NitroNetworkStatusInfo) {
        self.networkInfoStatusListener?(nitroNetworkInfo)
    }
}
