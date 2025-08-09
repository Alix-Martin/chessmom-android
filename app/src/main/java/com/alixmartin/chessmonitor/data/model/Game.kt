package com.alixmartin.chessmonitor.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class Game(
    @PrimaryKey
    val id: String,
    val tableNum: Int,
    val player1Name: String,
    val player1Rating: String,
    val player1Points: String,
    val result: String,
    val rawResult: String,
    val player2Name: String,
    val player2Rating: String,
    val player2Points: String,
    val tournamentId: Int,
    val round: Int,
    val finishedTimestamp: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedResult(): String {
        return "$player1Name ($player1Rating) $result $player2Name ($player2Rating)"
    }

    fun isFinished(): Boolean {
        return result.isNotBlank() && result != "-"
    }

    fun getPoints(): Pair<Float, Float> {
        return when (result.replace(" ", "")) {
            "1-0" -> Pair(1f, 0f)
            "0-1" -> Pair(0f, 1f)
            "X-X" -> Pair(0.5f, 0.5f)
            "½-½" -> Pair(0.5f, 0.5f)
            "1-0F", "+/-" -> Pair(1f, 0f)
            "0-1F", "-/+" -> Pair(0f, 1f)
            else -> Pair(0f, 0f)
        }
    }
}
