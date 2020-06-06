package com.sunasterisk.music_72.utils

import com.sunasterisk.music_72.BuildConfig

object Constants {
    const val API_KEY = BuildConfig.SOUNDCLOUD_API_KEY
    const val CLIENT_ID = "client_id"
    const val AUTHORIZED_SERVER =
        "?" + CLIENT_ID + "=" + BuildConfig.SOUNDCLOUD_API_KEY
    const val ARGUMENT_NOTIFICATION_TRACK_KEY = "ARGUMENT_NOTIFICATION_TRACK_KEY"
    const val ARGUMENT_NOTIFICATION_TYPE_KEY = "ARGUMENT_NOTIFICATION_TYPE_KEY"
    const val LIMIT_ITEM = 10
}

object Action {
    var ACTION_PLAY_AND_PAUSE = "com.sun.music.ACTION_PLAY_AND_PAUSE"
    var ACTION_NEXT = "com.sun.music.ACTION_NEXT"
    var ACTION_PREVIOUS = "com.sun.music.ACTION_PREVIOUS"
    var ACTION_DELETE = "com.sun.music.ACTION_DELETE"
    var ACTION_PLAY_TRACK = "com.sun.music.ACTION_PLAY_TRACK"
}
