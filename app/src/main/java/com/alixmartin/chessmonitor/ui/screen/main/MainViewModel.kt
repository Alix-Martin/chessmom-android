package com.alixmartin.chessmonitor.ui.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.alixmartin.chessmonitor.data.model.Game
import com.alixmartin.chessmonitor.data.model.Player
import com.alixmartin.chessmonitor.data.repository.TournamentRepository
import com.alixmartin.chessmonitor.data.repository.UserPreferencesRepository
import com.alixmartin.chessmonitor.worker.MonitoringWorker
import com.alixmartin.chessmonitor.ui.screen.main.SelectedPane
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TournamentRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _watchlistNames = MutableStateFlow<Set<String>>(emptySet())
    val watchlistNames: StateFlow<Set<String>> = _watchlistNames.asStateFlow()

    private var gamesFlow: Flow<List<Game>>? = null

    fun addToWatchlist(player: Player) {
        viewModelScope.launch {
            userPreferencesRepository.addToWatchList(player.name)
            Log.d("MainViewModel", "Added ${player.name} to watchlist")
        }
    }

    fun removeFromWatchlist(playerName: String) {
        viewModelScope.launch {
            userPreferencesRepository.removeFromWatchList(playerName)
            Log.d("MainViewModel", "Removed $playerName from watchlist")
        }
    }

    fun isPlayerInWatchlist(playerName: String): Boolean {
        return _watchlistNames.value.contains(playerName)
    }

    init {
        // Load last used tournament ID and round
        viewModelScope.launch {
            combine(
                userPreferencesRepository.tournamentIdFlow,
                userPreferencesRepository.roundFlow
            ) { tournamentId, round ->
                Pair(tournamentId, round)
            }.collect { (tournamentId, round) ->
                _uiState.update {
                    it.copy(
                        tournamentId = tournamentId,
                        round = round
                    )
                }
            }
        }

        // Load watch list
        viewModelScope.launch {
            userPreferencesRepository.watchListFlow.collect { watchlistNames ->
                _watchlistNames.value = watchlistNames
            }
        }

        // Observe work status
        observeWorkStatus()
    }

    fun onPaneSelected(pane: SelectedPane) {
        _uiState.update { it.copy(selectedPane = pane) }
    }

    fun updateTournamentId(id: String) {
        _uiState.update { it.copy(tournamentId = id) }
    }

    fun updateRound(round: String) {
        _uiState.update { it.copy(round = round) }
    }

    fun startMonitoring() {
        val tournamentId = _uiState.value.tournamentId.toIntOrNull()
        val round = _uiState.value.round.toIntOrNull()

        if (tournamentId == null || round == null) {
            return
        }

        // Save the values for next time
        viewModelScope.launch {
            userPreferencesRepository.saveTournamentId(_uiState.value.tournamentId)
            userPreferencesRepository.saveRound(_uiState.value.round)
        }

        _uiState.update {
            it.copy(
                isMonitoring = true,
                connectionStatus = ConnectionStatus.CONNECTED
            )
        }

        // Start observing games from database
        startObservingGames(tournamentId, round)

        // Start WorkManager periodic task
        startPeriodicWork(tournamentId, round)

        // Initial fetch
        fetchGames(tournamentId, round)
    }

    fun stopMonitoring() {
        _uiState.update {
            it.copy(
                isMonitoring = false,
                connectionStatus = ConnectionStatus.DISCONNECTED
            )
        }

        // Cancel WorkManager task
        workManager.cancelUniqueWork(MonitoringWorker.WORK_NAME)
    }

    private fun startObservingGames(tournamentId: Int, round: Int) {
        gamesFlow = repository.getGamesForTournament(tournamentId, round)

        viewModelScope.launch {
            gamesFlow?.collect { games ->
                _uiState.update {
                    it.copy(
                        games = games,
                        players = extractAndSortPlayers(games),
                        lastUpdate = getCurrentTime()
                    )
                }
            }
        }
    }

    private fun startPeriodicWork(tournamentId: Int, round: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<MonitoringWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    MonitoringWorker.KEY_TOURNAMENT_ID to tournamentId,
                    MonitoringWorker.KEY_ROUND to round
                )
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            MonitoringWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun fetchGames(tournamentId: Int, round: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionStatus = ConnectionStatus.CONNECTED) }

            repository.fetchTournamentGames(tournamentId, round)
                .onSuccess { tournamentData ->
                    _uiState.update {
                        it.copy(
                            tournamentName = tournamentData.tournamentName,
                            games = tournamentData.games,
                            players = tournamentData.players.sortedWith(compareByDescending<Player> { it.points }.thenByDescending { it.rating }),
                            connectionStatus = ConnectionStatus.CONNECTED,
                            lastUpdate = getCurrentTime()
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("MainViewModel", "Failed to fetch tournament games", error)
                    val newStatus = if (error is java.io.IOException) {
                        ConnectionStatus.NETWORK_ERROR
                    } else {
                        ConnectionStatus.ERROR
                    }
                    _uiState.update {
                        it.copy(connectionStatus = newStatus)
                    }
                }
        }
    }

    private fun observeWorkStatus() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkLiveData(MonitoringWorker.WORK_NAME)
                .asFlow()
                .collect { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    if (workInfo != null) {
                        when (workInfo.state) {
                            WorkInfo.State.RUNNING -> {
                                _uiState.update {
                                    it.copy(connectionStatus = ConnectionStatus.CONNECTED)
                                }
                            }
                            WorkInfo.State.FAILED -> {
                                _uiState.update {
                                    it.copy(connectionStatus = ConnectionStatus.ERROR)
                                }
                            }
                            else -> { /* Handle other states if needed */
                            }
                        }
                    }
                }
        }
    }

    private fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }

    private fun extractAndSortPlayers(games: List<Game>): List<Player> {
        val playersMap = mutableMapOf<String, Player>()
        
        // Collect all players with their tournament points before this round
        games.forEach { game ->
            addPlayerWithPoints(playersMap, game.player1Name, game.player1Rating, game.player1Points)
            addPlayerWithPoints(playersMap, game.player2Name, game.player2Rating, game.player2Points)
        }

        return playersMap.values.sortedWith(compareByDescending<Player> { it.points }.thenByDescending { it.rating })
    }

    private fun addPlayerWithPoints(players: MutableMap<String, Player>, name: String, rating: String, points: String) {
        if (name.isNotBlank() && !name.equals("EXEMPT", ignoreCase = true) && !players.containsKey(name)) {
            players[name] = Player(
                name = name,
                rating = rating.filter { it.isDigit() }.toIntOrNull() ?: 0,
                points = formatPoints(points) // Use tournament points before this round
            )
        }
    }

    private fun formatPoints(points: String): Float {
        var formattedPoints = points.replace("½", ".5").replace(" ", "")
        if (formattedPoints.startsWith(".")) {
            formattedPoints = "0$formattedPoints"
        }
        return formattedPoints.toFloatOrNull() ?: 0f
    }
}

data class MainUiState(
    val tournamentId: String = "",
    val round: String = "",
    val tournamentName: String = "Status",
    val isMonitoring: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val games: List<Game> = emptyList(),
    val players: List<Player> = emptyList(),
    val watchlist: List<Player> = emptyList(),
    val lastUpdate: String = "",
    val selectedPane: SelectedPane = SelectedPane.RESULTS
)
