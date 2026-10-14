const { AndroidConfig, withAndroidManifest } = require('expo/config-plugins');

function upsertMetaData(application, name, value) {
  application['meta-data'] = application['meta-data'] || [];
  const existing = application['meta-data'].find(item => item.$?.['android:name'] === name);
  const attributes = { 'android:name': name, 'android:resource': value };
  if (existing) existing.$ = attributes;
  else application['meta-data'].push({ $: attributes });
}

function withDevModeNotifications(config, options = {}) {
  if (options.googleServicesFile) {
    config.android = config.android || {};
    config.android.googleServicesFile = options.googleServicesFile;
  }

  return withAndroidManifest(config, modConfig => {
    const manifest = modConfig.modResults.manifest;
    manifest['uses-permission'] = manifest['uses-permission'] || [];
    if (!manifest['uses-permission'].some(item => item.$?.['android:name'] === 'android.permission.POST_NOTIFICATIONS')) {
      manifest['uses-permission'].push({ $: { 'android:name': 'android.permission.POST_NOTIFICATIONS' } });
    }

    const application = AndroidConfig.Manifest.getMainApplicationOrThrow(modConfig.modResults);
    upsertMetaData(
      application,
      'com.google.firebase.messaging.default_notification_icon',
      `@drawable/${options.defaultIcon || 'devmode_notification_icon'}`
    );
    if (options.defaultColor) {
      application['meta-data'] = application['meta-data'] || [];
      const name = 'com.google.firebase.messaging.default_notification_color';
      const existing = application['meta-data'].find(item => item.$?.['android:name'] === name);
      const resource = options.defaultColor.startsWith('@color/') ? options.defaultColor : `@color/${options.defaultColor}`;
      const attributes = { 'android:name': name, 'android:resource': resource };
      if (existing) existing.$ = attributes;
      else application['meta-data'].push({ $: attributes });
    }
    return modConfig;
  });
}

module.exports = withDevModeNotifications;
