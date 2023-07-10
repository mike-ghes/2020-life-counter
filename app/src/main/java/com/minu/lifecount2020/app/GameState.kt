package com.minu.lifecount2020.app

import android.content.SharedPreferences
import android.os.Bundle
import java.io.Serializable

data class GameState(
    var history: List<GameSnapshot>,
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

    fun collapseHistory(): Boolean {
        var currentTime: Long
        var nextTime: Long

        var i = 0
        var didChange = false

        val mutableList = history.toMutableList()

        while (i + 1 < mutableList.size) {
            if (!mutableList[i].isRead && !mutableList[i+1].isRead) {
                currentTime = mutableList[i].timestamp.toLong()
                nextTime = mutableList[i + 1].timestamp.toLong()
                if (nextTime - currentTime < 2000) {
                    mutableList.removeAt(i)
                    i--
                }
                didChange = true
            }
            i++
        }

        for (i in mutableList.indices) {
            mutableList[i] = mutableList[i].asRead()
        }

        history = mutableList
        return didChange
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
    putString(Constants.HISTORY, state.history.map { it.value }.toString())
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

    putStringArrayList(Constants.HISTORY, ArrayList(state.history.map { it.value }))

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
    val history = getStringArrayList(Constants.HISTORY)
        ?.map { GameSnapshot(it) }
        ?: mutableListOf()

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
        ?.map(::GameSnapshot)
        ?: emptyList()

    val timeRemaining = getLong(Constants.REMAINING_ROUND_TIME, Constants.BASE_ROUND_TIME_IN_MS)

    return GameState(history = ArrayList(tempList), remainingMillis = timeRemaining, playerOne = p1, playerTwo = p2)
}

//@JvmInline
data class GameSnapshot(val tokens: List<String>): Serializable {

    constructor(value: String): this(value.split(" "))

    constructor(
        leftLife: String,
        rightLife: String,
        leftPoison: String,
        rightPoison: String,
        leftEnergy: String,
        rightEnergy: String,
        timestamp: Long = System.currentTimeMillis()
    ): this(listOf(leftLife, rightLife, leftPoison, rightPoison, leftEnergy, rightEnergy, timestamp.toString()))

    val value: String
        get() = "$leftLife $rightLife $leftPoison $rightPoison $leftEnergy $rightEnergy $timestamp"

    val leftLife: String
        get() = tokens[0]

    val rightLife: String
        get() = tokens[1]

    val leftPoison: String
        get() = tokens[2]

    val rightPoison: String
        get() = tokens[3]

    val leftEnergy: String
        get() = tokens[4]

    val rightEnergy: String
        get() = tokens[5]

    val isRead: Boolean
        get() = tokens[6].compareTo(Constants.READ) == 0

    val timestamp: String
        get() = tokens[6]

    fun asRead(): GameSnapshot {
        val split = tokens
        return GameSnapshot("${split[0]} ${split[1]} ${split[2]} ${split[3]} ${split[4]} ${split[5]} ${Constants.READ}")
    }

}