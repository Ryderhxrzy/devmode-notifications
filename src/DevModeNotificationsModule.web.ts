import { NativeModule, registerWebModule } from 'expo';

import type { DevModeNotificationsModuleEvents } from './DevModeNotifications.types';

class DevModeNotificationsModule extends NativeModule<DevModeNotificationsModuleEvents> {
  private unsupported(): never {
    throw new Error('@devmode/notifications is Android-only.');
  }

  async createChannelAsync(): Promise<void> { this.unsupported(); }
  async displayNotificationAsync(): Promise<void> { this.unsupported(); }
  async cancelNotificationAsync(): Promise<void> { this.unsupported(); }
  async cancelAllNotificationsAsync(): Promise<void> { this.unsupported(); }
  async getPermissionStatusAsync() { return { status: 'denied' as const, granted: false, canAskAgain: false, expires: 'never' as const }; }
  async requestPermissionAsync() { return { status: 'denied' as const, granted: false, canAskAgain: false, expires: 'never' as const }; }
  async getDevicePushTokenAsync(): Promise<string> { this.unsupported(); }
  async getPendingActionsAsync(): Promise<never[]> { return []; }
}

export default registerWebModule(DevModeNotificationsModule, 'DevModeNotificationsModule');
