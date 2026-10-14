import DevModeNotifications from './DevModeNotificationsModule';

export default DevModeNotifications;
export const createChannel = DevModeNotifications.createChannelAsync.bind(DevModeNotifications);
export const displayNotification = DevModeNotifications.displayNotificationAsync.bind(DevModeNotifications);
export const cancelNotification = DevModeNotifications.cancelNotificationAsync.bind(DevModeNotifications);
export const cancelAllNotifications = DevModeNotifications.cancelAllNotificationsAsync.bind(DevModeNotifications);
export const getNotificationPermissionStatus = DevModeNotifications.getPermissionStatusAsync.bind(DevModeNotifications);
export const requestNotificationPermission = DevModeNotifications.requestPermissionAsync.bind(DevModeNotifications);
export const getDevicePushToken = DevModeNotifications.getDevicePushTokenAsync.bind(DevModeNotifications);
export const getPendingNotificationActions = DevModeNotifications.getPendingActionsAsync.bind(DevModeNotifications);
export * from './DevModeNotifications.types';
