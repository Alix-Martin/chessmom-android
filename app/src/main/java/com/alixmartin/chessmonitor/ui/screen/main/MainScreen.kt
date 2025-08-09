package com.alixmartin.chessmonitor.ui.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alixmartin.chessmonitor.data.model.Game
import com.alixmartin.chessmonitor.data.model.Player

enum class SelectedPane {
    RESULTS, RANKINGS, WATCHLIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watchlistNames by viewModel.watchlistNames.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.isMonitoring) {
            Button(
                onClick = viewModel::stopMonitoring,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Monitoring")
            }
        } else {
            TournamentInputCard(
                tournamentId = uiState.tournamentId,
                round = uiState.round,
                isMonitoring = uiState.isMonitoring,
                onTournamentIdChange = viewModel::updateTournamentId,
                onRoundChange = viewModel::updateRound,
                onStartMonitoring = viewModel::startMonitoring,
                onStopMonitoring = viewModel::stopMonitoring
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Section
        StatusCard(
            tournamentName = uiState.tournamentName,
            isMonitoring = uiState.isMonitoring,
            connectionStatus = uiState.connectionStatus,
            lastUpdate = uiState.lastUpdate,
            totalGames = uiState.games.size,
            finishedGames = uiState.games.count { it.isFinished() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isMonitoring) {
            Column(modifier = Modifier.fillMaxHeight()) {
                TabRow(selectedTabIndex = uiState.selectedPane.ordinal) {
                    Tab(
                        selected = uiState.selectedPane == SelectedPane.RESULTS,
                        onClick = { viewModel.onPaneSelected(SelectedPane.RESULTS) },
                        text = { Text("Results") }
                    )
                    Tab(
                        selected = uiState.selectedPane == SelectedPane.RANKINGS,
                        onClick = { viewModel.onPaneSelected(SelectedPane.RANKINGS) },
                        text = { Text("Players") }
                    )
                    Tab(
                        selected = uiState.selectedPane == SelectedPane.WATCHLIST,
                        onClick = { viewModel.onPaneSelected(SelectedPane.WATCHLIST) },
                        text = { Text("Watch List") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                when (uiState.selectedPane) {
                    SelectedPane.RESULTS -> GamesListCard(
                        games = uiState.games.filter { it.isFinished() },
                        watchlistNames = watchlistNames,
                        round = uiState.round,
                        modifier = Modifier.fillMaxHeight()
                    )
                    SelectedPane.RANKINGS -> PlayerListCard(
                        players = uiState.players,
                        watchlistNames = watchlistNames,
                        onAddToWatchlist = viewModel::addToWatchlist,
                        onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                        modifier = Modifier.fillMaxHeight()
                    )
                    SelectedPane.WATCHLIST -> WatchListCard(
                        players = uiState.players.filter { watchlistNames.contains(it.name) },
                        onRemoveFromWatchlist = viewModel::removeFromWatchlist,
                        modifier = Modifier.fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerListCard(
    players: List<Player>,
    watchlistNames: Set<String>,
    onAddToWatchlist: (Player) -> Unit,
    onRemoveFromWatchlist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Player Ranking",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (players.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No players loaded yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = players,
                        key = { player -> player.name }
                    ) { player ->
                        PlayerListItem(
                            player = player,
                            isInWatchlist = watchlistNames.contains(player.name),
                            onAddToWatchlist = onAddToWatchlist,
                            onRemoveFromWatchlist = onRemoveFromWatchlist
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchListCard(
    players: List<Player>,
    onRemoveFromWatchlist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Watch List",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (players.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No players in watch list",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = players,
                        key = { player -> player.name }
                    ) { player ->
                        WatchListItem(
                            player = player,
                            onRemoveFromWatchlist = onRemoveFromWatchlist
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WatchListItem(
    player: Player,
    onRemoveFromWatchlist: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "${player.points} pts (${player.rating})",
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = { onRemoveFromWatchlist(player.name) }
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Remove from watchlist",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun PlayerListItem(
    player: Player,
    isInWatchlist: Boolean,
    onAddToWatchlist: (Player) -> Unit,
    onRemoveFromWatchlist: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isInWatchlist) FontWeight.Bold else FontWeight.Medium,
            color = if (isInWatchlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${player.points} pts (${player.rating})",
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = {
                if (isInWatchlist) {
                    onRemoveFromWatchlist(player.name)
                } else {
                    onAddToWatchlist(player)
                }
            }
        ) {
            Icon(
                imageVector = if (isInWatchlist) Icons.Default.Remove else Icons.Default.Add,
                contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                tint = if (isInWatchlist) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentInputCard(
    tournamentId: String,
    round: String,
    isMonitoring: Boolean,
    onTournamentIdChange: (String) -> Unit,
    onRoundChange: (String) -> Unit,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tournament Configuration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tournamentId,
                    onValueChange = onTournamentIdChange,
                    label = { Text("Tournament ID") },
                    placeholder = { Text("66882") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = !isMonitoring
                )

                OutlinedTextField(
                    value = round,
                    onValueChange = onRoundChange,
                    label = { Text("Round") },
                    placeholder = { Text("8") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    enabled = !isMonitoring
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = if (isMonitoring) onStopMonitoring else onStartMonitoring,
                modifier = Modifier.fillMaxWidth(),
                colors = if (isMonitoring) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isMonitoring) "Stop Monitoring" else "Start Monitoring")
            }
        }
    }
}

@Composable
fun StatusCard(
    tournamentName: String,
    isMonitoring: Boolean,
    connectionStatus: ConnectionStatus,
    lastUpdate: String,
    totalGames: Int,
    finishedGames: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = tournamentName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Monitoring:")
                Text(
                    text = if (isMonitoring) "Active" else "Stopped",
                    color = if (isMonitoring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Connection:")
                Text(
                    text = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> "Connected"
                        ConnectionStatus.ERROR -> "Error"
                        ConnectionStatus.DISCONNECTED -> "Disconnected"
                        ConnectionStatus.NETWORK_ERROR -> "Network Error"
                    },
                    color = when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> MaterialTheme.colorScheme.primary
                        ConnectionStatus.ERROR, ConnectionStatus.NETWORK_ERROR -> MaterialTheme.colorScheme.error
                        ConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            if (isMonitoring) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Games:")
                    Text("$finishedGames/$totalGames finished")
                }
            }

            if (lastUpdate.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Last Update:")
                    Text(lastUpdate)
                }
            }
        }
    }
}

@Composable
fun GamesListCard(
    games: List<Game>,
    watchlistNames: Set<String>,
    round: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Round $round results",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (games.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No games loaded yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = games,
                        key = { game -> game.id }
                    ) { game ->
                        GameResultItem(
                            game = game,
                            watchlistNames = watchlistNames
                        )
                    }
                }
            }
        }
    }
}

private fun formatPoints(points: String): String {
    var formattedPoints = points.replace("½", ".5").replace(" ", "")
    if (formattedPoints.startsWith(".")) {
        formattedPoints = "0$formattedPoints"
    }
    return formattedPoints
}

@Composable
fun GameResultItem(game: Game, watchlistNames: Set<String> = emptySet()) {
    val isRecentlyUpdated = game.finishedTimestamp?.let { finishedTime ->
        val currentTime = System.currentTimeMillis()
        val fiveMinutesInMs = 5 * 60 * 1000
        currentTime - finishedTime <= fiveMinutesInMs
    } ?: false
    
    val hasWatchedPlayer = watchlistNames.contains(game.player1Name) || 
                          watchlistNames.contains(game.player2Name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    isRecentlyUpdated -> Modifier.border(
                        width = 2.dp,
                        color = Color(0xFFE6CC00), // Golden yellow border for recent updates
                        shape = MaterialTheme.shapes.medium
                    )
                    hasWatchedPlayer -> Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary, // Blue border for watched players
                        shape = MaterialTheme.shapes.medium
                    )
                    else -> Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isRecentlyUpdated -> Color(0xFFFFFBE6) // Pale yellow for recent updates
                hasWatchedPlayer -> Color(0xFFE3F2FD) // Light blue background for watched players
                game.isFinished() -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val player1Name = game.player1Name
                    val player1RatingPoints = if (game.player1Name.equals("EXEMPT", ignoreCase = true)) {
                        ""
                    } else {
                        val rating = game.player1Rating.filter { it.isDigit() }
                        val points = formatPoints(game.player1Points)
                        "($rating) $points"
                    }
                    Text(
                        text = player1Name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (watchlistNames.contains(game.player1Name)) FontWeight.Bold else FontWeight.Medium,
                        color = if (watchlistNames.contains(game.player1Name)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (player1RatingPoints.isNotEmpty()) {
                        Text(
                            text = player1RatingPoints,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = game.result.ifEmpty { "—" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    val player2Name = game.player2Name
                    val player2RatingPoints = if (game.player2Name.equals("EXEMPT", ignoreCase = true)) {
                        ""
                    } else {
                        val rating = game.player2Rating.filter { it.isDigit() }
                        val points = formatPoints(game.player2Points)
                        "($rating) $points"
                    }
                    Text(
                        text = player2Name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (watchlistNames.contains(game.player2Name)) FontWeight.Bold else FontWeight.Medium,
                        color = if (watchlistNames.contains(game.player2Name)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                    if (player2RatingPoints.isNotEmpty()) {
                        Text(
                            text = player2RatingPoints,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerInfo(name: String, rating: String, points: String) {
    Column {
        Text(
            text = "$name ($points)",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Rating: $rating",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun GameItem(game: Game) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PlayerInfo(
            name = game.player1Name,
            rating = game.player1Rating,
            points = game.player1Points
        )

        Text(
            text = game.result,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        PlayerInfo(
            name = game.player2Name,
            rating = game.player2Rating,
            points = game.player2Points
        )
    }
}

enum class ConnectionStatus {
    CONNECTED, ERROR, DISCONNECTED, NETWORK_ERROR
}
