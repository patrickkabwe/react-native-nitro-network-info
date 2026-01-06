//
//  NitroNetworkInfoImpl.swift
//  NitroNetworkInfo
//
//  Created by Patrick Kabwe on 04/05/2025.
//

import Foundation
import Network

protocol NitroNetworkInfoDelegate: AnyObject {
    func nitroNetworkInfoDidChange(_ nitroNetworkInfo: NitroNetworkStatusInfo)
}

class NitroNetworkInfoImpl {
    var nwPathMonitor: NWPathMonitor? = nil
    weak var delegate: NitroNetworkInfoDelegate?
    var nwPath: NWPath?
    private(set) var connectionType: ConnectionType = .unknown
    private(set) var isConnected: Bool = false {
        didSet {
#if DEBUG
            print("isConnected: \(isConnected)")
#endif
        }
    }
    
    func start() {
        nwPathMonitor = NWPathMonitor()
        
        nwPathMonitor?.pathUpdateHandler = { [weak self] path in
            DispatchQueue.main.async {
                guard let self else { return }

                self.nwPath = path
                self.isConnected = (path.status == .satisfied)

                if path.usesInterfaceType(.cellular) { self.connectionType = .cellular }
                else if path.usesInterfaceType(.wifi) { self.connectionType = .wifi }
                else if path.usesInterfaceType(.wiredEthernet) { self.connectionType = .ethernet }
                else { self.connectionType = .unknown }

                self.delegate?.nitroNetworkInfoDidChange(
                    NitroNetworkStatusInfo(isConnected: self.isConnected, connectionType: self.connectionType)
                )
            }
        }
        nwPathMonitor?.start(queue: .global(qos: .background))
    }
    
    func getIsConnected() -> Bool {
        return isConnected
    }
    
    func getConnectionType() -> ConnectionType {
        return connectionType
    }
    
    func stop() {
        nwPathMonitor?.cancel()
        self.delegate = nil
    }
}
