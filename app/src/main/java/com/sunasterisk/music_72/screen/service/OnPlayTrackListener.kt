package com.sunasterisk.music_72.screen.service

import com.sunasterisk.music_72.data.model.Track

interface OnPlayTrackListener {
    fun onChangeTrackListener(track: Track)
    fun onUpdateTimeListener()
    fun onUpdateActionPlayAndPauseTrack()
    fun onDefaultTimeChangeTrack()
}