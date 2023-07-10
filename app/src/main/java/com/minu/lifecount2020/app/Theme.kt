package com.minu.lifecount2020.app

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.io.Serializable

/**
 * Created by Miro on 2/3/2016.
 */
enum class Theme(
    @ColorRes val background: Int,
    @DrawableRes val drawable: Int
): Serializable {

    WHITE(R.color.background_light, R.drawable.color_scheme_light),
    GREY(R.color.background_dark, R.drawable.color_scheme_dark),
    BLACK(R.color.background_black, R.drawable.color_scheme_black);

    operator fun next(): Theme {
        return when (this) {
            WHITE -> GREY
            GREY -> BLACK
            BLACK -> WHITE
        }
    }

    fun getDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, drawable)!!
    }


    @ColorRes
    val playerOne: Int = R.color.light_red_2020

    @ColorRes
    val playerTwo: Int = R.color.light_blue_2020

    @ColorRes
    val textColor: Int = R.color.text_light_grey
}