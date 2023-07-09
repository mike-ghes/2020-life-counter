package com.minu.lifecount2020.app

import android.content.SharedPreferences
import android.os.Bundle
import kotlin.collections.ArrayList

data class GameState(
        var history: ArrayList<String>,
        var remainingMillis: Long,
        var playerOne: PlayerState,
        var playerTwo: PlayerState
) {

    operator fun get(player: Player) = when(player) {
        Player.ONE -> playerOne
        Player.TWO -> playerTwo
    }

    fun saveTo(editor: SharedPreferences.Editor) = editor.putGameState(this)

    fun saveTo(bundle: Bundle) = bundle.putGameState(this)

    fun isLethal(): Boolean = playerOne.isLethal() || playerTwo.isLethal()

    fun update(player: Player, field: PlayerField, value: String) = when(field) {
        PlayerField.Life -> updateLife(player, value)
        PlayerField.Energy -> updateEnergy(player, value)
        PlayerField.Poison -> updatePoison(player, value)
    }

    fun updateLife(player: Player, value: String) {
        get(player).currentLife = value
    }

    fun updatePoison(player: Player, value: String) {
        get(player).currentPoison = value
    }

    fun updateEnergy(player: Player, value: String) {
        get(player).currentEnergy = value
    }

    companion object {
        fun fromBundle(bundle: Bundle) = bundle.getGameState()

        fun fromPreferences(preferences: SharedPreferences) = preferences.getGameState()
    }
}

enum class Player {
    ONE,
    TWO
}

enum class PlayerField {
    Life, Energy, Poison
}

data class PlayerState(
        var currentLife: String,
        var currentEnergy: String,
        var currentPoison: String
) {

    fun isLethal() = (currentLife.toInt() <= Constants.LETHAL_LIFE || currentPoison.toInt() >= Constants.LETHAL_POISON)

}



fun SharedPreferences.Editor.putGameState(state: GameState) {
    putString(Constants.HISTORY, state.history.toString())
    putLong(Constants.REMAINING_ROUND_TIME, state.remainingMillis)

    putString(Constants.PICKER_ONE_LIFE, state.playerOne.currentLife)
    putString(Constants.PICKER_TWO_LIFE, state.playerTwo.currentLife)

    putString(Constants.PICKER_ONE_POISON, state.playerOne.currentPoison)
    putString(Constants.PICKER_TWO_POISON, state.playerTwo.currentPoison)

    putString(Constants.PICKER_ONE_ENERGY, state.playerOne.currentEnergy)
    putString(Constants.PICKER_TWO_ENERGY, state.playerTwo.currentEnergy)
}

fun Bundle.putGameState(state: GameState) {

    putString(Constants.PICKER_ONE_LIFE, state.playerOne.currentLife)
    putString(Constants.PICKER_ONE_POISON, state.playerOne.currentPoison)
    putString(Constants.PICKER_ONE_ENERGY, state.playerOne.currentEnergy)

    putString(Constants.PICKER_TWO_LIFE, state.playerTwo.currentLife)
    putString(Constants.PICKER_TWO_POISON, state.playerTwo.currentPoison)
    putString(Constants.PICKER_TWO_ENERGY, state.playerTwo.currentEnergy)

    putStringArrayList(Constants.HISTORY, state.history)

    putLong(Constants.REMAINING_ROUND_TIME, state.remainingMillis)
}

fun Bundle.getGameState(): GameState {
    val playerOne = PlayerState(
            currentLife = getString(Constants.PICKER_ONE_LIFE)!!,
            currentPoison = getString(Constants.PICKER_ONE_POISON)!!,
            currentEnergy = getString(Constants.PICKER_ONE_ENERGY)!!
    )

    val playerTwo = PlayerState(
            currentLife = getString(Constants.PICKER_TWO_LIFE)!!,
            currentPoison = getString(Constants.PICKER_TWO_POISON)!!,
            currentEnergy = getString(Constants.PICKER_TWO_ENERGY)!!
    )
    val history = getStringArrayList(Constants.HISTORY) ?: ArrayList()

    val remainingTime = getLong(Constants.REMAINING_ROUND_TIME)

    return GameState(history, remainingTime, playerOne, playerTwo)
}

fun SharedPreferences.getGameState(): GameState {
    val lifeOne = getString(Constants.PICKER_ONE_LIFE, Constants.STARTING_LIFE)!!
    val lifeTwo = getString(Constants.PICKER_TWO_LIFE, Constants.STARTING_LIFE)!!
    val poisonOne = getString(Constants.PICKER_ONE_POISON, Constants.STARTING_POISON)!!
    val poisonTwo = getString(Constants.PICKER_TWO_POISON, Constants.STARTING_POISON)!!
    val energyOne = getString(Constants.PICKER_ONE_ENERGY, Constants.STARTING_ENERGY)!!
    val energyTwo = getString(Constants.PICKER_TWO_ENERGY, Constants.STARTING_ENERGY)!!

    val p1 = PlayerState(currentLife = lifeOne, currentEnergy = energyOne, currentPoison = poisonOne)
    val p2 = PlayerState(currentLife = lifeTwo, currentEnergy = energyTwo, currentPoison = poisonTwo)

    val historyAsString = getString(Constants.HISTORY, null)
    val tempList = historyAsString
        ?.substring(1, historyAsString.length - 1)
        ?.split(", ".toRegex())
        ?.dropLastWhile { it.isEmpty() }
        ?: emptyList()

    val timeRemaining = getLong(Constants.REMAINING_ROUND_TIME, Constants.BASE_ROUND_TIME_IN_MS)

    return GameState(history = ArrayList(tempList), remainingMillis = timeRemaining, playerOne = p1, playerTwo = p2)
}