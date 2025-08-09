package com.alixmartin.chessmonitor.data.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface ChessApi {
    @GET
    suspend fun getTournamentPage(@Url url: String): Response<ResponseBody>
    
    companion object {
        const val BASE_URL = "https://www.echecs.asso.fr/"
        
        fun buildTournamentUrl(tournamentId: Int, round: Int): String {
            val roundStr = String.format("%02d", round)
            return "${BASE_URL}Resultats.aspx?URL=Tournois/Id/$tournamentId/$tournamentId&Action=$roundStr"
        }
    }
}
