package com.alixmartin.chessmonitor.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.alixmartin.chessmonitor.data.model.Game

@Database(
    entities = [Game::class],
    version = 4,
    exportSchema = false
)
abstract class ChessDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    
    companion object {
        const val DATABASE_NAME = "chess_monitor_db"
    }
}
