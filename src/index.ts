// Reexport the native module. On web, it will be resolved to DevModeNotificationsModule.web.ts
// and on native platforms to DevModeNotificationsModule.ts
export { default } from './DevModeNotificationsModule';
export * from './DevModeNotifications.types';
