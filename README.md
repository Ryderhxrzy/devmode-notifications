# @jhayrdev/notifications

Android-first custom notifications for React Native and Expo, rendered natively with Kotlin, `RemoteViews`, AndroidX notifications, and Firebase Cloud Messaging.

## Current features

- `basic`, `hero`, and `progress` templates
- Custom text, colors, drawable icons, remote hero images, and progress
- Android notification channels and Android 13 permission helpers
- Open-app/deep-link and background actions
- Action recovery after a cold start with `getPendingNotificationActions()`
- Data-only FCM handling while the app is foregrounded, backgrounded, or terminated
- Expo config plugin and setup/doctor CLI
- Expo development-build example

This package is Android-only. It requires a native/development build and does not run in Expo Go.

## Install and configure

```bash
npm install @jhayrdev/notifications
npx devmode-notifications setup
```

Configure Firebase in `app.json`:

```json
{
  "expo": {
    "android": {
      "package": "com.example.app",
      "googleServicesFile": "./google-services.json"
    },
    "plugins": [
      [
        "@jhayrdev/notifications",
        { "defaultIcon": "devmode_notification_icon" }
      ]
    ]
  }
}
```

Then create a development build:

```bash
npx expo prebuild
npx expo run:android
```

## Display a notification

```ts
import {
  createChannel,
  displayNotification,
  requestNotificationPermission,
} from '@jhayrdev/notifications';

await requestNotificationPermission();
await createChannel({ id: 'orders', name: 'Orders', importance: 'high' });

await displayNotification({
  id: 'order-1042',
  channelId: 'orders',
  template: 'hero',
  content: {
    title: 'Order ready',
    body: 'Your order is ready for pickup.',
    image: 'https://example.com/order.png'
  },
  appearance: {
    backgroundColor: '#101820',
    titleColor: '#FFFFFF',
    bodyColor: '#D5D9DC',
    accentColor: '#FFB000'
  },
  actions: [
    { id: 'open', title: 'View order', type: 'openApp', url: 'myapp://orders/1042' },
    { id: 'dismiss', title: 'Dismiss', type: 'background' }
  ]
});
```

## FCM payload

Use a data-only Android message. A `notification` payload is rendered by Android/FCM in the background and bypasses this package's custom renderer.

```json
{
  "message": {
    "token": "FCM_DEVICE_TOKEN",
    "data": {
      "devmode_version": "1",
      "devmode_spec": "{\"id\":\"order-1042\",\"channelId\":\"orders\",\"template\":\"basic\",\"content\":{\"title\":\"Order ready\",\"body\":\"Tap to view\"}}"
    },
    "android": { "priority": "high" }
  }
}
```

Keep the entire FCM payload under FCM's size limit. Remote images are downloaded by WorkManager and update the already-visible notification when ready.

## Android limitations

Templates use precompiled native XML layouts. Arbitrary JSX, runtime XML, pixel-perfect absolute positioning, and identical rendering across OEM devices are intentionally unsupported. Android 12 and newer wrap custom content in system-controlled notification chrome.

Only one component should own `FirebaseMessagingService` dispatch in an application. If an app already uses another FCM-handling library, resolve the service ownership before enabling automatic background rendering.
