package com.sunasterisk.music_72.screen.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.sunasterisk.music_72.BR
import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.model.Track
import com.sunasterisk.music_72.utils.listener.OnDownloadItemListener
import com.sunasterisk.music_72.utils.listener.OnRecyclerViewItemListener

class ItemTrackViewModel(
    private val data: Track,
    private val currentTrack: Track?,
    private val listener: OnRecyclerViewItemListener<Track>?,
    private val downloadListener: OnDownloadItemListener?
) : BaseObservable() {

    @Bindable
    var track: Track? = null

    init {
        track = data
        notifyPropertyChanged(BR.track)
    }

    fun setActionPlayTrack() =
        if (currentTrack != null) {
            when(currentTrack.id) {
                data.id -> R.drawable.ic_pause_white_24dp
                else -> R.drawable.ic_play_white_50dp
            }
        } else {
            R.drawable.ic_play_white_50dp
        }

    fun onDownloadListener() { track?.let { downloadListener?.onItemDownloadClick(it) } }

    fun onClickListener() { track?.let { listener?.onItemClick(it) } }
}
