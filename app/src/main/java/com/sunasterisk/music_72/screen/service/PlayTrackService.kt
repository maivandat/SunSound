package com.sunasterisk.music_72.screen.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import com.sunasterisk.music_72.data.anotation.Loop
import com.sunasterisk.music_72.data.anotation.State
import com.sunasterisk.music_72.data.anotation.Type
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.media.MediaManager
import com.sunasterisk.music_72.screen.service.listener.OnControlListener
import com.sunasterisk.music_72.screen.service.listener.OnPlayTrackListener
import com.sunasterisk.music_72.utils.Action
import com.sunasterisk.music_72.utils.listener.OnUpdateControlListener
import com.sunasterisk.music_72.utils.notification.NotificationHelper

class PlayTrackService : Service(), MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {
    private var mediaManager: MediaManager? = null
    private var notificationHelper: NotificationHelper? = null
    private val binder: IBinder = PlayTrackBinder()
    private var onPlayTrackListener: OnPlayTrackListener? = null
    private var onControlListener: OnControlListener? = null
    private var handler: Handler? = null
    private var updateTime: UpdateTime? = null
    var loop = Loop.NON
    var state = State.PAUSE

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        notificationHelper?.createNotificationChannel()
        mediaManager = MediaManager.getInstance(this)
        handler = Handler()
        handler?.let {  updateTime = UpdateTime(it) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == null) {
            return START_NOT_STICKY
        } else {
            when(intent.action) {
                Action.ACTION_PREVIOUS -> {
                    onPlayTrackListener?.onDefaultTimeChangeTrack()
                    previousTrack()
                }
                Action.ACTION_NEXT -> {
                    onPlayTrackListener?.onDefaultTimeChangeTrack()
                    nextTrack()
                }
                Action.ACTION_PLAY_AND_PAUSE -> {
                    if (state == State.PLAY) {
                        pauseTrack()
                    } else {
                        startTrack()
                    }
                    onPlayTrackListener?.onUpdateActionPlayAndPauseTrack()
                    onControlListener?.onUpdateActionPlayAndPauseTrack()
                }
                Action.ACTION_DELETE -> {
                    stopForeground(true)
                }
            }
        }
        return START_NOT_STICKY
    }

    fun registerPlayTrackListener(listener: OnPlayTrackListener?) {
        onPlayTrackListener = listener
        mediaManager?.registerPlayTrackListener(listener)
    }

    fun registerControlListener(controlListener: OnControlListener?) {
        onControlListener = controlListener
    }

    fun registerUpdateControlListener(listener: OnUpdateControlListener?) {
        mediaManager?.registerUpdateControlListener(listener)
    }

    fun setTracks(tracks: MutableList<Track>) {
        mediaManager?.setTracks(tracks)
    }

    fun getCurrentTrack() = mediaManager?.getCurrentTrack()

    fun getNextTrack() = mediaManager?.getNextTrack()

    fun changeTrack(track: Track) {
        mediaManager?.change(track)
        notificationHelper?.builderNotification(track)
    }

    fun startTrack() {
        mediaManager?.start()
        state = State.PLAY
        getCurrentTrack()?.let { notificationHelper?.builderNotification(it) }
    }

    fun pauseTrack() {
        mediaManager?.pause()
        state = State.PAUSE
        getCurrentTrack()?.let { notificationHelper?.builderNotification(it) }
    }

    fun nextTrack() {
        handler?.removeCallbacks(updateTime)
        mediaManager?.nextTrack()
        getCurrentTrack()?.let { notificationHelper?.builderNotification(it) }
        onPlayTrackListener?.onDefaultTimeChangeTrack()
    }

    fun previousTrack() {
        handler?.removeCallbacks(updateTime)
        mediaManager?.previousTrack()
        getCurrentTrack()?.let { notificationHelper?.builderNotification(it) }
    }

    fun getCurrentDuration() = mediaManager?.getCurrentDuration()

    fun getDuration() = mediaManager?.getDuration()

    fun seekTo(milliseconds: Int) = mediaManager?.seek(milliseconds)

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int) = true

    override fun onCompletion(mp: MediaPlayer?) {
        handler?.removeCallbacks(updateTime)
        mediaManager?.apply {
            when(loop) {
                Loop.NON -> {
                    getCurrentTrack()?.let {
                        if (getTracks().indexOf(it) != getTracks().size.minus(1)) {
                            nextTrack()
                        }
                    }
                }
                Loop.ONE -> {
                    getCurrentTrack()?.let {
                        change(it)
                    }
                }
                Loop.ALL -> { nextTrack() }
            }
        }
    }

    fun getType() = mediaManager?.getType()

    fun setType(@Type type: String) = mediaManager?.setType(type)

    override fun onPrepared(mp: MediaPlayer?) {
        this.state = State.PLAY
        startTrack()
        handler?.post(updateTime)
    }

    inner class UpdateTime(private val handler: Handler) : Runnable {
        override fun run() {
            onPlayTrackListener?.onUpdateTimeListener()
            onControlListener?.onUpdateTimeListener()
            handler.post(this)
        }
    }

    inner class PlayTrackBinder : Binder() {
        fun getService() = this@PlayTrackService
    }

    companion object {
        fun getIntent(context: Context) = Intent(context, PlayTrackService::class.java)
    }
}
