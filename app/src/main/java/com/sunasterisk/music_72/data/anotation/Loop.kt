package com.sunasterisk.music_72.data.anotation

import androidx.annotation.StringDef
import com.sunasterisk.music_72.data.anotation.Loop.Companion.ALL
import com.sunasterisk.music_72.data.anotation.Loop.Companion.NON
import com.sunasterisk.music_72.data.anotation.Loop.Companion.ONE

@Retention(AnnotationRetention.SOURCE)
@StringDef(
    NON,
    ONE,
    ALL
)

annotation class Loop {
    companion object {
        const val NON = "NON"
        const val ONE = "ONE"
        const val ALL = "ALL"
    }
}
