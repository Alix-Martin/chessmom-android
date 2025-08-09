package com.alixmartin.chessmonitor.data.repository

import android.util.Log
import com.alixmartin.chessmonitor.data.database.GameDao
import com.alixmartin.chessmonitor.data.model.Game
import com.alixmartin.chessmonitor.data.model.Player
import com.alixmartin.chessmonitor.data.model.TournamentPageData
import com.alixmartin.chessmonitor.data.network.ChessApi
import kotlinx.coroutines.flow.Flow
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TournamentRepository @Inject constructor(
    private val api: ChessApi,
    private val gameDao: GameDao
) {

    fun getGamesForTournament(tournamentId: Int, round: Int): Flow<List<Game>> {
        return gameDao.getGamesForTournament(tournamentId, round)
    }

    suspend fun fetchTournamentGames(tournamentId: Int, round: Int): Result<TournamentPageData> {
        return try {
            val url = ChessApi.buildTournamentUrl(tournamentId, round)
            val response = api.getTournamentPage(url)

            if (response.isSuccessful) {
                val html = response.body()?.string() ?: ""
                val tournamentData = parseTournamentPageDataFromHtml(html, tournamentId, round)

                // Cache the games in the database, updating finished timestamps
                updateGamesWithFinishedTimestamps(tournamentData.games, tournamentId, round)

                Result.success(tournamentData)
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseTournamentPageDataFromHtml(html: String, tournamentId: Int, round: Int): TournamentPageData {
        val games = mutableListOf<Game>()
        val playersMap = mutableMapOf<String, Player>()
        var tournamentName = ""

        val doc = Jsoup.parse(html)

        // Extract tournament name
        val titleElement = doc.selectFirst("tr.papi_titre td")
        if (titleElement != null) {
            tournamentName = titleElement.html().substringBefore("<br>").trim()
        }

        val gameRows = doc.select("tr.papi_liste_c, tr.papi_liste_f")

        for (row in gameRows) {
            val cells = row.select("td")
            if (cells.size >= 8) {
                val game = parseGameRow(cells, tournamentId, round)
                if (game != null) {
                    games.add(game)
                    addPlayer(playersMap, game.player1Name, game.player1Rating, game.player1Points)
                    addPlayer(playersMap, game.player2Name, game.player2Rating, game.player2Points)
                }
            }
        }

        val players = playersMap.values.toList()
        return TournamentPageData(games, players, tournamentName)
    }

    private fun addPlayer(players: MutableMap<String, Player>, name: String, rating: String, points: String) {
        if (name.isNotBlank() && !name.equals("EXEMPT", ignoreCase = true)) {
            players[name] = Player(
                name = name,
                rating = rating.filter { it.isDigit() }.toIntOrNull() ?: 0,
                points = formatPoints(points)
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

    private fun parseGameRow(cells: List<Element>, tournamentId: Int, round: Int): Game? {
        return try {
            val tableNum = cells[0].text().trim()
            val player1Points = cells[1].text().trim()
            val player1Name = cells[2].text().trim()
            val player1Rating = cells[3].text().trim()
            val rawResult = cells[4].text()
            val result = rawResult.trim()
            val player2Name = cells[5].text().trim()
            val player2Rating = cells[6].text().trim()
            val player2Points = cells[7].text().trim()

            // Create unique ID for the game
            val gameId = "${tournamentId}_${round}_${tableNum}"

            Game(
                id = gameId,
                tableNum = tableNum.toInt(),
                player1Name = player1Name,
                player1Rating = player1Rating,
                player1Points = player1Points,
                result = result,
                rawResult = rawResult,
                player2Name = player2Name,
                player2Rating = player2Rating,
                player2Points = player2Points,
                tournamentId = tournamentId,
                round = round
            )
        } catch (e: Exception) {
            Log.e("TournamentRepository", "Failed to parse game row", e)
            null
        }
    }

    suspend fun getFinishedGames(tournamentId: Int, round: Int): List<Game> {
        return gameDao.getGamesForTournamentSync(tournamentId, round)
            .filter { it.isFinished() }
    }

    suspend fun clearTournamentData(tournamentId: Int, round: Int) {
        gameDao.clearTournamentGames(tournamentId, round)
    }

    private suspend fun updateGamesWithFinishedTimestamps(newGames: List<Game>, tournamentId: Int, round: Int) {
        val existingGames = gameDao.getGamesForTournamentSync(tournamentId, round)
        val existingGamesMap = existingGames.associateBy { it.id }
        val currentTime = System.currentTimeMillis()

        val updatedGames = newGames.map { newGame ->
            val existingGame = existingGamesMap[newGame.id]
            
            when {
                // Game wasn't finished before but is now finished
                existingGame != null && !existingGame.isFinished() && newGame.isFinished() -> {
                    newGame.copy(finishedTimestamp = currentTime)
                }
                // Game was already finished, keep the original finished timestamp
                existingGame != null && existingGame.isFinished() && newGame.isFinished() -> {
                    newGame.copy(finishedTimestamp = existingGame.finishedTimestamp)
                }
                // New game that's already finished
                existingGame == null && newGame.isFinished() -> {
                    newGame.copy(finishedTimestamp = currentTime)
                }
                // Game is not finished or other cases
                else -> newGame
            }
        }

        gameDao.insertGames(updatedGames)
    }
}
