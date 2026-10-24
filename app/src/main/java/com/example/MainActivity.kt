package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.RecordingStudioScreen
import com.example.ui.screens.WorkspaceScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MeetingViewModel
import com.example.viewmodel.RecordingViewModel

sealed interface Screen {
    object Library : Screen
    object RecordingStudio : Screen
    data class Workspace(val meetingId: String) : Screen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GuzNotaraApp()
                }
            }
        }
    }
}

@Composable
fun GuzNotaraApp() {
    val meetingViewModel: MeetingViewModel = viewModel()
    val recordingViewModel: RecordingViewModel = viewModel()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Library) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (recordAudioGranted) {
            currentScreen = Screen.RecordingStudio
        }
    }

    val requestPermissionsAndOpenStudio = {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Handle back button behavior per guideline
    BackHandler(enabled = currentScreen !is Screen.Library) {
        when (currentScreen) {
            is Screen.RecordingStudio -> {
                recordingViewModel.cancelRecording()
                currentScreen = Screen.Library
            }
            is Screen.Workspace -> {
                currentScreen = Screen.Library
            }
            Screen.Library -> {
                // Exit app
            }
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Library -> {
                LibraryScreen(
                    viewModel = meetingViewModel,
                    onStartNewRecording = requestPermissionsAndOpenStudio,
                    onOpenMeeting = { meetingId ->
                        meetingViewModel.selectMeeting(meetingId)
                        currentScreen = Screen.Workspace(meetingId)
                    }
                )
            }
            is Screen.RecordingStudio -> {
                RecordingStudioScreen(
                    viewModel = recordingViewModel,
                    onNavigateBack = { currentScreen = Screen.Library },
                    onRecordingSaved = { meetingId ->
                        meetingViewModel.selectMeeting(meetingId)
                        currentScreen = Screen.Workspace(meetingId)
                    }
                )
            }
            is Screen.Workspace -> {
                WorkspaceScreen(
                    viewModel = meetingViewModel,
                    onNavigateBack = { currentScreen = Screen.Library }
                )
            }
        }
    }
}
