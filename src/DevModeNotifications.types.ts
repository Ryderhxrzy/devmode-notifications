export type NotificationTemplate = 'basic' | 'hero' | 'progress';
export type AndroidNotificationImportance = 'min' | 'low' | 'default' | 'high' | 'max';

export type NotificationContent = {
  title: string;
  body?: string;
  subtitle?: string;
  badge?: string;
  /** An HTTPS URL or an Android drawable resource name. */
  image?: string;
  progress?: number;
  progressMax?: number;
  progressIndeterminate?: boolean;
};

export type NotificationAppearance = {
  backgroundColor?: string;
  titleColor?: string;
  bodyColor?: string;
  accentColor?: string;
  smallIcon?: string;
  largeIcon?: string;
};

export type NotificationAction = {
  id: string;
  title: string;
  type?: 'openApp' | 'background';
  url?: string;
  payload?: Record<string, string>;
};

export type NotificationSpec = {
  id: string;
  channelId: string;
  template?: NotificationTemplate;
  content: NotificationContent;
  appearance?: NotificationAppearance;
  actions?: NotificationAction[];
  data?: Record<string, string>;
  autoCancel?: boolean;
  ongoing?: boolean;
};

export type NotificationChannel = {
  id: string;
  name: string;
  description?: string;
  importance?: AndroidNotificationImportance;
  sound?: 'default' | 'none';
  vibration?: boolean;
  lights?: boolean;
  lightColor?: string;
};

export type NotificationActionEvent = {
  notificationId: string;
  actionId: string;
  payload: Record<string, string>;
};

export type NotificationPermissionStatus = 'granted' | 'denied' | 'undetermined';

export type NotificationPermissionResponse = {
  status: NotificationPermissionStatus;
  granted: boolean;
  canAskAgain: boolean;
  expires: 'never' | number;
};

export type DevModeNotificationsModuleEvents = {
  onNotificationAction: (event: NotificationActionEvent) => void;
  onTokenChanged: (event: { token: string }) => void;
};
