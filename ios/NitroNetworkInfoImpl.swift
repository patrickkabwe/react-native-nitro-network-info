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

/// Watches the device's network path for as long as it exists.
///
/// Monitoring starts at init rather than with the first listener, so the
/// getters are right even when nobody listens. Before the monitor's first
/// answer — a few milliseconds after init — NWPathMonitor knows nothing, and
/// reporting "disconnected" for that moment is wrong on every launch. So a
/// getter read before it waits for it, once; nothing else ever blocks, and
/// every read after it is a lock and a copy.
final class NitroNetworkInfoImpl {
    /// The longest a read before the first path waits. The bound only
    /// matters if the system never answers.
    private static let firstPathTimeout: DispatchTimeInterval = .milliseconds(500)

    weak var delegate: NitroNetworkInfoDelegate?

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "com.nitronetworkinfo.path-monitor")
    private let firstPath = DispatchGroup()
    private let lock = NSLock()
    private var status = NitroNetworkStatusInfo(isConnected: false, connectionType: .unknown)
    private var hasStatus = false

    init() {
        firstPath.enter()
        monitor.pathUpdateHandler = { [weak self] path in
            self?.update(with: path)
        }
        monitor.start(queue: queue)
    }

    deinit {
        monitor.cancel()
    }

    /// The latest status. Safe from any thread; waits only if read before the
    /// monitor's first answer.
    var current: NitroNetworkStatusInfo {
        if let status = currentIfKnown {
            return status
        }
        _ = firstPath.wait(timeout: .now() + Self.firstPathTimeout)
        lock.lock()
        defer { lock.unlock() }
        return status
    }

    /// The latest status, or nil before the monitor's first answer. Never blocks.
    var currentIfKnown: NitroNetworkStatusInfo? {
        lock.lock()
        defer { lock.unlock() }
        return hasStatus ? status : nil
    }

    private func update(with path: NWPath) {
        let next = NitroNetworkStatusInfo(
            isConnected: path.status == .satisfied,
            connectionType: Self.connectionType(of: path)
        )

        lock.lock()
        let isFirst = !hasStatus
        let changed = isFirst
            || next.isConnected != status.isConnected
            || next.connectionType != status.connectionType
        status = next
        hasStatus = true
        lock.unlock()

        if isFirst {
            firstPath.leave()
        }
        // The monitor repeats a path when only details nobody reads moved.
        // The first path is always delivered: a listener added before it was
        // given nothing at registration.
        if changed {
            DispatchQueue.main.async { [weak self] in
                self?.delegate?.nitroNetworkInfoDidChange(next)
            }
        }
    }

    private static func connectionType(of path: NWPath) -> ConnectionType {
        guard path.status == .satisfied else { return .unknown }
        if path.usesInterfaceType(.wifi) { return .wifi }
        if path.usesInterfaceType(.cellular) { return .cellular }
        if path.usesInterfaceType(.wiredEthernet) { return .ethernet }
        return .unknown
    }
}
