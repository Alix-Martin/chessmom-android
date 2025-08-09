package com.alixmartin.chessmonitor.data.model

data class TournamentPageData(
    val games: List<Game>,
    val players: List<Player>,
    val tournamentName: String
)
