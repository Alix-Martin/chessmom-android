package com.alixmartin.chessmonitor.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.alixmartin.chessmonitor.R
import com.alixmartin.chessmonitor.data.model.Game
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    init {
        createNotificationChannel()
    }
    
    fun showGameFinishedNotification(game: Game, watchedPlayerNames: Set<String> = emptySet()) {
        val title = if (watchedPlayerNames.isNotEmpty()) {
            val watchedPlayers = mutableListOf<String>()
            if (watchedPlayerNames.contains(game.player1Name)) watchedPlayers.add(game.player1Name)
            if (watchedPlayerNames.contains(game.player2Name)) watchedPlayers.add(game.player2Name)
            
            "Watched Player Game Finished!"
        } else {
            "Game Finished!"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("${game.player1Name} ${game.result} ${game.player2Name}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${game.player1Name} (${game.player1Rating}) ${game.result} ${game.player2Name} (${game.player2Rating})")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(
                game.id.hashCode(),
                notification
            )
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Game Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for finished chess games"
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val CHANNEL_ID = "chess_monitor_channel"
    }
}
