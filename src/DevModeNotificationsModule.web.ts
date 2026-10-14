import { registerWebModule, NativeModule } from 'expo';

import { DevModeNotificationsModuleEvents } from './DevModeNotifications.types';

// DevModeNotificationsModule is not available on the web platform.
class DevModeNotificationsModule extends NativeModule<DevModeNotificationsModuleEvents> {}

export default registerWebModule(DevModeNotificationsModule, 'DevModeNotificationsModule');
