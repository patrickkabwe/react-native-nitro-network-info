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
/// getters are right even when nobody listens, and init waits briefly for the
/// monitor's first answer: before it, NWPathMonitor knows nothing, and
/// reporting "disconnected" for that moment is wrong on every launch.
final class NitroNetworkInfoImpl {
    /// How long init waits for the first path. It normally arrives within a
    /// few milliseconds; the bound only matters if the system never answers.
    private static let initialPathTimeout: DispatchTimeInterval = .milliseconds(500)

    weak var delegate: NitroNetworkInfoDelegate?

    private let monitor = NWPathMonitor()
    private let queue = DispatchQueue(label: "com.nitronetworkinfo.path-monitor")
    private let lock = NSLock()
    private var status = NitroNetworkStatusInfo(isConnected: false, connectionType: .unknown)
    private var hasStatus = false

    init() {
        let firstPath = DispatchSemaphore(value: 0)
        monitor.pathUpdateHandler = { [weak self] path in
            guard let self else { return }
            if self.update(with: path) {
                firstPath.signal()
            }
        }
        monitor.start(queue: queue)
        _ = firstPath.wait(timeout: .now() + Self.initialPathTimeout)
    }

    deinit {
        monitor.cancel()
    }

    /// The latest status. Safe from any thread.
    var current: NitroNetworkStatusInfo {
        lock.lock()
        defer { lock.unlock() }
        return status
    }

    /// Stores the path's status and tells the delegate when it changed.
    /// Returns true for the first path, which init is waiting on.
    private func update(with path: NWPath) -> Bool {
        let next = NitroNetworkStatusInfo(
            isConnected: path.status == .satisfied,
            connectionType: Self.connectionType(of: path)
        )

        lock.lock()
        let isFirst = !hasStatus
        let changed = next.isConnected != status.isConnected
            || next.connectionType != status.connectionType
        status = next
        hasStatus = true
        lock.unlock()

        // The first path is the starting state, not a change anybody missed;
        // and the monitor repeats a path when only details nobody reads moved.
        if changed && !isFirst {
            DispatchQueue.main.async { [weak self] in
                self?.delegate?.nitroNetworkInfoDidChange(next)
            }
        }
        return isFirst
    }

    private static func connectionType(of path: NWPath) -> ConnectionType {
        guard path.status == .satisfied else { return .unknown }
        if path.usesInterfaceType(.wifi) { return .wifi }
        if path.usesInterfaceType(.cellular) { return .cellular }
        if path.usesInterfaceType(.wiredEthernet) { return .ethernet }
        return .unknown
    }
}
