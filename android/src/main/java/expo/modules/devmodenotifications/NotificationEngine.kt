package expo.modules.devmodenotifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

internal object NotificationEngine {
  fun createChannel(context: Context, source: Map<String, Any?>) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val id = source["id"] as? String ?: error("id must be a string")
    val name = source["name"] as? String ?: error("name must be a string")
    val importance = when (source["importance"] as? String ?: "default") {
      "min" -> NotificationManager.IMPORTANCE_MIN
      "low" -> NotificationManager.IMPORTANCE_LOW
      "high", "max" -> NotificationManager.IMPORTANCE_HIGH
      else -> NotificationManager.IMPORTANCE_DEFAULT
    }
    val channel = NotificationChannel(id, name, importance).apply {
      description = source["description"] as? String
      enableVibration(source["vibration"] as? Boolean ?: true)
      enableLights(source["lights"] as? Boolean ?: false)
      (source["lightColor"] as? String)?.let { lightColor = parseColor(it, Color.WHITE) }
      if ((source["sound"] as? String) == "none") setSound(null, null)
    }
    context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
  }

  fun display(context: Context, source: Map<String, Any?>) {
    val spec = NotificationSpecParser.fromMap(source)
    display(context, spec, null)
    val image = spec.content.image
    if (image?.startsWith("https://") == true) {
      enqueueImage(context, image, NotificationSpecParser.toJson(source))
    }
  }

  fun display(context: Context, spec: NotificationSpec, imageBitmap: Bitmap?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    val contentView = render(context, spec, imageBitmap)
    val builder = NotificationCompat.Builder(context, spec.channelId)
      .setSmallIcon(resolveDrawable(context, spec.appearance.smallIcon) ?: android.R.drawable.ic_dialog_info)
      .setContentTitle(spec.content.title)
      .setContentText(spec.content.body ?: spec.content.subtitle ?: "")
      .setStyle(NotificationCompat.DecoratedCustomViewStyle())
      .setCustomContentView(contentView)
      .setCustomBigContentView(contentView)
      .setCustomHeadsUpContentView(contentView)
      .setContentIntent(contentIntent(context, spec))
      .setAutoCancel(spec.autoCancel)
      .setOngoing(spec.ongoing)
      .setOnlyAlertOnce(imageBitmap != null)

    spec.appearance.accentColor?.let { builder.color = parseColor(it, Color.TRANSPARENT) }
    spec.actions.forEach { action ->
      builder.addAction(0, action.title, actionIntent(context, spec, action))
    }
    NotificationManagerCompat.from(context).notify(notificationId(spec.id), builder.build())
  }

  fun cancel(context: Context, id: String) = NotificationManagerCompat.from(context).cancel(notificationId(id))
  fun cancelAll(context: Context) = NotificationManagerCompat.from(context).cancelAll()

  private fun render(context: Context, spec: NotificationSpec, imageBitmap: Bitmap?): RemoteViews {
    val layout = when (spec.template) {
      "hero" -> R.layout.devmode_notification_hero
      "progress" -> R.layout.devmode_notification_progress
      else -> R.layout.devmode_notification_basic
    }
    return RemoteViews(context.packageName, layout).apply {
      setTextViewText(R.id.devmode_title, spec.content.title)
      setOptionalText(R.id.devmode_body, spec.content.body)
      setOptionalText(R.id.devmode_subtitle, spec.content.subtitle)
      setOptionalText(R.id.devmode_badge, spec.content.badge)
      setInt(R.id.devmode_root, "setBackgroundColor", parseColor(spec.appearance.backgroundColor, Color.WHITE))
      setTextColor(R.id.devmode_title, parseColor(spec.appearance.titleColor, Color.BLACK))
      setTextColor(R.id.devmode_body, parseColor(spec.appearance.bodyColor, Color.DKGRAY))
      setTextColor(R.id.devmode_subtitle, parseColor(spec.appearance.bodyColor, Color.DKGRAY))
      if (spec.template == "progress") {
        setProgressBar(
          R.id.devmode_progress,
          spec.content.progressMax,
          spec.content.progress.coerceAtMost(spec.content.progressMax),
          spec.content.progressIndeterminate
        )
      }
      if (spec.template == "hero") {
        when {
          imageBitmap != null -> setImageViewBitmap(R.id.devmode_image, imageBitmap)
          spec.content.image?.startsWith("https://") == false -> {
            val drawable = resolveDrawable(context, spec.content.image)
            if (drawable != null) setImageViewResource(R.id.devmode_image, drawable)
          }
          else -> setViewVisibility(R.id.devmode_image, View.GONE)
        }
      }
    }
  }

  private fun RemoteViews.setOptionalText(id: Int, value: String?) {
    setTextViewText(id, value ?: "")
    setViewVisibility(id, if (value.isNullOrBlank()) View.GONE else View.VISIBLE)
  }

  private fun contentIntent(context: Context, spec: NotificationSpec): PendingIntent {
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
      ?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
    intent.putExtra("devmode_notification_id", spec.id)
    spec.data.forEach { (key, value) -> intent.putExtra(key, value) }
    return PendingIntent.getActivity(
      context,
      notificationId(spec.id),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  private fun actionIntent(context: Context, spec: NotificationSpec, action: NotificationAction): PendingIntent {
    val requestCode = notificationId("${spec.id}:${action.id}")
    if (action.type == "openApp") {
      val intent = action.url?.let { Intent(Intent.ACTION_VIEW, Uri.parse(it)).setPackage(context.packageName) }
        ?: context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
      intent.putExtra("devmode_notification_id", spec.id)
      intent.putExtra("devmode_action_id", action.id)
      action.payload.forEach { (key, value) -> intent.putExtra(key, value) }
      return PendingIntent.getActivity(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
    }

    val intent = Intent(context, NotificationActionReceiver::class.java).apply {
      putExtra("notificationId", spec.id)
      putExtra("actionId", action.id)
      action.payload.forEach { (key, value) -> putExtra("payload.$key", value) }
    }
    return PendingIntent.getBroadcast(
      context,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
  }

  fun enqueueImage(context: Context, url: String, specJson: String) {
    val input = Data.Builder().putString("url", url).putString("spec", specJson).build()
    val work = OneTimeWorkRequestBuilder<NotificationImageWorker>().setInputData(input).build()
    WorkManager.getInstance(context).enqueue(work)
  }

  private fun resolveDrawable(context: Context, name: String?): Int? {
    if (name.isNullOrBlank()) return null
    return context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 }
  }

  private fun parseColor(value: String?, fallback: Int): Int = try {
    value?.let(Color::parseColor) ?: fallback
  } catch (_: IllegalArgumentException) {
    fallback
  }

  private fun notificationId(id: String): Int = id.hashCode() and Int.MAX_VALUE
}
