package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.model.DocumentSectionEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.TranscriptSegmentEntity
import com.example.export.DocumentExportManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun verifyAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("GuzNotara", appName)
    }

    @Test
    fun testRoomDatabaseMeetingAndSegmentsCrud() = runBlocking {
        val meetingDao = database.meetingDao()
        val meetingId = UUID.randomUUID().toString()

        val meeting = MeetingEntity(
            id = meetingId,
            title = "Rapat Arsitektur Mobile GuzNotara",
            durationSeconds = 120L,
            status = "RECORDED"
        )
        meetingDao.insertMeeting(meeting)

        val retrievedMeeting = meetingDao.getMeetingByIdOnce(meetingId)
        assertNotNull(retrievedMeeting)
        assertEquals("Rapat Arsitektur Mobile GuzNotara", retrievedMeeting?.title)

        // Add segments
        val segments = listOf(
            TranscriptSegmentEntity(
                id = UUID.randomUUID().toString(),
                meetingId = meetingId,
                speaker = "Speaker 1",
                startTimeMs = 0L,
                endTimeMs = 4000L,
                text = "Halo semuanya, mari kita mulai rapat integrasi."
            ),
            TranscriptSegmentEntity(
                id = UUID.randomUUID().toString(),
                meetingId = meetingId,
                speaker = "Speaker 2",
                startTimeMs = 4500L,
                endTimeMs = 9000L,
                text = "Siap, semua pipeline audio dan Room sudah diverifikasi."
            )
        )
        meetingDao.insertSegments(segments)

        val retrievedSegments = meetingDao.getSegmentsForMeetingOnce(meetingId)
        assertEquals(2, retrievedSegments.size)

        // Rename speaker
        meetingDao.renameSpeaker(meetingId, "Speaker 1", "Budi Santoso")
        val updatedSegments = meetingDao.getSegmentsForMeetingOnce(meetingId)
        assertEquals("Budi Santoso", updatedSegments[0].speaker)

        // Delete meeting
        meetingDao.deleteMeetingById(meetingId)
        val remainingMeeting = meetingDao.getMeetingByIdOnce(meetingId)
        assertEquals(null, remainingMeeting)
    }

    @Test
    fun testDocumentExportManagerGeneratesValidMarkdownAndTxt() {
        val exportManager = DocumentExportManager(context)
        val meeting = MeetingEntity(
            id = "test-id",
            title = "Sprint Planning Meeting",
            durationSeconds = 180L,
            status = "COMPLETED"
        )
        val sections = listOf(
            DocumentSectionEntity(
                id = "sec-1",
                meetingId = "test-id",
                title = "Agenda Utama",
                content = "Pembahasan modul ekspor dan transkrip.",
                sectionOrder = 0
            )
        )
        val segments = listOf(
            TranscriptSegmentEntity(
                id = "seg-1",
                meetingId = "test-id",
                speaker = "Budi",
                startTimeMs = 0L,
                endTimeMs = 3000L,
                text = "Meeting dimulai sekarang."
            )
        )

        val mdFile = exportManager.exportToMarkdown(meeting, sections, segments)
        assertTrue(mdFile.exists())
        assertTrue(mdFile.length() > 0)
        val mdContent = mdFile.readText()
        assertTrue(mdContent.contains("# Sprint Planning Meeting"))
        assertTrue(mdContent.contains("Agenda Utama"))
        assertTrue(mdContent.contains("Meeting dimulai sekarang."))

        val txtFile = exportManager.exportToTxt(meeting, sections, segments)
        assertTrue(txtFile.exists())
        assertTrue(txtFile.length() > 0)
        val txtContent = txtFile.readText()
        assertTrue(txtContent.contains("SPRINT PLANNING MEETING"))
    }
}
