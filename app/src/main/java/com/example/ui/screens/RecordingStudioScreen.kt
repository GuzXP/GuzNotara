package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.LiveRecordingRed
import com.example.viewmodel.RecordingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingStudioScreen(
    viewModel: RecordingViewModel,
    onNavigateBack: () -> Unit,
    onRecordingSaved: (String) -> Unit
) {
    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    var meetingTitleInput by remember { mutableStateOf("") }
    var hasStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateToWorkspaceEvent.collect { meetingId ->
            onRecordingSaved(meetingId)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val timeFormatted = remember(sessionState.durationSeconds) {
        val totalSec = sessionState.durationSeconds
        val hours = totalSec / 3600
        val minutes = (totalSec % 3600) / 60
        val seconds = totalSec % 60
        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Studio Rekaman", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (sessionState.isRecording) {
                                viewModel.cancelRecording()
                            }
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup Studio")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
        ) {
            // Meeting Title Field
            OutlinedTextField(
                value = meetingTitleInput,
                onValueChange = { meetingTitleInput = it },
                label = { Text("Judul Rapat") },
                placeholder = { Text("Contoh: Rapat Evaluasi Arsitektur") },
                singleLine = true,
                enabled = !sessionState.isRecording,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meeting_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Storage and background service indicator
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SdStorage,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ruang Penyimpanan: ${sessionState.availableSpaceMb} MB • Foreground Service Aktif",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Status Indicator Pill
            Surface(
                color = when {
                    sessionState.isPaused -> AmberWarning.copy(alpha = 0.15f)
                    sessionState.isRecording -> LiveRecordingRed.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    sessionState.isPaused -> AmberWarning
                                    sessionState.isRecording -> LiveRecordingRed
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                            .alpha(if (sessionState.isRecording && !sessionState.isPaused) pulseAlpha else 1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            sessionState.isPaused -> "Perekaman Dijeda"
                            sessionState.isRecording -> "Merekam Suara Langsung..."
                            else -> "Siap Merekam"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            sessionState.isPaused -> AmberWarning
                            sessionState.isRecording -> LiveRecordingRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Big Digital Timer
            Text(
                text = timeFormatted,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 2.sp,
                modifier = Modifier.testTag("recording_timer_text")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Live Waveform Visualizer
            WaveformVisualizer(
                amplitudes = sessionState.amplitudes,
                isRecording = sessionState.isRecording,
                isPaused = sessionState.isPaused,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            if (sessionState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = sessionState.error ?: "",
                    color = LiveRecordingRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            if (!sessionState.isRecording) {
                Button(
                    onClick = {
                        hasStarted = true
                        viewModel.startRecording(meetingTitleInput)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LiveRecordingRed),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_record_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mulai Perekaman", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel
                    OutlinedButton(
                        onClick = {
                            viewModel.cancelRecording()
                            onNavigateBack()
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("cancel_recording_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Batalkan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Pause / Resume
                    FilledTonalButton(
                        onClick = {
                            if (sessionState.isPaused) {
                                viewModel.resumeRecording()
                            } else {
                                viewModel.pauseRecording()
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("pause_resume_btn")
                    ) {
                        Icon(
                            imageVector = if (sessionState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (sessionState.isPaused) "Lanjutkan" else "Jeda",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Stop & Save
                    Button(
                        onClick = { viewModel.stopRecording() },
                        colors = ButtonDefaults.buttonColors(containerColor = LiveRecordingRed),
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("stop_save_recording_btn")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Selesai & Simpan", modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
