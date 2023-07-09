package com.minu.lifecount2020.app

import android.content.SharedPreferences
import android.os.Bundle

data class Settings(
    var poisonShowing: Boolean,
    var energyShowing: Boolean,
    var startingLife: Int,
    var theme: Theme,
    var roundTimeInMinutes: Int,
    var timerShowing: Boolean,
    var hapticFeedbackEnabled: Boolean
) {

    fun saveTo(editor: SharedPreferences.Editor) = editor.putSettings(this)

    fun saveTo(bundle: Bundle) = bundle.putSettings(this)

    fun buildNewGame() = GameState(
            ArrayList(),
            minutesToMilliseconds(roundTimeInMinutes),
            PlayerState(startingLife.toString(), Constants.STARTING_ENERGY, Constants.STARTING_POISON),
            PlayerState(startingLife.toString(), Constants.STARTING_ENERGY, Constants.STARTING_POISON)
    )
    companion object {
        fun fromBundle(bundle: Bundle) = bundle.getSettings()

        fun fromPreferences(preferences: SharedPreferences) = preferences.getSettings()

        fun getDefault() = Settings(
                poisonShowing = false,
                energyShowing = false,
                startingLife = 20,
                theme = Theme.WHITE,
                roundTimeInMinutes = 50,
                timerShowing = false,
                hapticFeedbackEnabled = false
        )
    }
}

fun SharedPreferences.Editor.putSettings(settings: Settings) {
    putInt(Constants.BACKGROUND_WHITE, settings.theme.ordinal)
    putBoolean(Constants.POISON, settings.poisonShowing)
    putBoolean(Constants.ENERGY, settings.energyShowing)
    putBoolean(Constants.ENABLE_HAPTIC, settings.hapticFeedbackEnabled)
    putInt(Constants.STARTING_LIFE, settings.startingLife)
    putInt(Constants.ROUND_TIME, settings.roundTimeInMinutes)
    putBoolean(Constants.ROUND_TIMER_SHOWING, settings.timerShowing)
}

fun Bundle.putSettings(settings: Settings) {
    putInt(Constants.BACKGROUND_WHITE, settings.theme.ordinal)
    putBoolean(Constants.POISON, settings.poisonShowing)
    putBoolean(Constants.ENERGY, settings.energyShowing)
    putBoolean(Constants.ENABLE_HAPTIC, settings.hapticFeedbackEnabled)
    putInt(Constants.STARTING_LIFE, settings.startingLife)
    putInt(Constants.ROUND_TIME, settings.roundTimeInMinutes)
    putBoolean(Constants.ROUND_TIMER_SHOWING, settings.timerShowing)
}

fun Bundle.getSettings(): Settings {
    return Settings(
        poisonShowing = getBoolean(Constants.POISON),
        energyShowing = getBoolean(Constants.ENERGY),
        startingLife = getInt(Constants.STARTING_LIFE),
        theme = Theme.values()[getInt(Constants.BACKGROUND_WHITE, 0)],
        roundTimeInMinutes = getInt(Constants.ROUND_TIME),
        timerShowing = getBoolean(Constants.ROUND_TIMER_SHOWING),
        hapticFeedbackEnabled = getBoolean(Constants.ENABLE_HAPTIC)
    )
}

fun SharedPreferences.getSettings(): Settings {
    return Settings(
        poisonShowing = getBoolean(Constants.POISON, false),
        energyShowing = getBoolean(Constants.ENERGY, false),
        startingLife = getInt(Constants.STARTING_LIFE, Integer.parseInt(Constants.STARTING_LIFE)),
        theme = Theme.values()[getInt(Constants.BACKGROUND_WHITE, 0)],
        roundTimeInMinutes = getInt(Constants.ROUND_TIME, 50),
        timerShowing = getBoolean(Constants.ROUND_TIMER_SHOWING, false),
        hapticFeedbackEnabled = getBoolean(Constants.ENABLE_HAPTIC, false)
    )
}

private fun minutesToMilliseconds(minutes: Int): Long {
    return (minutes * 60 * 1000).toLong()
}
