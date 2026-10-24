package com.example.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.TranscriptionPipeline
import com.example.data.AppDatabase
import com.example.data.model.DocumentSectionEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.TranscriptSegmentEntity
import com.example.data.repository.MeetingRepository
import com.example.export.DocumentExportManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MeetingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MeetingRepository
    private val transcriptionPipeline: TranscriptionPipeline
    private val exportManager: DocumentExportManager

    val allMeetings: StateFlow<List<MeetingEntity>>

    val searchQuery = MutableStateFlow("")
    val filteredMeetings: StateFlow<List<MeetingEntity>>

    private val _selectedMeetingId = MutableStateFlow<String?>(null)
    val selectedMeetingId: StateFlow<String?> = _selectedMeetingId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMeeting: StateFlow<MeetingEntity?> = _selectedMeetingId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getMeetingById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSegments: StateFlow<List<TranscriptSegmentEntity>> = _selectedMeetingId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getSegments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedSections: StateFlow<List<DocumentSectionEntity>> = _selectedMeetingId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getSections(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isReprocessing = MutableStateFlow(false)
    val isReprocessing: StateFlow<Boolean> = _isReprocessing.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MeetingRepository(database.meetingDao())
        transcriptionPipeline = TranscriptionPipeline(application, repository)
        exportManager = DocumentExportManager(application)

        allMeetings = repository.allMeetings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        filteredMeetings = combine(allMeetings, searchQuery) { meetings, query ->
            if (query.isBlank()) {
                meetings
            } else {
                meetings.filter { it.title.contains(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun selectMeeting(meetingId: String) {
        _selectedMeetingId.value = meetingId
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun renameSpeaker(oldSpeaker: String, newSpeaker: String) {
        val meetingId = _selectedMeetingId.value ?: return
        if (oldSpeaker.isBlank() || newSpeaker.isBlank() || oldSpeaker == newSpeaker) return

        viewModelScope.launch {
            repository.renameSpeaker(meetingId, oldSpeaker, newSpeaker)
            _toastMessage.value = "Pembicara diubah menjadi '$newSpeaker'"
        }
    }

    fun updateSegmentText(segment: TranscriptSegmentEntity, newText: String) {
        viewModelScope.launch {
            repository.updateSegment(segment.copy(text = newText, isEdited = true))
        }
    }

    fun updateSectionContent(section: DocumentSectionEntity, newContent: String) {
        viewModelScope.launch {
            repository.updateSection(section.copy(content = newContent))
        }
    }

    fun reprocessDocument(customInstruction: String) {
        val meeting = selectedMeeting.value ?: return
        viewModelScope.launch {
            _isReprocessing.value = true
            val result = transcriptionPipeline.reprocessDocument(meeting, customInstruction)
            _isReprocessing.value = false
            if (result.isSuccess) {
                _toastMessage.value = "Dokumen berhasil diperbarui dengan AI"
            } else {
                _toastMessage.value = "Gagal memproses ulang: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun retryProcessing() {
        val meeting = selectedMeeting.value ?: return
        viewModelScope.launch {
            _isReprocessing.value = true
            val result = transcriptionPipeline.processMeeting(meeting)
            _isReprocessing.value = false
            if (result.isSuccess) {
                _toastMessage.value = "Transkripsi berhasil diproses"
            } else {
                _toastMessage.value = "Gagal memproses: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteMeeting(meeting: MeetingEntity) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
            if (_selectedMeetingId.value == meeting.id) {
                _selectedMeetingId.value = null
            }
            _toastMessage.value = "Rapat telah dihapus"
        }
    }

    fun deleteAudioOnly(meeting: MeetingEntity) {
        viewModelScope.launch {
            repository.deleteAudioOnly(meeting)
            _toastMessage.value = "Berkas audio dihapus (dokumen tetap tersimpan)"
        }
    }

    fun exportPdf(): Intent? {
        val meeting = selectedMeeting.value ?: return null
        val sections = selectedSections.value
        val segments = selectedSegments.value

        return try {
            val file = exportManager.exportToPdf(meeting, sections, segments)
            exportManager.shareExportedFile(file, "application/pdf", "Notula Rapat: ${meeting.title}")
        } catch (e: Exception) {
            _toastMessage.value = "Gagal mengekspor PDF: ${e.message}"
            null
        }
    }

    fun exportMarkdown(): Intent? {
        val meeting = selectedMeeting.value ?: return null
        val sections = selectedSections.value
        val segments = selectedSegments.value

        return try {
            val file = exportManager.exportToMarkdown(meeting, sections, segments)
            exportManager.shareExportedFile(file, "text/markdown", "Dokumen Markdown: ${meeting.title}")
        } catch (e: Exception) {
            _toastMessage.value = "Gagal mengekspor Markdown: ${e.message}"
            null
        }
    }

    fun exportTxt(): Intent? {
        val meeting = selectedMeeting.value ?: return null
        val sections = selectedSections.value
        val segments = selectedSegments.value

        return try {
            val file = exportManager.exportToTxt(meeting, sections, segments)
            exportManager.shareExportedFile(file, "text/plain", "Transkrip Teks: ${meeting.title}")
        } catch (e: Exception) {
            _toastMessage.value = "Gagal mengekspor TXT: ${e.message}"
            null
        }
    }

    fun loadSampleMeeting() {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val sampleMeeting = MeetingEntity(
                id = id,
                title = "Diskusi Desain Sistem & Sprint Planning GuzNotara",
                createdAt = System.currentTimeMillis() - (3600 * 1000 * 2),
                durationSeconds = 245L,
                status = "COMPLETED",
                language = "id-ID (Campur EN)"
            )
            repository.insertMeeting(sampleMeeting)
            transcriptionPipeline.processMeeting(sampleMeeting)
            selectMeeting(id)
            _toastMessage.value = "Sampel rapat berhasil dimuat"
        }
    }
}
