package expo.modules.devmodenotifications

import android.content.Context
import android.graphics.BitmapFactory
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class NotificationImageWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    val imageUrl = inputData.getString("url") ?: return@withContext Result.failure()
    val specJson = inputData.getString("spec") ?: return@withContext Result.failure()
    try {
      val connection = URL(imageUrl).openConnection() as HttpURLConnection
      connection.connectTimeout = 8_000
      connection.readTimeout = 12_000
      connection.instanceFollowRedirects = true
      connection.inputStream.use { stream ->
        val bitmap = BitmapFactory.decodeStream(stream) ?: return@withContext Result.failure()
        NotificationEngine.display(applicationContext, NotificationSpecParser.fromJson(specJson), bitmap)
      }
      connection.disconnect()
      Result.success()
    } catch (_: Exception) {
      if (runAttemptCount < 2) Result.retry() else Result.failure()
    }
  }
}
