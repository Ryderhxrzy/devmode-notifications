import { NativeModule, requireNativeModule } from 'expo';

import { DevModeNotificationsModuleEvents } from './DevModeNotifications.types';

declare class DevModeNotificationsModule extends NativeModule<DevModeNotificationsModuleEvents> {
  setValueAsync(value: string): Promise<void>;
}

export default requireNativeModule<DevModeNotificationsModule>('DevModeNotifications');
