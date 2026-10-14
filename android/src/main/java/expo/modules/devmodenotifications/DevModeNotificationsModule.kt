package expo.modules.devmodenotifications

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class DevModeNotificationsModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("DevModeNotifications")

    Events("onChange")

    AsyncFunction("setValueAsync") { value: String ->
      sendEvent("onChange", mapOf(
        "value" to value
      ))
    }
  }
}
