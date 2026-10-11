//
//  HybridNitroNetworkInfo.swift
//  NitroNetworkInfo
//
//  Created by Patrick Kabwe on 04/05/2025.
//

import Foundation

typealias NetworkInfoListener = (NitroNetworkStatusInfo) -> Void

class HybridNitroNetworkInfo: HybridNitroNetworkInfoSpec {
    private let nitrogenNetworkInfoImpl = NitroNetworkInfoImpl()
    private let lock = NSLock()
    private var listeners: [UInt64: NetworkInfoListener] = [:]
    private var nextListenerId: UInt64 = 0

    override init() {
        super.init()
        nitrogenNetworkInfoImpl.delegate = self
    }

    var isConnected: Bool {
        nitrogenNetworkInfoImpl.current.isConnected
    }

    var connectionType: ConnectionType {
        nitrogenNetworkInfoImpl.current.connectionType
    }

    /// Adds a listener beside any others; the function returned removes only
    /// this one. The listener is called once with the current status, then on
    /// every change.
    func addListener(listener: @escaping NetworkInfoListener) throws -> () -> Void {
        lock.lock()
        let id = nextListenerId
        nextListenerId += 1
        listeners[id] = listener
        lock.unlock()

        // On main, behind any change already queued there, so the listener
        // never sees an older status after a newer one. Before the monitor's
        // first answer there is nothing to give, and nothing is waited for:
        // the first path is delivered to every listener registered by then.
        DispatchQueue.main.async { [weak self] in
            guard let self, self.isListening(id),
                  let status = self.nitrogenNetworkInfoImpl.currentIfKnown else { return }
            listener(status)
        }

        return { [weak self] in
            guard let self else { return }
            self.lock.lock()
            self.listeners.removeValue(forKey: id)
            self.lock.unlock()
        }
    }

    private func isListening(_ id: UInt64) -> Bool {
        lock.lock()
        defer { lock.unlock() }
        return listeners[id] != nil
    }
}

extension HybridNitroNetworkInfo: NitroNetworkInfoDelegate {
    func nitroNetworkInfoDidChange(_ nitroNetworkInfo: NitroNetworkStatusInfo) {
        lock.lock()
        let current = Array(listeners.values)
        lock.unlock()
        current.forEach { $0(nitroNetworkInfo) }
    }
}
