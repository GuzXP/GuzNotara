package com.example.recording

data class RecordingSessionState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val durationSeconds: Long = 0L,
    val currentAmplitude: Float = 0f,
    val amplitudes: List<Float> = emptyList(),
    val currentAudioFilePath: String? = null,
    val currentMeetingId: String? = null,
    val availableSpaceMb: Long = 0L,
    val error: String? = null
)
