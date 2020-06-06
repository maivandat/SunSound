package com.sunasterisk.music_72.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.State
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.screen.MainActivity
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.utils.Action
import com.sunasterisk.music_72.utils.Constants


class NotificationHelper(private val service: PlayTrackService) {

    private lateinit var notification: NotificationCompat.Builder
    fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = service.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            // Register the channel with the system
            val notificationManager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun builderNotification(track: Track) {
        notification = NotificationCompat.Builder(service, CHANNEL_ID)
            .setContentTitle(track.title)
            .setContentText(track.user.username)
            .setSmallIcon(R.drawable.ic_headphones_50dp)
            .setLargeIcon(logoNotification())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                R.drawable.ic_previous_24dp, service.getString(R.string.title_previous),
                pendingIntentAction(Action.ACTION_PREVIOUS)
            )
            .addAction(
                if(service.state == State.PLAY) {
                    R.drawable.ic_pause_white_50dp
                } else {
                    R.drawable.ic_play_white_50dp
                },
                if(service.state == State.PLAY) {
                    service.getString(R.string.title_play)
                } else {
                    service.getString(R.string.title_pause)
                },
                pendingIntentAction(Action.ACTION_PLAY_AND_PAUSE))
            .addAction(R.drawable.ic_next_24dp, service.getString(R.string.title_next), pendingIntentAction(Action.ACTION_NEXT))
            .addAction(R.drawable.ic_clear_white_24dp, service.getString(R.string.title_stop), pendingIntentAction(Action.ACTION_DELETE))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(ACTION_PREVIOUS, ACTION_PAUSE_AND_PLAY, ACTION_NEXT, ACTION_DELETE)
                .setShowCancelButton(false)
            )
        service.startForeground(ID_NOTIFICATION, notification.build())
    }

    private fun logoNotification() =
        BitmapFactory.decodeResource(service.resources, R.drawable.logo_app)

    private fun pendingIntentAction(action: String): PendingIntent? {
        val prevIntent = Intent(service, PlayTrackService::class.java)
        prevIntent.action = action
        return PendingIntent.getService(
            service,
            REQUEST_CODE,
            prevIntent,
            FLAG
        )
    }

    private fun intentAction(action: String): PendingIntent? {
        val prevIntent = Intent(service, MainActivity::class.java).apply {
            this.action = action
            putExtra(Constants.ARGUMENT_NOTIFICATION_TRACK_KEY, service.getCurrentTrack())
            putExtra(Constants.ARGUMENT_NOTIFICATION_TYPE_KEY, service.getType())
        }
        return PendingIntent.getActivity(
            service,
            REQUEST_CODE,
            prevIntent,
            FLAG
        )
    }

    companion object {
        private const val REQUEST_CODE = 1499
        private const val FLAG = 0
        private const val ID_NOTIFICATION = 1
        private const val ACTION_PREVIOUS = 0
        private const val ACTION_PAUSE_AND_PLAY = 1
        private const val ACTION_NEXT = 2
        private const val ACTION_DELETE = 3
        private const val CHANNEL_ID = "MUSIC"
    }
}
