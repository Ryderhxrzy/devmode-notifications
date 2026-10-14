import {
  createChannel,
  displayNotification,
  requestNotificationPermission,
} from '@jhayrdev/notifications';
import { useEffect, useState } from 'react';
import { Alert, Button, SafeAreaView, StyleSheet, Text, View } from 'react-native';

export default function App() {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    void (async () => {
      const permission = await requestNotificationPermission();
      await createChannel({
        id: 'demo',
        name: 'Demo notifications',
        description: 'Notifications created by the example app',
        importance: 'high',
      });
      setReady(permission.granted);
    })().catch(error => Alert.alert('Setup failed', String(error)));
  }, []);

  const show = async (template: 'basic' | 'hero' | 'progress') => {
    await displayNotification({
      id: `demo-${template}`,
      channelId: 'demo',
      template,
      content: {
        title: template === 'progress' ? 'Downloading update' : 'DevMode notification',
        subtitle: 'Rendered by Kotlin + RemoteViews',
        body: 'Configured entirely from TypeScript.',
        badge: template.toUpperCase(),
        image: template === 'hero' ? 'https://picsum.photos/800/400' : undefined,
        progress: template === 'progress' ? 68 : undefined,
      },
      appearance: {
        backgroundColor: '#101820',
        titleColor: '#FFFFFF',
        bodyColor: '#D5D9DC',
        accentColor: '#FFB000',
        smallIcon: 'devmode_notification_icon',
      },
      actions: [
        { id: 'open', title: 'Open app', type: 'openApp' },
        { id: 'acknowledge', title: 'Acknowledge', type: 'background', payload: { source: 'example' } },
      ],
    });
  };

  return (
    <SafeAreaView style={styles.screen}>
      <Text style={styles.title}>@jhayrdev/notifications</Text>
      <Text style={styles.status}>{ready ? 'Notification permission granted' : 'Waiting for notification permission'}</Text>
      <View style={styles.buttons}>
        <Button title="Basic notification" onPress={() => void show('basic')} />
        <Button title="Hero notification" onPress={() => void show('hero')} />
        <Button title="Progress notification" onPress={() => void show('progress')} />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: '#eef2f5' },
  title: { fontSize: 25, fontWeight: '700', marginBottom: 8 },
  status: { color: '#44515c', marginBottom: 24 },
  buttons: { gap: 12 },
});
