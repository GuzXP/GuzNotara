package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.TranscriptionPipeline
import com.example.data.AppDatabase
import com.example.data.model.MeetingEntity
import com.example.data.repository.MeetingRepository
import com.example.recording.AudioRecordManager
import com.example.recording.RecordingForegroundService
import com.example.recording.RecordingSessionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class RecordingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MeetingRepository
    private val audioRecordManager: AudioRecordManager
    private val transcriptionPipeline: TranscriptionPipeline

    val sessionState: StateFlow<RecordingSessionState>

    private val _navigateToWorkspaceEvent = MutableSharedFlow<String>()
    val navigateToWorkspaceEvent: SharedFlow<String> = _navigateToWorkspaceEvent.asSharedFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MeetingRepository(database.meetingDao())
        audioRecordManager = AudioRecordManager(application)
        transcriptionPipeline = TranscriptionPipeline(application, repository)
        sessionState = audioRecordManager.sessionState
    }

    fun startRecording(title: String) {
        val meetingId = UUID.randomUUID().toString()
        val finalTitle = title.ifBlank { "Rapat ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}" }

        val audioFile = audioRecordManager.startRecording(meetingId)
        if (audioFile != null) {
            RecordingForegroundService.start(getApplication(), finalTitle)
            viewModelScope.launch {
                val newMeeting = MeetingEntity(
                    id = meetingId,
                    title = finalTitle,
                    createdAt = System.currentTimeMillis(),
                    durationSeconds = 0L,
                    audioFilePath = audioFile.absolutePath,
                    status = "RECORDED"
                )
                repository.insertMeeting(newMeeting)
            }
        }
    }

    fun pauseRecording() {
        audioRecordManager.pauseRecording()
    }

    fun resumeRecording() {
        audioRecordManager.resumeRecording()
    }

    fun stopRecording() {
        val duration = sessionState.value.durationSeconds
        val meetingId = sessionState.value.currentMeetingId
        val audioFile = audioRecordManager.stopRecording()

        RecordingForegroundService.stop(getApplication())

        if (meetingId != null && audioFile != null) {
            viewModelScope.launch {
                val existing = repository.getMeetingByIdOnce(meetingId)
                if (existing != null) {
                    val updated = existing.copy(
                        durationSeconds = duration,
                        audioFilePath = audioFile.absolutePath,
                        status = "RECORDED"
                    )
                    repository.updateMeeting(updated)

                    // Kick off transcription & structuring pipeline
                    launch {
                        transcriptionPipeline.processMeeting(updated)
                    }

                    _navigateToWorkspaceEvent.emit(meetingId)
                }
            }
        }
    }

    fun cancelRecording() {
        val meetingId = sessionState.value.currentMeetingId
        audioRecordManager.cancelRecording()
        RecordingForegroundService.stop(getApplication())

        if (meetingId != null) {
            viewModelScope.launch {
                val meeting = repository.getMeetingByIdOnce(meetingId)
                if (meeting != null) {
                    repository.deleteMeeting(meeting)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (sessionState.value.isRecording) {
            audioRecordManager.stopRecording()
            RecordingForegroundService.stop(getApplication())
        }
    }
}
