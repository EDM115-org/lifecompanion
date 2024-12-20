package org.lifecompanion.phonecontrolapp.services

import android.app.Service
import android.content.Intent
import android.os.FileObserver
import android.os.IBinder
import android.util.Log
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class JSONProcessingService : Service() {

    companion object {
        private const val TAG = "JSONProcessingService"
        private const val CHANNEL_ID = "JSONProcessingServiceChannel"
    }

    private val inputDirPath: String by lazy { File(filesDir, "input").absolutePath }
    private val outputDirPath: String by lazy { File(filesDir, "output").absolutePath }

    private var watcherThread: Thread? = null
    private var pollingInterval = 1000L // Default polling interval in milliseconds

    override fun onCreate() {
        super.onCreate()

        Notify.createNotificationChannel("JSON Processing Service", CHANNEL_ID, this)
        startForegroundService()

        val inputDir = File(inputDirPath)
        if (!inputDir.exists()) inputDir.mkdirs()
        val outputDir = File(outputDirPath)
        if (!outputDir.exists()) outputDir.mkdirs()

        startFileWatcher()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        watcherThread?.interrupt()
        stopForeground(true)
    }

    private fun startForegroundService() {
        val notification = Notify.createNotification("JSON Processing Service", CHANNEL_ID, this)
        startForeground(1, notification)
    }

    private fun startFileWatcher() {
        watcherThread = Thread {
            val inputDir = File(inputDirPath)
            if (!inputDir.exists()) inputDir.mkdirs()

            while (!Thread.currentThread().isInterrupted) {
                try {
                    val files = inputDir.listFiles { _, name -> name.endsWith(".json") }
                    files?.forEach { file ->
                        processFile(file)
                        file.delete()
                    }

                    Thread.sleep(pollingInterval)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing files: ${e.message}")
                }
            }
        }.apply { start() }
    }

    private fun processFile(file: File) {
        Log.i(TAG, "Processing file: ${file.name}")
        try {
            val content = FileInputStream(file).bufferedReader().use { it.readText() }
            val json = JSONObject(content)
            Log.i(TAG, "Processing JSON: $json")

            if (!validateJson(json)) {
                Log.e(TAG, "Invalid JSON: ${file.name}")
                return
            }

            routeJsonToService(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing file: ${file.name}", e)
        }
    }

    private fun validateJson(json: JSONObject): Boolean {
        return json.has("sender") && json.has("type") && json.has("subtype") && json.has("data")
    }

    private fun routeJsonToService(json: JSONObject) {
        when (json.optString("type")) {
            "call" -> startService(Intent(this, CallService::class.java).apply { putExtra("json", json.toString()) })
            "sms" -> startService(Intent(this, SMSService::class.java).apply { putExtra("json", json.toString()) })
            "system" -> startService(Intent(this, SystemService::class.java).apply { putExtra("json", json.toString()) })
            else -> Log.e(TAG, "Unknown type: ${json.optString("type")}")
        }
    }

    private fun writeResponse(requestId: String, responseData: JSONObject) {
        val outputDir = File(outputDirPath)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val responseFile = File(outputDir, "$requestId.json")

        try {
            FileOutputStream(responseFile).use { it.write(responseData.toString().toByteArray()) }
            Log.i(TAG, "Response written to file: $responseFile")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write response for requestId: $requestId", e)
        }
    }

    public fun setPollingInterval(interval: Long) {
        pollingInterval = interval
    }
}
