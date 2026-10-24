package com.example.data.repository

import com.example.data.dao.MeetingDao
import com.example.data.model.DocumentSectionEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.TranscriptSegmentEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

class MeetingRepository(private val meetingDao: MeetingDao) {

    val allMeetings: Flow<List<MeetingEntity>> = meetingDao.getAllMeetings()

    fun getMeetingById(id: String): Flow<MeetingEntity?> = meetingDao.getMeetingById(id)

    suspend fun getMeetingByIdOnce(id: String): MeetingEntity? = meetingDao.getMeetingByIdOnce(id)

    suspend fun insertMeeting(meeting: MeetingEntity) = meetingDao.insertMeeting(meeting)

    suspend fun updateMeeting(meeting: MeetingEntity) = meetingDao.updateMeeting(meeting)

    suspend fun deleteMeeting(meeting: MeetingEntity) {
        if (meeting.audioFilePath.isNotBlank()) {
            val audioFile = File(meeting.audioFilePath)
            if (audioFile.exists()) {
                audioFile.delete()
            }
        }
        meetingDao.deleteMeeting(meeting)
    }

    suspend fun deleteAudioOnly(meeting: MeetingEntity) {
        if (meeting.audioFilePath.isNotBlank()) {
            val audioFile = File(meeting.audioFilePath)
            if (audioFile.exists()) {
                audioFile.delete()
            }
            meetingDao.updateMeeting(meeting.copy(audioFilePath = ""))
        }
    }

    // Segments
    fun getSegments(meetingId: String): Flow<List<TranscriptSegmentEntity>> =
        meetingDao.getSegmentsForMeeting(meetingId)

    suspend fun getSegmentsOnce(meetingId: String): List<TranscriptSegmentEntity> =
        meetingDao.getSegmentsForMeetingOnce(meetingId)

    suspend fun saveSegments(meetingId: String, segments: List<TranscriptSegmentEntity>) {
        meetingDao.deleteSegmentsForMeeting(meetingId)
        meetingDao.insertSegments(segments)
    }

    suspend fun updateSegment(segment: TranscriptSegmentEntity) =
        meetingDao.updateSegment(segment)

    suspend fun renameSpeaker(meetingId: String, oldName: String, newName: String) =
        meetingDao.renameSpeaker(meetingId, oldName, newName)

    // Sections
    fun getSections(meetingId: String): Flow<List<DocumentSectionEntity>> =
        meetingDao.getSectionsForMeeting(meetingId)

    suspend fun getSectionsOnce(meetingId: String): List<DocumentSectionEntity> =
        meetingDao.getSectionsForMeetingOnce(meetingId)

    suspend fun saveSections(meetingId: String, sections: List<DocumentSectionEntity>) {
        meetingDao.deleteSectionsForMeeting(meetingId)
        meetingDao.insertSections(sections)
    }

    suspend fun updateSection(section: DocumentSectionEntity) =
        meetingDao.updateSection(section)
}
