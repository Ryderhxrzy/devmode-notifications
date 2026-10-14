package expo.modules.devmodenotifications

import android.Manifest
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import expo.modules.interfaces.permissions.Permissions
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class DevModeNotificationsModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("DevModeNotifications")

    Events("onNotificationAction", "onTokenChanged")

    OnCreate {
      NotificationEventStore.setListener { event -> sendEvent("onNotificationAction", event) }
    }

    OnDestroy {
      NotificationEventStore.setListener(null)
    }

    AsyncFunction("createChannelAsync") { channel: Map<String, Any?> ->
      NotificationEngine.createChannel(appContext.applicationContext, channel)
    }

    AsyncFunction("displayNotificationAsync") { notification: Map<String, Any?> ->
      NotificationEngine.display(appContext.applicationContext, notification)
    }

    AsyncFunction("cancelNotificationAsync") { id: String ->
      NotificationEngine.cancel(appContext.applicationContext, id)
    }

    AsyncFunction("cancelAllNotificationsAsync") {
      NotificationEngine.cancelAll(appContext.applicationContext)
    }

    AsyncFunction("getPermissionStatusAsync") { promise: Promise ->
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        promise.resolve(grantedPermissionResponse())
      } else {
        Permissions.getPermissionsWithPermissionsManager(
          appContext.permissions,
          promise,
          Manifest.permission.POST_NOTIFICATIONS
        )
      }
    }

    AsyncFunction("requestPermissionAsync") { promise: Promise ->
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        promise.resolve(grantedPermissionResponse())
      } else {
        if (appContext.permissions == null) throw Exceptions.PermissionsModuleNotFound()
        Permissions.askForPermissionsWithPermissionsManager(
          appContext.permissions,
          promise,
          Manifest.permission.POST_NOTIFICATIONS
        )
      }
    }

    AsyncFunction("getDevicePushTokenAsync") { promise: Promise ->
      FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token -> promise.resolve(token) }
        .addOnFailureListener { error -> promise.reject("ERR_FCM_TOKEN", error.message, error) }
    }

    AsyncFunction("getPendingActionsAsync") {
      NotificationEventStore.takePendingActions(appContext.applicationContext)
    }
  }

  private fun grantedPermissionResponse() = mapOf(
    "status" to "granted",
    "granted" to true,
    "canAskAgain" to true,
    "expires" to "never"
  )
}
