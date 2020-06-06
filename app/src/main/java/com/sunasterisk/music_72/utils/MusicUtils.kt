package com.sunasterisk.music_72.utils

import com.sunasterisk.music_72.R
import com.sunasterisk.music_72.data.anotation.GenreName
import java.text.SimpleDateFormat

object MusicUtils {

    fun getGenreImage(@GenreName genre: String) = getGenreImages()[genre]

    private fun getGenreImages() = hashMapOf(
        GenreName.ALL_TRACK to R.drawable.bg_all_music,
        GenreName.COUNTRY to R.drawable.bg_country,
        GenreName.AMBIENT to R.drawable.bg_ambient,
        GenreName.ROCK to R.drawable.bg_rock,
        GenreName.CLASSICAL to R.drawable.bg_classical
    )

    fun formatIntToTimeString(time: Int) = SimpleDateFormat("mm:ss").format(time)

    fun formatUserName(username: String?) = username?.replace(".", "")

}
