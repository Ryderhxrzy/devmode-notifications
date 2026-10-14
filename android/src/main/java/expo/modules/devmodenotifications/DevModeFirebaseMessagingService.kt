package expo.modules.devmodenotifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class DevModeFirebaseMessagingService : FirebaseMessagingService() {
  override fun onMessageReceived(message: RemoteMessage) {
    val specJson = message.data["devmode_spec"] ?: return
    try {
      val spec = NotificationSpecParser.fromJson(specJson)
      NotificationEngine.display(applicationContext, spec, null)
      val image = spec.content.image
      if (image?.startsWith("https://") == true) {
        NotificationEngine.enqueueImage(applicationContext, image, specJson)
      }
    } catch (_: Exception) {
      // Ignore payloads that do not conform to the versioned package schema.
    }
  }

  override fun onNewToken(token: String) {
    getSharedPreferences("devmode_notifications", MODE_PRIVATE)
      .edit().putString("fcm_token", token).apply()
  }
}
