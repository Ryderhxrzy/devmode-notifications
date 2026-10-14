package expo.modules.devmodenotifications

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

internal object NotificationEventStore {
  private const val PREFERENCES = "devmode_notifications"
  private const val PENDING_ACTIONS = "pending_actions"
  private var listener: ((Map<String, Any?>) -> Unit)? = null

  @Synchronized
  fun setListener(value: ((Map<String, Any?>) -> Unit)?) {
    listener = value
  }

  @Synchronized
  fun recordAction(context: Context, notificationId: String, actionId: String, payload: Map<String, String>) {
    val event = mapOf<String, Any?>(
      "notificationId" to notificationId,
      "actionId" to actionId,
      "payload" to payload
    )
    listener?.invoke(event)

    val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    val events = JSONArray(preferences.getString(PENDING_ACTIONS, "[]"))
    events.put(JSONObject(event))
    while (events.length() > 50) events.remove(0)
    preferences.edit().putString(PENDING_ACTIONS, events.toString()).apply()
  }

  @Synchronized
  fun takePendingActions(context: Context): List<Map<String, Any?>> {
    val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    val events = JSONArray(preferences.getString(PENDING_ACTIONS, "[]"))
    preferences.edit().remove(PENDING_ACTIONS).apply()
    return (0 until events.length()).map { index ->
      val event = events.getJSONObject(index)
      val payloadJson = event.optJSONObject("payload") ?: JSONObject()
      val payload = payloadJson.keys().asSequence().associateWith { payloadJson.optString(it) }
      mapOf(
        "notificationId" to event.optString("notificationId"),
        "actionId" to event.optString("actionId"),
        "payload" to payload
      )
    }
  }
}
