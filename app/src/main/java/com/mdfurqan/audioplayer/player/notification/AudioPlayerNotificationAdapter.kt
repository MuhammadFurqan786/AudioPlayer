package com.mdfurqan.audioplayer.player.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

@UnstableApi
class AudioPlayerNotificationAdapter(
    private val context: Context,
    private val pendingIntent: PendingIntent?
) : PlayerNotificationManager.MediaDescriptionAdapter {
    /**
     * Gets the content title for the current media item.
     *
     *
     * See [NotificationCompat.Builder.setContentTitle].
     *
     * @param player The [Player] for which a notification is being built.
     * @return The content title for the current media item.
     */
    override fun getCurrentContentTitle(player: Player): CharSequence =
        player.mediaMetadata.albumTitle ?: "Unknown"

    /**
     * Creates a content intent for the current media item.
     *
     *
     * See [NotificationCompat.Builder.setContentIntent].
     *
     * @param player The [Player] for which a notification is being built.
     * @return The content intent for the current media item, or null if no intent should be fired.
     */
    override fun createCurrentContentIntent(player: Player): PendingIntent? = pendingIntent

    /**
     * Gets the content text for the current media item.
     *
     *
     * See [NotificationCompat.Builder.setContentText].
     *
     * @param player The [Player] for which a notification is being built.
     * @return The content text for the current media item, or null if no context text should be
     * displayed.
     */
    override fun getCurrentContentText(player: Player): CharSequence =
        player.mediaMetadata.displayTitle ?: "Unknown"


    /**
     * Gets the large icon for the current media item.
     *
     *
     * When a bitmap needs to be loaded asynchronously, a placeholder bitmap (or null) should be
     * returned. The actual bitmap should be passed to the [BitmapCallback] once it has been
     * loaded. Because the adapter may be called multiple times for the same media item, bitmaps
     * should be cached by the app and returned synchronously when possible.
     *
     *
     * See [NotificationCompat.Builder.setLargeIcon].
     *
     * @param player The [Player] for which a notification is being built.
     * @param callback A [BitmapCallback] to provide a [Bitmap] asynchronously.
     * @return The large icon for the current media item, or null if the icon will be returned
     * through the [BitmapCallback] or if no icon should be displayed.
     */
    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        Glide.with(context)
            .asBitmap()
            .load(player.mediaMetadata.artworkUri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>() {
                /**
                 * The method that will be called when the resource load has finished.
                 *
                 * @param resource the loaded resource.
                 */
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    callback.onBitmap(resource)
                }

                /**
                 * A **mandatory** lifecycle callback that is called when a load is cancelled and its resources
                 * are freed.
                 *
                 *
                 * You **must** ensure that any current Drawable received in [.onResourceReady] is no longer used before redrawing the container (usually a View) or changing its
                 * visibility.
                 *
                 * @param placeholder The placeholder drawable to optionally show, or null.
                 */
                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
        return null
    }

}