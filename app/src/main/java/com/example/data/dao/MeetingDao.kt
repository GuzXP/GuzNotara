package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DocumentSectionEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.TranscriptSegmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY createdAt DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :id LIMIT 1")
    fun getMeetingById(id: String): Flow<MeetingEntity?>

    @Query("SELECT * FROM meetings WHERE id = :id LIMIT 1")
    suspend fun getMeetingByIdOnce(id: String): MeetingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity)

    @Update
    suspend fun updateMeeting(meeting: MeetingEntity)

    @Delete
    suspend fun deleteMeeting(meeting: MeetingEntity)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: String)

    // Transcript Segments
    @Query("SELECT * FROM transcript_segments WHERE meetingId = :meetingId ORDER BY startTimeMs ASC")
    fun getSegmentsForMeeting(meetingId: String): Flow<List<TranscriptSegmentEntity>>

    @Query("SELECT * FROM transcript_segments WHERE meetingId = :meetingId ORDER BY startTimeMs ASC")
    suspend fun getSegmentsForMeetingOnce(meetingId: String): List<TranscriptSegmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(segments: List<TranscriptSegmentEntity>)

    @Update
    suspend fun updateSegment(segment: TranscriptSegmentEntity)

    @Query("UPDATE transcript_segments SET speaker = :newName WHERE meetingId = :meetingId AND speaker = :oldName")
    suspend fun renameSpeaker(meetingId: String, oldName: String, newName: String)

    @Query("DELETE FROM transcript_segments WHERE meetingId = :meetingId")
    suspend fun deleteSegmentsForMeeting(meetingId: String)

    // Document Sections
    @Query("SELECT * FROM document_sections WHERE meetingId = :meetingId ORDER BY sectionOrder ASC")
    fun getSectionsForMeeting(meetingId: String): Flow<List<DocumentSectionEntity>>

    @Query("SELECT * FROM document_sections WHERE meetingId = :meetingId ORDER BY sectionOrder ASC")
    suspend fun getSectionsForMeetingOnce(meetingId: String): List<DocumentSectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<DocumentSectionEntity>)

    @Update
    suspend fun updateSection(section: DocumentSectionEntity)

    @Query("DELETE FROM document_sections WHERE meetingId = :meetingId")
    suspend fun deleteSectionsForMeeting(meetingId: String)
}
