package com.bfrb.tracker.wear.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import com.bfrb.tracker.wear.service.MovementDetectionService

class MainActivity : ComponentActivity() {
    private var isServiceRunning by mutableStateOf(false)
    private var showConfirmationDialog by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startDetectionService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if launched from service to show confirmation
        if (intent?.getBooleanExtra("SHOW_CONFIRMATION", false) == true) {
            showConfirmationDialog = true
        }

        setContent {
            MaterialTheme {
                WearApp(
                    isRunning = isServiceRunning,
                    showDialog = showConfirmationDialog,
                    onToggleDetection = { toggleDetection() },
                    onDialogResponse = { wasConfirmed ->
                        handleConfirmation(wasConfirmed)
                        showConfirmationDialog = false
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new intent when activity is already running
        if (intent.getBooleanExtra("SHOW_CONFIRMATION", false)) {
            showConfirmationDialog = true
        }
    }

    private fun toggleDetection() {
        if (isServiceRunning) {
            stopDetectionService()
        } else {
            checkPermissionsAndStart()
        }
    }

    private fun checkPermissionsAndStart() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED -> {
                startDetectionService()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    private fun startDetectionService() {
        Intent(this, MovementDetectionService::class.java).also { intent ->
            startForegroundService(intent)
            isServiceRunning = true
        }
    }

    private fun stopDetectionService() {
        Intent(this, MovementDetectionService::class.java).also { intent ->
            stopService(intent)
            isServiceRunning = false
        }
    }

    private fun handleConfirmation(wasConfirmed: Boolean) {
        // TODO: Send confirmation to phone app via DataClient
        // For now, just log it
        android.util.Log.d("BFRBTracker", "User confirmed: $wasConfirmed")
    }

    fun showMovementConfirmation() {
        showConfirmationDialog = true
    }
}

@Composable
fun WearApp(
    isRunning: Boolean,
    showDialog: Boolean,
    onToggleDetection: () -> Unit,
    onDialogResponse: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (showDialog) {
            ConfirmationDialog(onResponse = onDialogResponse)
        } else {
            MainScreen(isRunning = isRunning, onToggle = onToggleDetection)
        }
    }
}

@Composable
fun MainScreen(isRunning: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BFRB Tracker",
            style = MaterialTheme.typography.title3,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Chip(
            onClick = onToggle,
            label = {
                Text(
                    text = if (isRunning) "Stop Detection" else "Start Detection",
                    textAlign = TextAlign.Center
                )
            },
            colors = ChipDefaults.primaryChipColors(
                backgroundColor = if (isRunning) Color(0xFFB00020) else Color(0xFF03DAC6)
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isRunning) "Active" else "Inactive",
            style = MaterialTheme.typography.caption1,
            color = if (isRunning) Color.Green else Color.Gray
        )
    }
}

@Composable
fun ConfirmationDialog(onResponse: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Was this the repetitive behavior?",
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip(
                onClick = { onResponse(true) },
                label = { Text("Yes") },
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = Color(0xFF4CAF50)
                )
            )

            Chip(
                onClick = { onResponse(false) },
                label = { Text("No") },
                colors = ChipDefaults.primaryChipColors(
                    backgroundColor = Color(0xFFF44336)
                )
            )
        }
    }
}
