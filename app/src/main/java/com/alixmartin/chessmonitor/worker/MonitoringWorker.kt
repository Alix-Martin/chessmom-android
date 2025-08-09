package com.alixmartin.chessmonitor.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.alixmartin.chessmonitor.data.repository.TournamentRepository
import com.alixmartin.chessmonitor.data.repository.UserPreferencesRepository
import com.alixmartin.chessmonitor.service.NotificationService
import kotlinx.coroutines.flow.first
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class MonitoringWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TournamentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val notificationService: NotificationService
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val tournamentId = inputData.getInt(KEY_TOURNAMENT_ID, -1)
            val round = inputData.getInt(KEY_ROUND, -1)

            if (tournamentId == -1 || round == -1) {
                return Result.failure()
            }

            // Get previously known finished games
            val previousFinishedGames = repository.getFinishedGames(tournamentId, round)
            
            // Get current watch list
            val watchListNames = userPreferencesRepository.watchListFlow.first()

            // Fetch latest games
            repository.fetchTournamentGames(tournamentId, round)
                .onSuccess { tournamentData ->
                    // Find newly finished games
                    val currentFinishedGames = tournamentData.games.filter { it.isFinished() }
                    val newFinishedGames = currentFinishedGames.filter { newGame ->
                        previousFinishedGames.none { it.id == newGame.id }
                    }

                    // Send notifications only for games involving watch list players
                    newFinishedGames.forEach { game ->
                        val isWatchedGame = watchListNames.contains(game.player1Name) || 
                                          watchListNames.contains(game.player2Name)
                        
                        if (isWatchedGame) {
                            Log.i(TAG, "New finished game with watched player detected: $game")
                            notificationService.showGameFinishedNotification(game, watchListNames)
                        } else {
                            Log.d(TAG, "New finished game detected but no watched players: $game")
                        }
                    }
                }
                .onFailure {
                    return Result.retry()
                }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "MonitoringWorker"
        const val WORK_NAME = "tournament_monitoring"
        const val KEY_TOURNAMENT_ID = "tournament_id"
        const val KEY_ROUND = "round"
    }
}
