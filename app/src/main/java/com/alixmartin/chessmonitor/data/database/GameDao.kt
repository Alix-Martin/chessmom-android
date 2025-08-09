package com.alixmartin.chessmonitor.data.database

import androidx.room.*
import com.alixmartin.chessmonitor.data.model.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE tournamentId = :tournamentId AND round = :round ORDER BY CASE WHEN finishedTimestamp IS NOT NULL THEN finishedTimestamp ELSE timestamp END DESC")
    fun getGamesForTournament(tournamentId: Int, round: Int): Flow<List<Game>>
    
    @Query("SELECT * FROM games WHERE tournamentId = :tournamentId AND round = :round")
    suspend fun getGamesForTournamentSync(tournamentId: Int, round: Int): List<Game>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<Game>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: Game)
    
    @Query("DELETE FROM games WHERE tournamentId = :tournamentId AND round = :round")
    suspend fun clearTournamentGames(tournamentId: Int, round: Int)
    
    @Query("SELECT COUNT(*) FROM games WHERE tournamentId = :tournamentId AND round = :round")
    suspend fun getGameCount(tournamentId: Int, round: Int): Int
}
