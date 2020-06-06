package com.sunasterisk.music_72.media

import android.media.AudioManager
import android.net.Uri
import com.sunasterisk.music_72.data.anotation.Type
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.screen.service.PlayTrackService
import com.sunasterisk.music_72.screen.service.listener.OnPlayTrackListener
import com.sunasterisk.music_72.utils.Constants
import com.sunasterisk.music_72.utils.listener.OnUpdateControlListener

class MediaManager(
    private val service: PlayTrackService
) : MediaSetting() {
    private var type = Type.TRACKS
    private var currentTrack: Track? = null
    private var tracks = mutableListOf<Track>()
    private var onPlayTrackListener: OnPlayTrackListener? = null
    private var onUpdateControlListener: OnUpdateControlListener? = null

    fun registerPlayTrackListener(listener: OnPlayTrackListener?) {
        onPlayTrackListener = listener
    }

    fun registerUpdateControlListener(listener: OnUpdateControlListener?) {
        onUpdateControlListener = listener
    }

    override fun create() {
        reset()
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                if (type.equals(Type.TRACKS, false) || type.equals(Type.SEARCH, false)) {
                    mediaPlayer.setDataSource(
                        service,
                        Uri.parse(currentTrack?.streamUrl + Constants.AUTHORIZED_SERVER)
                    )
                } else {
                    mediaPlayer.setDataSource(service, Uri.parse(currentTrack?.streamUrl))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        mediaPlayer.setOnErrorListener(service)
        mediaPlayer.setOnCompletionListener(service)
        mediaPlayer.setOnPreparedListener(service)
        mediaPlayer.prepareAsync()
    }

    override fun start() {
        mediaPlayer.start()
    }

    override fun change(track: Track) {
        currentTrack = track
        onPlayTrackListener?.onChangeTrackListener(track)
        onUpdateControlListener?.onUpdateControl()
        create()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun previousTrack() {
        change(getPreviousTrack())
    }

    private fun getPreviousTrack(): Track {
        return when (val position = tracks.indexOf(currentTrack)) {
            0 -> {
                tracks[tracks.size - 1]
            }
            -1 -> {
                tracks[tracks.size - 1]
            }
            else -> {
                tracks[position - 1]
            }
        }
    }

    override fun nextTrack() {
        change(getNextTrack())
    }

    fun getNextTrack(): Track {
        val position = tracks.indexOf(currentTrack)
        return if (position == tracks.size - 1) {
            tracks[0]
        } else {
            tracks[position + 1]
        }
    }

    fun setTracks(tracks: MutableList<Track>) {
        this.tracks = tracks
    }

    fun getTracks() = this.tracks

    fun getCurrentTrack() = this.currentTrack


    fun setCurrentTrack(track: Track) {
        currentTrack = track
    }

    override fun stop() {
        mediaPlayer.stop()
    }

    fun setType(@Type type: String) {
        this.type = type
    }

    fun getType() = this.type

    companion object {
        @Volatile
        @JvmStatic
        private var INSTANCE: MediaManager? = null

        @JvmStatic
        fun getInstance(service: PlayTrackService): MediaManager =
            (INSTANCE ?: synchronized(this) {
                INSTANCE ?: MediaManager(service).also { INSTANCE = it }
            })
    }
}
