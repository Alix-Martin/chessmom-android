package com.alixmartin.chessmonitor.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import com.alixmartin.chessmonitor.worker.MonitoringWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MonitoringService : Service() {
    
    @Inject
    lateinit var workManager: WorkManager
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tournamentId = intent?.getIntExtra(EXTRA_TOURNAMENT_ID, -1) ?: -1
        val round = intent?.getIntExtra(EXTRA_ROUND, -1) ?: -1
        
        if (tournamentId != -1 && round != -1) {
            startMonitoring(tournamentId, round)
        }
        
        return START_STICKY
    }
    
    private fun startMonitoring(tournamentId: Int, round: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<MonitoringWorker>(
            repeatInterval = 2,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    MonitoringWorker.KEY_TOURNAMENT_ID to tournamentId,
                    MonitoringWorker.KEY_ROUND to round
                )
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "tournament_monitoring",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    companion object {
        const val EXTRA_TOURNAMENT_ID = "tournament_id"
        const val EXTRA_ROUND = "round"
    }
}
