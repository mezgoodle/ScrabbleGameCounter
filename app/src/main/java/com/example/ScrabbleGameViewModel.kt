package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScrabbleGameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("scrabble_game_prefs", Context.MODE_PRIVATE)

    private val _gameState = MutableStateFlow(ScrabbleGameState())
    val gameState: StateFlow<ScrabbleGameState> = _gameState.asStateFlow()

    init {
        loadSavedGame()
    }

    private fun loadSavedGame() {
        val savedJson = prefs.getString("scrabble_game_state", null)
        if (savedJson != null) {
            val loadedState = ScrabbleGameState.fromJsonString(savedJson)
            if (loadedState != null) {
                _gameState.value = loadedState
            }
        }
    }

    private fun saveGame() {
        val stateJson = _gameState.value.toJsonString()
        prefs.edit().putString("scrabble_game_state", stateJson).apply()
    }

    /**
     * Initializes a new game with the given player list names (2-4 players)
     */
    fun startGame(playerNames: List<String>) {
        val cleanNames = playerNames.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleanNames.size < 2) return // Insufficient players

        val newList = cleanNames.map { name -> Player(name = name) }
        _gameState.update {
            ScrabbleGameState(
                players = newList,
                activePlayerIndex = 0,
                history = emptyList(),
                isGameOver = false,
                isSetupActive = false
            )
        }
        saveGame()
    }

    /**
     * Records a score addition for the active player and records history log
     */
    fun addTurn(word: String, points: Int, description: String, baseScore: Int) {
        val currentState = _gameState.value
        if (currentState.players.isEmpty() || currentState.isGameOver) return

        val activeIndex = currentState.activePlayerIndex
        val activePlayer = currentState.players[activeIndex]

        val updatedPlayers = currentState.players.mapIndexed { idx, player ->
            if (idx == activeIndex) {
                player.copy(score = player.score + points)
            } else {
                player
            }
        }

        val formattedWord = word.uppercase().trim()
        val entry = PlayHistoryEntry(
            playerName = activePlayer.name,
            playerIndex = activeIndex,
            word = if (formattedWord.isEmpty()) "(Пропустив)" else formattedWord,
            baseScore = baseScore,
            finalScore = points,
            description = description
        )

        val updatedHistory = currentState.history + entry
        val nextIndex = (activeIndex + 1) % currentState.players.size

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                history = updatedHistory,
                activePlayerIndex = nextIndex
            )
        }
        saveGame()
    }

    /**
     * Undoes the last recorded history entry.
     * Restores players' scores and updates the active player to that person.
     */
    fun undoLastTurn() {
        val currentState = _gameState.value
        if (currentState.history.isEmpty()) return

        val lastEntry = currentState.history.last()
        val playerIndexToRevert = lastEntry.playerIndex

        val updatedPlayers = currentState.players.mapIndexed { idx, player ->
            if (idx == playerIndexToRevert) {
                player.copy(score = maxOf(0, player.score - lastEntry.finalScore))
            } else {
                player
            }
        }

        val updatedHistory = currentState.history.dropLast(1)

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                history = updatedHistory,
                activePlayerIndex = playerIndexToRevert // Return turn focus to this player
            )
        }
        saveGame()
    }

    /**
     * Replaces the last turn stats with updated inputs
     */
    fun editLastTurn(newWord: String, newPoints: Int, newDescription: String, newBaseScore: Int) {
        val currentState = _gameState.value
        if (currentState.history.isEmpty()) return

        val lastEntry = currentState.history.last()
        val playerIndexToRevert = lastEntry.playerIndex

        // Subtract historic, add new
        val updatedPlayers = currentState.players.mapIndexed { idx, player ->
            if (idx == playerIndexToRevert) {
                val scoreAfterRevert = maxOf(0, player.score - lastEntry.finalScore)
                player.copy(score = scoreAfterRevert + newPoints)
            } else {
                player
            }
        }

        val formattedWord = newWord.uppercase().trim()
        val newEntry = lastEntry.copy(
            word = if (formattedWord.isEmpty()) "(Пропустив)" else formattedWord,
            baseScore = newBaseScore,
            finalScore = newPoints,
            description = newDescription
        )

        val updatedHistory = currentState.history.dropLast(1) + newEntry

        _gameState.update {
            it.copy(
                players = updatedPlayers,
                history = updatedHistory
            )
        }
        saveGame()
    }

    /**
     * Transition the current game state to GameOver
     */
    fun endGame() {
        _gameState.update { it.copy(isGameOver = true) }
        saveGame()
    }

    /**
     * Completely resets back to fresh Setup screen
     */
    fun resetGame() {
        _gameState.update {
            ScrabbleGameState(
                players = emptyList(),
                activePlayerIndex = 0,
                history = emptyList(),
                isGameOver = false,
                isSetupActive = true
            )
        }
        saveGame()
    }
}
