import Foundation
import Network


protocol NitroNetworkInfoDelegate: AnyObject {
    func nitroNetworkInfoDidChange(_ nitroNetworkInfo: NitroNetworkStatusInfo)
}

class NitroNetworkInfoImpl {
    var nwPathMonitor = NWPathMonitor()
    weak var delegate: NitroNetworkInfoDelegate?
    var nwPath: NWPath?
    var isConnected: Bool = false {
        didSet {
#if DEBUG
            print("isConnected: \(isConnected)")
#endif
        }
    }
    
    init() {
        nwPathMonitor.start(queue: .global(qos: .background))
        nwPathMonitor.pathUpdateHandler = { path in
            DispatchQueue.main.async { [weak self] in
                guard let self else { return }
                let ni = NitroNetworkStatusInfo(
                    isConnected: path.status == .satisfied,
                    connectionType: self.getConnectionType()
                )
                self.isConnected = path.status == .satisfied
                self.nwPath = path
                self.delegate?.nitroNetworkInfoDidChange(ni)
            }
        }
    }
    
    func getIsConnected() -> Bool {
        return isConnected
    }
    
    func getConnectionType() -> ConnectionType {
        switch true {
        case nwPath?.usesInterfaceType(.cellular):
            return .cellular
        case nwPath?.usesInterfaceType(.wifi):
            return .wifi
        case nwPath?.usesInterfaceType(.wiredEthernet):
            return .ethernet
        default:
            return .unknown
        }
    }
    
    func unregister() {
        self.delegate = nil
        nwPathMonitor.cancel()
    }
}

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
