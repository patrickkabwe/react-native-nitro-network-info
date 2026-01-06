import React, { useEffect, useState } from 'react';
import {
  Animated,
  Pressable,
  StatusBar,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import NitroNetworkInfo, {
  type NitroNetworkStatusInfo,
} from 'react-native-nitro-network-info';
import { SafeAreaView } from 'react-native-safe-area-context';

const CONNECTION_ICONS: Record<string, string> = {
  wifi: '📶',
  cellular: '📱',
  ethernet: '🔌',
  unknown: '❓',
};

const NetworkCard = ({
  networkInfo,
}: {
  networkInfo: NitroNetworkStatusInfo | null;
}) => {
  const isConnected = networkInfo?.isConnected ?? false;
  const connectionType = networkInfo?.connectionType ?? 'unknown';

  return (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.cardTitle}>Network Status</Text>
        <View
          style={[
            styles.statusDot,
            isConnected ? styles.statusConnected : styles.statusDisconnected,
          ]}
        />
      </View>

      <View style={styles.statsContainer}>
        <View style={styles.statItem}>
          <Text style={styles.statLabel}>Connection</Text>
          <View style={styles.statusBadge}>
            <Text
              style={[
                styles.statusText,
                isConnected ? styles.connectedText : styles.disconnectedText,
              ]}>
              {isConnected ? 'Connected' : 'Disconnected'}
            </Text>
          </View>
        </View>

        <View style={styles.divider} />

        <View style={styles.statItem}>
          <Text style={styles.statLabel}>Type</Text>
          <View style={styles.typeContainer}>
            <Text style={styles.typeIcon}>
              {CONNECTION_ICONS[connectionType]}
            </Text>
            <Text style={styles.typeText}>
              {connectionType.charAt(0).toUpperCase() + connectionType.slice(1)}
            </Text>
          </View>
        </View>
      </View>
    </View>
  );
};

const RegisterListener = () => {
  const [networkInfo, setNetworkInfo] =
    useState<NitroNetworkStatusInfo | null>(null);

  useEffect(() => {
    const unsubscribe = NitroNetworkInfo.addListener(info => {
      setNetworkInfo(info);
    });

    return unsubscribe;
  }, []);

  return <NetworkCard networkInfo={networkInfo} />;
};

const ToggleButton = ({
  isActive,
  onPress,
}: {
  isActive: boolean;
  onPress: () => void;
}) => {
  const scaleAnim = React.useRef(new Animated.Value(1)).current;

  const handlePressIn = () => {
    Animated.spring(scaleAnim, {
      toValue: 0.95,
      useNativeDriver: true,
    }).start();
  };

  const handlePressOut = () => {
    Animated.spring(scaleAnim, {
      toValue: 1,
      friction: 3,
      tension: 40,
      useNativeDriver: true,
    }).start();
  };

  return (
    <Pressable
      onPress={onPress}
      onPressIn={handlePressIn}
      onPressOut={handlePressOut}>
      <Animated.View
        style={[
          styles.toggleButton,
          isActive ? styles.toggleButtonActive : styles.toggleButtonInactive,
          { transform: [{ scale: scaleAnim }] },
        ]}>
        <Text style={styles.toggleIcon}>{isActive ? '🔔' : '🔕'}</Text>
        <Text style={styles.toggleText}>
          {isActive ? 'Listening...' : 'Start Monitoring'}
        </Text>
      </Animated.View>
    </Pressable>
  );
};

function App(): React.JSX.Element {
  const [showRegisterListener, setShowRegisterListener] = useState(false);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" />

      <View style={styles.header}>
        <Text style={styles.headerEmoji}>🌐</Text>
        <Text style={styles.headerTitle}>Nitro Network Info</Text>
        <Text style={styles.headerSubtitle}>
          Real-time network monitoring
        </Text>
      </View>

      <View style={styles.content}>
        {showRegisterListener ? (
          <RegisterListener />
        ) : (
          <View style={styles.placeholder}>
            <Text style={styles.placeholderIcon}>📡</Text>
            <Text style={styles.placeholderText}>
              Tap the button below to start monitoring your network connection
            </Text>
          </View>
        )}
      </View>

      <View style={styles.footer}>
        <ToggleButton
          isActive={showRegisterListener}
          onPress={() => setShowRegisterListener(!showRegisterListener)}
        />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0f0f23',
  },
  header: {
    paddingTop: 40,
    paddingBottom: 30,
    alignItems: 'center',
  },
  headerEmoji: {
    fontSize: 48,
    marginBottom: 12,
  },
  headerTitle: {
    fontSize: 28,
    fontWeight: '700',
    color: '#ffffff',
    letterSpacing: -0.5,
  },
  headerSubtitle: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 6,
    letterSpacing: 0.5,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
    justifyContent: 'center',
  },
  card: {
    backgroundColor: '#1a1a2e',
    borderRadius: 20,
    padding: 24,
    borderWidth: 1,
    borderColor: '#2d2d44',
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#ffffff',
  },
  statusDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  statusConnected: {
    backgroundColor: '#10b981',
    shadowColor: '#10b981',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.8,
    shadowRadius: 8,
  },
  statusDisconnected: {
    backgroundColor: '#ef4444',
    shadowColor: '#ef4444',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.8,
    shadowRadius: 8,
  },
  statsContainer: {
    gap: 20,
  },
  statItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  statLabel: {
    fontSize: 15,
    color: '#9ca3af',
    fontWeight: '500',
  },
  statusBadge: {
    paddingHorizontal: 14,
    paddingVertical: 6,
    borderRadius: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
  },
  connectedText: {
    color: '#10b981',
  },
  disconnectedText: {
    color: '#ef4444',
  },
  divider: {
    height: 1,
    backgroundColor: '#2d2d44',
  },
  typeContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  typeIcon: {
    fontSize: 18,
  },
  typeText: {
    fontSize: 15,
    color: '#ffffff',
    fontWeight: '500',
  },
  placeholder: {
    alignItems: 'center',
    paddingHorizontal: 40,
  },
  placeholderIcon: {
    fontSize: 64,
    marginBottom: 20,
    opacity: 0.6,
  },
  placeholderText: {
    fontSize: 16,
    color: '#6b7280',
    textAlign: 'center',
    lineHeight: 24,
  },
  footer: {
    padding: 20,
    paddingBottom: 40,
  },
  toggleButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 18,
    borderRadius: 16,
    gap: 10,
  },
  toggleButtonActive: {
    backgroundColor: '#1e3a5f',
    borderWidth: 1,
    borderColor: '#3b82f6',
  },
  toggleButtonInactive: {
    backgroundColor: '#3b82f6',
  },
  toggleIcon: {
    fontSize: 20,
  },
  toggleText: {
    fontSize: 17,
    fontWeight: '600',
    color: '#ffffff',
  },
});

export default App;
