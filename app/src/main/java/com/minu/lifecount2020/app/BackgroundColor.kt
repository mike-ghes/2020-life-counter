package com.minu.lifecount2020.app

import androidx.annotation.ColorInt

/**
 * Created by Miro on 2/3/2016.
 */
enum class BackgroundColor(@param:ColorInt val color: Int) {
    WHITE(0xf5f5f5),
    GREY(0x333231),
    BLACK(0x000000);

    operator fun next(): BackgroundColor {
        return when (this) {
            WHITE -> GREY
            GREY -> BLACK
            BLACK -> WHITE
        }
    }
}