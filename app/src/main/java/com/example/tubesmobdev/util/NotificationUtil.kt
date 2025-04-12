import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.tubesmobdev.R

object NotificationUtil {
    fun createForegroundNotification(context: Context): Notification {
        val channelId = "token_refresh_channel"
        val channelName = "Token Refresh"

        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(chan)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Token Refresh Running")
            .setContentText("Refreshing token periodically")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
