import { NativeModule, requireNativeModule } from 'expo';

import type {
  DevModeNotificationsModuleEvents,
  NotificationActionEvent,
  NotificationChannel,
  NotificationPermissionResponse,
  NotificationSpec,
} from './DevModeNotifications.types';

declare class DevModeNotificationsModule extends NativeModule<DevModeNotificationsModuleEvents> {
  createChannelAsync(channel: NotificationChannel): Promise<void>;
  displayNotificationAsync(notification: NotificationSpec): Promise<void>;
  cancelNotificationAsync(id: string): Promise<void>;
  cancelAllNotificationsAsync(): Promise<void>;
  getPermissionStatusAsync(): Promise<NotificationPermissionResponse>;
  requestPermissionAsync(): Promise<NotificationPermissionResponse>;
  getDevicePushTokenAsync(): Promise<string>;
  getPendingActionsAsync(): Promise<NotificationActionEvent[]>;
}

export default requireNativeModule<DevModeNotificationsModule>('DevModeNotifications');
