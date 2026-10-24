package com.example.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AudioRecordManager(private val context: Context) {

    private val _sessionState = MutableStateFlow(RecordingSessionState())
    val sessionState: StateFlow<RecordingSessionState> = _sessionState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var startTimeMillis: Long = 0L
    private var accumulatedSeconds: Long = 0L
    private var outputFile: File? = null

    companion object {
        private const val TAG = "AudioRecordManager"
        const val MAX_WAVEFORM_POINTS = 50
    }

    init {
        updateAvailableStorage()
    }

    fun updateAvailableStorage() {
        try {
            val stat = StatFs(context.filesDir.path)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val mbAvailable = bytesAvailable / (1024 * 1024)
            _sessionState.update { it.copy(availableSpaceMb = mbAvailable) }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking storage", e)
        }
    }

    @Synchronized
    fun startRecording(meetingId: String): File? {
        updateAvailableStorage()
        if (_sessionState.value.availableSpaceMb < 10) {
            _sessionState.update { it.copy(error = "Ruang penyimpanan tidak mencukupi (< 10 MB)") }
            return null
        }

        val recordingsDir = File(context.filesDir, "recordings")
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs()
        }

        val file = File(recordingsDir, "meeting_${meetingId}_${System.currentTimeMillis()}.m4a")
        outputFile = file

        return try {
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            startTimeMillis = System.currentTimeMillis()
            accumulatedSeconds = 0L

            _sessionState.update {
                it.copy(
                    isRecording = true,
                    isPaused = false,
                    durationSeconds = 0L,
                    currentAudioFilePath = file.absolutePath,
                    currentMeetingId = meetingId,
                    amplitudes = emptyList(),
                    error = null
                )
            }

            startTimerAndAmplitudeTracker()
            file
        } catch (e: IOException) {
            Log.e(TAG, "prepare() failed", e)
            _sessionState.update { it.copy(error = "Gagal menginisialisasi mikrofon: ${e.message}") }
            cleanup()
            null
        } catch (e: IllegalStateException) {
            Log.e(TAG, "start() failed", e)
            _sessionState.update { it.copy(error = "Mikrofon tidak dapat diakses: ${e.message}") }
            cleanup()
            null
        }
    }

    @Synchronized
    fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && _sessionState.value.isRecording && !_sessionState.value.isPaused) {
            try {
                mediaRecorder?.pause()
                timerJob?.cancel()
                _sessionState.update { it.copy(isPaused = true) }
            } catch (e: Exception) {
                Log.e(TAG, "pause() failed", e)
            }
        }
    }

    @Synchronized
    fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && _sessionState.value.isRecording && _sessionState.value.isPaused) {
            try {
                mediaRecorder?.resume()
                _sessionState.update { it.copy(isPaused = false) }
                startTimerAndAmplitudeTracker()
            } catch (e: Exception) {
                Log.e(TAG, "resume() failed", e)
            }
        }
    }

    @Synchronized
    fun stopRecording(): File? {
        val file = outputFile
        cleanup()
        _sessionState.update {
            it.copy(
                isRecording = false,
                isPaused = false
            )
        }
        return file
    }

    @Synchronized
    fun cancelRecording() {
        val file = outputFile
        cleanup()
        if (file != null && file.exists()) {
            file.delete()
        }
        _sessionState.update {
            it.copy(
                isRecording = false,
                isPaused = false,
                durationSeconds = 0L,
                currentAudioFilePath = null,
                currentMeetingId = null,
                amplitudes = emptyList()
            )
        }
    }

    private fun startTimerAndAmplitudeTracker() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _sessionState.value.isRecording && !_sessionState.value.isPaused) {
                delay(100)
                accumulatedSeconds += 1

                val amp = try {
                    val max = mediaRecorder?.maxAmplitude ?: 0
                    (max / 32767f).coerceIn(0.05f, 1.0f)
                } catch (e: Exception) {
                    0.05f
                }

                _sessionState.update { current ->
                    val newAmps = (current.amplitudes + amp).takeLast(MAX_WAVEFORM_POINTS)
                    current.copy(
                        durationSeconds = accumulatedSeconds / 10,
                        currentAmplitude = amp,
                        amplitudes = newAmps
                    )
                }
            }
        }
    }

    private fun cleanup() {
        timerJob?.cancel()
        timerJob = null
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder", e)
        } finally {
            mediaRecorder = null
        }
    }
}
