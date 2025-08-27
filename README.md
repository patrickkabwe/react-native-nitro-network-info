# react-native-nitro-network-info

A React Native library for monitoring network connectivity status and connection type using Nitro Modules for high performance.

## Features

- 🚀 High performance with Nitro Modules
- 📡 Real-time network status monitoring
- 🔄 Connection type detection (WiFi, Cellular, etc.)
- 📱 Cross-platform support (iOS & Android)
- 🎣 React hooks friendly with listeners

## Requirements

- React Native v0.76.0 or higher
- Node 18.0.0 or higher

## Installation

```bash
bun add react-native-nitro-network-info react-native-nitro-modules@0.28.1
```

## Usage

### Basic Usage

```typescript
import React, { useEffect, useState } from 'react';
import { View, Text } from 'react-native';
import NitroNetworkInfo, {
  type NitroNetworkStatusInfo
} from 'react-native-nitro-network-info';

function App() {
  const [networkInfo, setNetworkInfo] = useState<NitroNetworkStatusInfo | null>(null);

  useEffect(() => {
    // Add listener for network changes
    const unsubscribe = NitroNetworkInfo.addListener((networkInfo) => {
      setNetworkInfo(networkInfo);
    });

    // Cleanup listener on unmount
    return () => {
      unsubscribe();
    };
  }, []);

  return (
    <View>
      <Text>
        Status: {NitroNetworkInfo.isConnected ? 'Connected' : 'Disconnected'}
      </Text>
      <Text>
        Type: {NitroNetworkInfo.connectionType || 'Unknown'}
      </Text>
      <Text>
        Network Info: {JSON.stringify(networkInfo, null, 2)}
      </Text>
    </View>
  );
}
```

## API Reference

### Properties

#### `isConnected: boolean`

A readonly property that indicates whether the device is currently connected to a network.

```typescript
const isConnected = NitroNetworkInfo.isConnected
```

#### `connectionType: ConnectionType`

A readonly property that returns the current connection type (e.g., 'wifi', 'cellular', 'ethernet', etc.).

```typescript
const connectionType = NitroNetworkInfo.connectionType
```

### Methods

#### `addListener(listener: NetworkInfoListener): () => void`

Adds a listener for network status changes. Returns a function to unsubscribe the listener.

**Parameters:**

- `listener: NetworkInfoListener` - Callback function that receives network status updates

**Returns:**

- `() => void` - Unsubscribe function

```typescript
const unsubscribe = NitroNetworkInfo.addListener((networkInfo) => {
  console.log('Network status changed:', networkInfo)
})

// Later, when you want to stop listening
unsubscribe()
```

### Types

| Type                     | Description                                                                                                                                                             |
| ------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `NetworkInfoListener`    | Callback function type that receives network status updates. Signature: `(networkInfo: NitroNetworkStatusInfo) => void`                                                 |
| `ConnectionType`         | Enum or string type representing the available connection types (e.g., 'wifi', 'cellular', 'ethernet'). Exact values depend on platform implementation.                 |
| `NitroNetworkStatusInfo` | Object containing detailed network status information passed to listeners. Structure depends on platform implementation and includes connection state and type details. |

## Credits

Bootstrapped with [create-nitro-module](https://github.com/patrickkabwe/create-nitro-module).

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
