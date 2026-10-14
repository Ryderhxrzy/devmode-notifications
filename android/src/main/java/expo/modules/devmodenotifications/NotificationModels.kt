package expo.modules.devmodenotifications

import org.json.JSONArray
import org.json.JSONObject

internal data class NotificationContent(
  val title: String,
  val body: String?,
  val subtitle: String?,
  val badge: String?,
  val image: String?,
  val progress: Int,
  val progressMax: Int,
  val progressIndeterminate: Boolean
)

internal data class NotificationAppearance(
  val backgroundColor: String?,
  val titleColor: String?,
  val bodyColor: String?,
  val accentColor: String?,
  val smallIcon: String?
)

internal data class NotificationAction(
  val id: String,
  val title: String,
  val type: String,
  val url: String?,
  val payload: Map<String, String>
)

internal data class NotificationSpec(
  val id: String,
  val channelId: String,
  val template: String,
  val content: NotificationContent,
  val appearance: NotificationAppearance,
  val actions: List<NotificationAction>,
  val data: Map<String, String>,
  val autoCancel: Boolean,
  val ongoing: Boolean
)

internal object NotificationSpecParser {
  fun fromMap(source: Map<String, Any?>): NotificationSpec {
    val content = source.map("content")
    val appearance = source.optionalMap("appearance")
    val id = source.requiredString("id")
    val channelId = source.requiredString("channelId")
    val template = source.string("template") ?: "basic"
    require(template in setOf("basic", "hero", "progress")) { "Unsupported template: $template" }

    return NotificationSpec(
      id = id,
      channelId = channelId,
      template = template,
      content = NotificationContent(
        title = content.requiredString("title"),
        body = content.string("body"),
        subtitle = content.string("subtitle"),
        badge = content.string("badge"),
        image = content.string("image"),
        progress = content.number("progress")?.toInt()?.coerceAtLeast(0) ?: 0,
        progressMax = content.number("progressMax")?.toInt()?.coerceAtLeast(1) ?: 100,
        progressIndeterminate = content.bool("progressIndeterminate") ?: false
      ),
      appearance = NotificationAppearance(
        backgroundColor = appearance.string("backgroundColor"),
        titleColor = appearance.string("titleColor"),
        bodyColor = appearance.string("bodyColor"),
        accentColor = appearance.string("accentColor"),
        smallIcon = appearance.string("smallIcon")
      ),
      actions = source.list("actions").take(3).map { raw ->
        val action = raw as? Map<*, *> ?: error("Every action must be an object")
        NotificationAction(
          id = action.requiredString("id"),
          title = action.requiredString("title"),
          type = action.string("type") ?: "openApp",
          url = action.string("url"),
          payload = action.stringMap("payload")
        )
      },
      data = source.stringMap("data"),
      autoCancel = source.bool("autoCancel") ?: true,
      ongoing = source.bool("ongoing") ?: false
    )
  }

  fun fromJson(json: String): NotificationSpec = fromMap(jsonObjectToMap(JSONObject(json)))

  fun toJson(source: Map<String, Any?>): String = JSONObject(source).toString()

  private fun jsonObjectToMap(value: JSONObject): Map<String, Any?> = buildMap {
    value.keys().forEach { key -> put(key, jsonValue(value.get(key))) }
  }

  private fun jsonValue(value: Any?): Any? = when (value) {
    JSONObject.NULL -> null
    is JSONObject -> jsonObjectToMap(value)
    is JSONArray -> (0 until value.length()).map { jsonValue(value.get(it)) }
    else -> value
  }

  private fun Map<*, *>.requiredString(key: String): String =
    string(key)?.takeIf { it.isNotBlank() } ?: error("$key must be a non-empty string")

  private fun Map<*, *>.string(key: String): String? = this[key] as? String
  private fun Map<*, *>.bool(key: String): Boolean? = this[key] as? Boolean
  private fun Map<*, *>.number(key: String): Number? = this[key] as? Number
  private fun Map<*, *>.map(key: String): Map<*, *> = this[key] as? Map<*, *> ?: error("$key must be an object")
  private fun Map<*, *>.optionalMap(key: String): Map<*, *> = this[key] as? Map<*, *> ?: emptyMap<String, Any?>()
  private fun Map<*, *>.list(key: String): List<*> = this[key] as? List<*> ?: emptyList<Any?>()
  private fun Map<*, *>.stringMap(key: String): Map<String, String> =
    optionalMap(key).entries.associate { (mapKey, mapValue) -> mapKey.toString() to mapValue.toString() }
}
