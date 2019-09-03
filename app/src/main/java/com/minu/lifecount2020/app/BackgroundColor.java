package com.minu.lifecount2020.app;

import android.support.annotation.ColorInt;

/**
 * Created by Miro on 2/3/2016.
 */
public enum BackgroundColor {
    WHITE(0xf5f5f5),
    GREY(0x333231),
    BLACK(0x000000);

    private int color;

    BackgroundColor(@ColorInt int c) {
        color = c;
    }

    public int getColor() {
        return color;
    }

//    public BackgroundColor next() {
//        if (this == WHITE) {
//            return GREY;
//        } else if (this == GREY) {
//            return BLACK;
//        } else if (this == BLACK) {
//            return WHITE;
//        }
//    }
}
