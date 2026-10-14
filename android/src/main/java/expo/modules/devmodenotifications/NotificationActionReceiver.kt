package expo.modules.devmodenotifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val notificationId = intent.getStringExtra("notificationId") ?: return
    val actionId = intent.getStringExtra("actionId") ?: return
    val payload = intent.extras?.keySet()
      ?.filter { it.startsWith("payload.") }
      ?.associate { it.removePrefix("payload.") to intent.getStringExtra(it).orEmpty() }
      ?: emptyMap()
    NotificationEventStore.recordAction(context, notificationId, actionId, payload)
  }
}
