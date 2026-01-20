package com.bfrb.tracker.wear.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bfrb.tracker.wear.presentation.MainActivity
import kotlin.math.sqrt

class MovementDetectionService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var vibrator: Vibrator

    // Movement detection parameters
    private val movementThreshold = 15.0f // Adjust based on testing
    private val gyroThreshold = 2.0f // Radians per second
    private val detectionWindow = 500L // milliseconds
    private var lastDetectionTime = 0L

    // For pattern detection
    private val accelerometerReadings = mutableListOf<FloatArray>()
    private val maxReadings = 50 // Keep last 50 readings (about 1 second at ~50Hz)

    companion object {
        private const val TAG = "MovementDetection"
        private const val NOTIFICATION_CHANNEL_ID = "bfrb_detection"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        // Register sensor listeners
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        gyroscope?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> handleAccelerometerData(it)
                Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(it)
            }
        }
    }

    private fun handleAccelerometerData(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Store reading
        accelerometerReadings.add(floatArrayOf(x, y, z))
        if (accelerometerReadings.size > maxReadings) {
            accelerometerReadings.removeAt(0)
        }

        // Calculate magnitude (subtract gravity)
        val magnitude = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

        // Check if movement exceeds threshold
        if (magnitude > movementThreshold) {
            checkForRepetitivePattern()
        }
    }

    private fun handleGyroscopeData(event: SensorEvent) {
        val rotationX = event.values[0]
        val rotationY = event.values[1]
        val rotationZ = event.values[2]

        val rotationMagnitude = sqrt(
            rotationX * rotationX +
            rotationY * rotationY +
            rotationZ * rotationZ
        )

        // Detect significant rotation
        if (rotationMagnitude > gyroThreshold) {
            // Combined with accelerometer data for better detection
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastDetectionTime > detectionWindow) {
                // Potential BFRB detected
                checkForRepetitivePattern()
            }
        }
    }

    private fun checkForRepetitivePattern() {
        val currentTime = System.currentTimeMillis()

        // Prevent too frequent detections
        if (currentTime - lastDetectionTime < detectionWindow) {
            return
        }

        // Simple pattern detection: Check if we have enough readings
        if (accelerometerReadings.size >= 10) {
            // Calculate variance to detect repetitive motion
            val variance = calculateVariance()

            // If variance is low, it suggests repetitive motion
            if (variance < 5.0f) { // Adjust threshold based on testing
                lastDetectionTime = currentTime
                onMovementDetected()
            }
        }
    }

    private fun calculateVariance(): Float {
        if (accelerometerReadings.isEmpty()) return 0f

        val magnitudes = accelerometerReadings.map { reading ->
            sqrt(reading[0] * reading[0] + reading[1] * reading[1] + reading[2] * reading[2])
        }

        val mean = magnitudes.average().toFloat()
        val variance = magnitudes.map { (it - mean) * (it - mean) }.average().toFloat()

        return variance
    }

    private fun onMovementDetected() {
        Log.d(TAG, "Repetitive movement detected!")

        // Vibrate to alert user
        triggerVibration()

        // Show confirmation dialog on MainActivity
        showConfirmationDialog()
    }

    private fun triggerVibration() {
        val vibrationEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VibrationEffect.createWaveform(
                longArrayOf(0, 200, 100, 200), // pattern: wait, vibrate, wait, vibrate
                -1 // don't repeat
            )
        } else {
            null
        }

        if (vibrationEffect != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500) // 500ms vibration for older devices
        }
    }

    private fun showConfirmationDialog() {
        // Launch MainActivity with confirmation dialog
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("SHOW_CONFIRMATION", true)
        }
        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Service destroyed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "BFRB Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Detects repetitive movements"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("BFRB Tracker")
            .setContentText("Movement detection active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
