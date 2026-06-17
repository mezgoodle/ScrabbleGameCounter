package com.example

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Representation of a single tile multiplier state.
 */
enum class LetterMultiplier(val value: Int, val label: String) {
    NORMAL(1, "1x"),
    DOUBLE_LETTER(2, "2Б"),
    TRIPLE_LETTER(3, "3Б"),
    BLANK(0, "ПУС")
}

/**
 * Represents word-level score doubling/tripling.
 */
enum class WordMultiplier(val value: Int, val label: String) {
    NONE(1, "1x"),
    DOUBLE_WORD(2, "2С"),
    TRIPLE_WORD(3, "3С")
}

/**
 * Single Player Model
 */
data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val score: Int = 0
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("score", score)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): Player {
            return Player(
                id = json.getString("id"),
                name = json.getString("name"),
                score = json.getInt("score")
            )
        }
    }
}

/**
 * Keeps track of score entry history log
 */
data class PlayHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val playerName: String,
    val playerIndex: Int,
    val word: String,
    val baseScore: Int,
    val finalScore: Int,
    val description: String, // e.g., "TL on L, DW", "+50 Bingo"
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("playerName", playerName)
            put("playerIndex", playerIndex)
            put("word", word)
            put("baseScore", baseScore)
            put("finalScore", finalScore)
            put("description", description)
            put("timestamp", timestamp)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): PlayHistoryEntry {
            return PlayHistoryEntry(
                id = json.getString("id"),
                playerName = json.getString("playerName"),
                playerIndex = json.getInt("playerIndex"),
                word = json.getString("word"),
                baseScore = json.getInt("baseScore"),
                finalScore = json.getInt("finalScore"),
                description = json.getString("description"),
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )
        }
    }
}

/**
 * Complete game configuration and ongoing session data.
 */
data class ScrabbleGameState(
    val players: List<Player> = emptyList(),
    val activePlayerIndex: Int = 0,
    val history: List<PlayHistoryEntry> = emptyList(),
    val isGameOver: Boolean = false,
    val isSetupActive: Boolean = true
) {
    fun toJsonString(): String {
        val root = JSONObject()
        val playersArray = JSONArray()
        players.forEach { playersArray.put(it.toJsonObject()) }
        
        val historyArray = JSONArray()
        history.forEach { historyArray.put(it.toJsonObject()) }
        
        root.put("players", playersArray)
        root.put("activePlayerIndex", activePlayerIndex)
        root.put("history", historyArray)
        root.put("isGameOver", isGameOver)
        root.put("isSetupActive", isSetupActive)
        
        return root.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): ScrabbleGameState? {
            return try {
                val root = JSONObject(jsonStr)
                
                val playersList = mutableListOf<Player>()
                val playersArray = root.getJSONArray("players")
                for (i in 0 until playersArray.length()) {
                    playersList.add(Player.fromJsonObject(playersArray.getJSONObject(i)))
                }
                
                val historyList = mutableListOf<PlayHistoryEntry>()
                val historyArray = root.getJSONArray("history")
                for (i in 0 until historyArray.length()) {
                    historyList.add(PlayHistoryEntry.fromJsonObject(historyArray.getJSONObject(i)))
                }
                
                ScrabbleGameState(
                    players = playersList,
                    activePlayerIndex = root.getInt("activePlayerIndex"),
                    history = historyList,
                    isGameOver = root.getBoolean("isGameOver"),
                    isSetupActive = root.getBoolean("isSetupActive")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Scrabble letter base score standard values
 */
object ScrabbleRules {
    fun getLetterBaseValue(char: Char): Int {
        return when (char.uppercaseChar()) {
            'А', 'В', 'Е', 'И', 'І', 'К', 'Н', 'О', 'Р', 'С', 'Т' -> 1
            'Д', 'Л', 'М', 'П', 'У' -> 2
            'Б', 'Г', 'Ґ', 'Є', 'З', 'Х', 'Ц', 'Ч', 'Ш' -> 3
            'Й', 'Ю', 'Я' -> 4
            'Ж', 'Ф', 'Щ' -> 5
            'Ь', 'Ї' -> 8
            '\'' -> 10 // Апостроф
            else -> 0
        }
    }
}
