package com.mdfurqan.audioplayer.player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.mdfurqan.audioplayer.R
import com.mdfurqan.audioplayer.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AudioPlayerNotificationManager @Inject constructor(
    @ApplicationContext  val context: Context,
     val exoPlayer: ExoPlayer
) {
    private val notificationManager: NotificationManagerCompat =
        NotificationManagerCompat.from(context)


    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @UnstableApi
    fun startNotificationService(
        mediaSessionService: MediaSessionService,
        mediaSession: MediaSession
    ) {
        buildNotification(mediaSession)
        startForegroundNotificationService(mediaSessionService)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundNotificationService(mediaSessionService: MediaSessionService) {
        val notification = Notification.Builder(
            context,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        mediaSessionService.startForeground(Constants.NOTIFICATION_ID, notification)
    }

    @UnstableApi
    private fun buildNotification(mediaSession: MediaSession) {
        PlayerNotificationManager.Builder(
            context,
            Constants.NOTIFICATION_ID,
            Constants.NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(
                AudioPlayerNotificationAdapter(
                    context = context,
                    pendingIntent = mediaSession.sessionActivity
                )
            )
            .setSmallIconResourceId(R.drawable.baseline_audiotrack_24)
            .build()
            .also {
                it.setMediaSessionToken(mediaSession.sessionCompatToken)
                it.setUseFastForwardActionInCompactView(true)
                it.setUseRewindActionInCompactView(true)
                it.setUseNextActionInCompactView(true)
                it.setPriority(NotificationCompat.PRIORITY_LOW)
                it.setPlayer(exoPlayer)
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
    }
}