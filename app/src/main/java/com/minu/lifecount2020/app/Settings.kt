package com.minu.lifecount2020.app

import android.content.SharedPreferences
import android.os.Bundle

data class Settings(
        var poisonShowing: Boolean,
        var energyShowing: Boolean,
        var startingLife: Int,
        var backgroundColor: BackgroundColor,
        var roundTimeInMinutes: Int,
        var timerShowing: Boolean,
        var hapticFeedbackEnabled: Boolean
) {

    fun saveTo(editor: SharedPreferences.Editor) = editor.putSettings(this)

    fun saveTo(bundle: Bundle) = bundle.putSettings(this)

    companion object {
        fun fromBundle(bundle: Bundle) = bundle.getSettings()

        fun fromPreferences(preferences: SharedPreferences) = preferences.getSettings()

        fun getDefault() = Settings(
                poisonShowing = false,
                energyShowing = false,
                startingLife = 20,
                backgroundColor = BackgroundColor.WHITE,
                roundTimeInMinutes = 50,
                timerShowing = false,
                hapticFeedbackEnabled = false
        )
    }
}

fun SharedPreferences.Editor.putSettings(settings: Settings) {
    putInt(Constants.BACKGROUND_WHITE, settings.backgroundColor.ordinal)
    putBoolean(Constants.POISON, settings.poisonShowing)
    putBoolean(Constants.ENERGY, settings.energyShowing)
    putBoolean(Constants.ENABLE_HAPTIC, settings.hapticFeedbackEnabled)
    putInt(Constants.STARTING_LIFE, settings.startingLife)
    putInt(Constants.ROUND_TIME, settings.roundTimeInMinutes)
    putBoolean(Constants.ROUND_TIMER_SHOWING, settings.timerShowing)
}

fun Bundle.putSettings(settings: Settings) {
    putInt(Constants.BACKGROUND_WHITE, settings.backgroundColor.ordinal)
    putBoolean(Constants.POISON, settings.poisonShowing)
    putBoolean(Constants.ENERGY, settings.energyShowing)
    putBoolean(Constants.ENABLE_HAPTIC, settings.hapticFeedbackEnabled)
    putInt(Constants.STARTING_LIFE, settings.startingLife)
    putInt(Constants.ROUND_TIME, settings.roundTimeInMinutes)
    putBoolean(Constants.ROUND_TIMER_SHOWING, settings.timerShowing)
}


fun Bundle.getSettings(): Settings {
    val mPoisonShowing = getBoolean(Constants.POISON)
    val mEnergyShowing = getBoolean(Constants.ENERGY)
    val mStartingLife = getInt(Constants.STARTING_LIFE)
    val mBackgroundColor = getSerializable(Constants.BACKGROUND_WHITE) as BackgroundColor
    val roundTime = getInt(Constants.ROUND_TIME)
    val timerShowing = getBoolean(Constants.ROUND_TIMER_SHOWING)
    val mHapticFeedbackEnabled = getBoolean(Constants.ENABLE_HAPTIC)

    return Settings(poisonShowing = mPoisonShowing,
            energyShowing = mEnergyShowing,
            startingLife = mStartingLife,
            backgroundColor = mBackgroundColor,
            roundTimeInMinutes = roundTime,
            timerShowing = timerShowing,
            hapticFeedbackEnabled = mHapticFeedbackEnabled)
}

fun SharedPreferences.getSettings(): Settings {
    val mPoisonShowing = getBoolean(Constants.POISON, false)
    val mEnergyShowing = getBoolean(Constants.ENERGY, false)
    val mStartingLife = getInt(Constants.STARTING_LIFE, Integer.parseInt(Constants.STARTING_LIFE))
    val mBackgroundColor = BackgroundColor.values()[getInt(Constants.BACKGROUND_WHITE, 0)]
    val roundTime = getInt(Constants.ROUND_TIME, 50)
    val timerShowing = getBoolean(Constants.ROUND_TIMER_SHOWING, false)
    val mHapticFeedbackEnabled = getBoolean(Constants.ENABLE_HAPTIC, false)

    return Settings(poisonShowing = mPoisonShowing,
            energyShowing = mEnergyShowing,
            startingLife = mStartingLife,
            backgroundColor = mBackgroundColor,
            roundTimeInMinutes = roundTime,
            timerShowing = timerShowing,
            hapticFeedbackEnabled = mHapticFeedbackEnabled)
}