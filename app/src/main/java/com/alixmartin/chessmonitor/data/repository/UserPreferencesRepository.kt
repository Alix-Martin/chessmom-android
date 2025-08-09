package com.alixmartin.chessmonitor.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val TOURNAMENT_ID = stringPreferencesKey("tournament_id")
    private val ROUND = stringPreferencesKey("round")
    private val WATCH_LIST = stringSetPreferencesKey("watch_list")

    val tournamentIdFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TOURNAMENT_ID] ?: ""
        }

    val roundFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ROUND] ?: ""
        }

    val watchListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[WATCH_LIST] ?: emptySet()
        }

    suspend fun saveTournamentId(tournamentId: String) {
        context.dataStore.edit { settings ->
            settings[TOURNAMENT_ID] = tournamentId
        }
    }

    suspend fun saveRound(round: String) {
        context.dataStore.edit { settings ->
            settings[ROUND] = round
        }
    }

    suspend fun addToWatchList(playerName: String) {
        context.dataStore.edit { settings ->
            val currentWatchList = settings[WATCH_LIST] ?: emptySet()
            settings[WATCH_LIST] = currentWatchList + playerName
        }
    }

    suspend fun removeFromWatchList(playerName: String) {
        context.dataStore.edit { settings ->
            val currentWatchList = settings[WATCH_LIST] ?: emptySet()
            settings[WATCH_LIST] = currentWatchList - playerName
        }
    }
}
