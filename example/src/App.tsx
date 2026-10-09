import { useState } from 'react';
import { Pressable, StatusBar, StyleSheet, Text, View } from 'react-native';
import { SafeAreaProvider, SafeAreaView } from 'react-native-safe-area-context';
import { StoryEditor } from './StoryEditor';
import { Recipes } from './Recipes';

type Screen = 'story' | 'recipes';

const TABS: { key: Screen; label: string }[] = [
  { key: 'story', label: 'Story' },
  { key: 'recipes', label: 'Recipes' },
];

export default function App() {
  const [screen, setScreen] = useState<Screen>('story');

  return (
    <SafeAreaProvider>
      <StatusBar barStyle="light-content" />
      <View style={styles.root}>
        {screen === 'story' ? <StoryEditor /> : <Recipes />}
        <SafeAreaView
          edges={['top']}
          style={styles.tabsOverlay}
          pointerEvents="box-none"
        >
          <View style={styles.tabs}>
            {TABS.map((tab) => {
              const active = tab.key === screen;
              return (
                <Pressable
                  key={tab.key}
                  testID={`tab-${tab.key}`}
                  onPress={() => setScreen(tab.key)}
                  style={[styles.tab, active && styles.tabActive]}
                >
                  <Text
                    style={[styles.tabText, active && styles.tabTextActive]}
                  >
                    {tab.label}
                  </Text>
                </Pressable>
              );
            })}
          </View>
        </SafeAreaView>
      </View>
    </SafeAreaProvider>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#0E0B16',
  },
  tabsOverlay: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    alignItems: 'center',
  },
  tabs: {
    flexDirection: 'row',
    marginTop: 8,
    padding: 3,
    borderRadius: 999,
    backgroundColor: 'rgba(0, 0, 0, 0.35)',
  },
  tab: {
    paddingHorizontal: 16,
    paddingVertical: 6,
    borderRadius: 999,
  },
  tabActive: {
    backgroundColor: '#FFFFFF',
  },
  tabText: {
    color: 'rgba(255, 255, 255, 0.8)',
    fontSize: 13,
    fontWeight: '600',
  },
  tabTextActive: {
    color: '#111111',
  },
});
