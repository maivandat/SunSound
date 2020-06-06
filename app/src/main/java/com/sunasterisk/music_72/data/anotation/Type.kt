package com.sunasterisk.music_72.data.anotation

import androidx.annotation.StringDef
import com.sunasterisk.music_72.data.anotation.Type.Companion.AUDIOS
import com.sunasterisk.music_72.data.anotation.Type.Companion.SEARCH
import com.sunasterisk.music_72.data.anotation.Type.Companion.TRACKS

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    TRACKS,
    AUDIOS,
    SEARCH
)

annotation class Type {
    companion object {
        const val TRACKS = "TRACKS"
        const val AUDIOS = "AUDIOS"
        const val SEARCH = "SEARCH"
    }
}
