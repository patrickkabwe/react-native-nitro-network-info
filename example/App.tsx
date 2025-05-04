import React, {useEffect, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import NitroNetworkInfo, {
  type NitroNetworkStatusInfo,
} from 'react-native-nitro-network-info';

function App(): React.JSX.Element {
  const [networkInfo, setNetworkInfo] = useState<NitroNetworkStatusInfo | null>(
    null,
  );

  useEffect(() => {
    const unsubscribe = NitroNetworkInfo.addListener(networkInfo => {
      setNetworkInfo(networkInfo);
    });

    return () => {
      unsubscribe();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.text}>
        {NitroNetworkInfo?.isConnected ? 'Connected' : 'Disconnected'}
      </Text>
      <Text style={styles.text}>
        {NitroNetworkInfo?.connectionType || 'Unknown'}
      </Text>
      <Text>{JSON.stringify(networkInfo, null, 2)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  text: {
    fontSize: 40,
    color: 'green',
  },
});

export default App;
