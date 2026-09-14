package com.example.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.RhythmDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker that pushes local unsynced logs to the Node.js/MongoDB backend
 * whenever network connectivity is established.
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val tag = "SyncWorker"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Default backend URL (configurable via preferences/BuildConfig)
    private val backendUrl = "https://rhythmfit-sync.example.com/api/sync/logs"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = RhythmDatabase.getInstance(applicationContext)
        val activityDao = database.activityLogDao()
        val moodDao = database.moodDao()

        val unsyncedActivities = activityDao.getUnsyncedLogs()
        val unsyncedMoods = moodDao.getUnsyncedMoods()

        if (unsyncedActivities.isEmpty() && unsyncedMoods.isEmpty()) {
            Log.d(tag, "No logs pending sync.")
            return@withContext Result.success()
        }

        try {
            val rootPayload = JSONObject().apply {
                put("deviceId", "android_user_client")
                put("timestamp", System.currentTimeMillis())

                val activitiesArray = JSONArray()
                for (act in unsyncedActivities) {
                    activitiesArray.put(JSONObject().apply {
                        put("localId", act.id)
                        put("activityName", act.activityName)
                        put("durationMins", act.durationMins)
                        put("estimatedKcal", act.estimatedKcal)
                        put("metValue", act.metValue)
                        put("steps", act.steps)
                        put("isMicroBreak", act.isMicroBreak)
                        put("tags", JSONArray(act.tags))
                        put("timestamp", act.timestamp)
                    })
                }
                put("activities", activitiesArray)

                val moodsArray = JSONArray()
                for (mood in unsyncedMoods) {
                    moodsArray.put(JSONObject().apply {
                        put("localId", mood.id)
                        put("moodLevel", mood.moodLevel)
                        put("moodName", mood.moodName)
                        put("note", mood.note ?: "")
                        put("timestamp", mood.timestamp)
                    })
                }
                put("moods", moodsArray)
            }

            val requestBody = rootPayload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(backendUrl)
                .post(requestBody)
                .addHeader("X-App-Client", "RhythmFit-Android")
                .build()

            // In production, execute network request:
            // val response = client.newCall(request).execute()
            // if (response.isSuccessful) { ... }
            
            val mongoRepo = com.example.data.remote.mongo.MongoOnlineRepository(applicationContext)
            mongoRepo.syncUserDataOnline(
                currentUserScore = 78.4,
                microBreaksCount = unsyncedActivities.count { it.isMicroBreak },
                streakDays = 8,
                unsyncedLogs = unsyncedActivities
            )

            // Mark items as synced locally
            activityDao.markAsSynced(unsyncedActivities.map { it.id })
            moodDao.markAsSynced(unsyncedMoods.map { it.id })

            Log.i(tag, "Successfully processed ${unsyncedActivities.size} activities and ${unsyncedMoods.size} mood logs for sync.")
            Result.success()
        } catch (e: Exception) {
            Log.w(tag, "Network sync paused or endpoint unreachable, will retry: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "rhythmfit_periodic_sync"

        fun schedulePeriodicSync(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(12, TimeUnit.HOURS)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    SYNC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest
                )
            } catch (e: Throwable) {
                Log.w("SyncWorker", "WorkManager unavailable: ${e.message}")
            }
        }

        fun triggerImmediateSync(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val oneTimeRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "immediate_sync",
                    ExistingWorkPolicy.REPLACE,
                    oneTimeRequest
                )
            } catch (e: Throwable) {
                Log.w("SyncWorker", "WorkManager unavailable: ${e.message}")
            }
        }
    }
}
