package com.example.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.DocumentSectionEntity
import com.example.data.model.MeetingEntity
import com.example.data.model.TranscriptSegmentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentExportManager(private val context: Context) {

    private val exportsDir: File
        get() {
            val dir = File(context.filesDir, "exports")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun exportToPdf(
        meeting: MeetingEntity,
        sections: List<DocumentSectionEntity>,
        segments: List<TranscriptSegmentEntity>
    ): File {
        val sanitizedTitle = sanitizeFilename(meeting.title)
        val file = File(exportsDir, "${sanitizedTitle}_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width in PostScript points
        val pageHeight = 842 // Standard A4 height
        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val sectionHeadingPaint = Paint().apply {
            color = Color.parseColor("#1E3A8A")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val speakerPaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val timestampPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            isAntiAlias = true
        }

        val dividerPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPos = margin

        fun checkPageBreak(requiredHeight: Float) {
            if (yPos + requiredHeight > pageHeight - margin) {
                // Draw footer
                canvas.drawText("Halaman $pageNumber — GuzNotara Workspace", margin, pageHeight - 20f, subtitlePaint)
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPos = margin
            }
        }

        // Header
        canvas.drawText("GuzNotara — Notula Rapat Resmi", margin, yPos, subtitlePaint)
        yPos += 18f
        canvas.drawText(meeting.title, margin, yPos, titlePaint)
        yPos += 14f

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(meeting.createdAt))
        val durMin = meeting.durationSeconds / 60
        val durSec = meeting.durationSeconds % 60
        val metaStr = "Waktu: $dateStr | Durasi: ${durMin}m ${durSec}s | Bahasa: ${meeting.language}"
        canvas.drawText(metaStr, margin, yPos, subtitlePaint)
        yPos += 10f
        canvas.drawLine(margin, yPos, pageWidth - margin, yPos, dividerPaint)
        yPos += 20f

        // 1. Structured Sections
        for (section in sections) {
            checkPageBreak(50f)
            canvas.drawText(section.title, margin, yPos, sectionHeadingPaint)
            yPos += 16f

            val lines = wrapText(section.content, bodyPaint, contentWidth)
            for (line in lines) {
                checkPageBreak(14f)
                canvas.drawText(line, margin, yPos, bodyPaint)
                yPos += 14f
            }
            yPos += 12f
        }

        // 2. Verbatim Transcript Section
        if (segments.isNotEmpty()) {
            checkPageBreak(50f)
            yPos += 8f
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, dividerPaint)
            yPos += 18f
            canvas.drawText("Transkrip Percakapan Lengkap (Verbatim)", margin, yPos, sectionHeadingPaint)
            yPos += 16f

            for (seg in segments) {
                checkPageBreak(30f)
                val timeStr = "[${formatTime(seg.startTimeMs)} - ${formatTime(seg.endTimeMs)}]"
                canvas.drawText(timeStr, margin, yPos, timestampPaint)
                val timeWidth = timestampPaint.measureText(timeStr) + 8f
                canvas.drawText(seg.speaker + ":", margin + timeWidth, yPos, speakerPaint)
                yPos += 13f

                val segLines = wrapText(seg.text, bodyPaint, contentWidth - 10f)
                for (line in segLines) {
                    checkPageBreak(14f)
                    canvas.drawText(line, margin + 10f, yPos, bodyPaint)
                    yPos += 13f
                }
                yPos += 6f
            }
        }

        // Final footer
        canvas.drawText("Halaman $pageNumber — GuzNotara Workspace", margin, pageHeight - 20f, subtitlePaint)
        document.finishPage(page)

        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    fun exportToMarkdown(
        meeting: MeetingEntity,
        sections: List<DocumentSectionEntity>,
        segments: List<TranscriptSegmentEntity>
    ): File {
        val sanitizedTitle = sanitizeFilename(meeting.title)
        val file = File(exportsDir, "${sanitizedTitle}_${System.currentTimeMillis()}.md")

        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(meeting.createdAt))

        val sb = StringBuilder()
        sb.append("# ${meeting.title}\n\n")
        sb.append("**GuzNotara — Notula Rapat & Workspace**\n\n")
        sb.append("- **Tanggal**: $dateStr\n")
        sb.append("- **Durasi**: ${meeting.durationSeconds / 60}m ${meeting.durationSeconds % 60}s\n")
        sb.append("- **Status**: ${meeting.status}\n\n")
        sb.append("---\n\n")

        sb.append("## Bagian Dokumen Terstruktur\n\n")
        for (sec in sections) {
            sb.append("### ${sec.title}\n\n")
            sb.append("${sec.content}\n\n")
        }

        if (segments.isNotEmpty()) {
            sb.append("---\n\n")
            sb.append("## Transkrip Percakapan Lengkap (Verbatim)\n\n")
            for (seg in segments) {
                val t1 = formatTime(seg.startTimeMs)
                val t2 = formatTime(seg.endTimeMs)
                sb.append("**[$t1 - $t2] ${seg.speaker}**:\n")
                sb.append("> ${seg.text}\n\n")
            }
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    fun exportToTxt(
        meeting: MeetingEntity,
        sections: List<DocumentSectionEntity>,
        segments: List<TranscriptSegmentEntity>
    ): File {
        val sanitizedTitle = sanitizeFilename(meeting.title)
        val file = File(exportsDir, "${sanitizedTitle}_${System.currentTimeMillis()}.txt")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(meeting.createdAt))

        val sb = StringBuilder()
        sb.append("============================================================\n")
        sb.append("                    ${meeting.title.uppercase()}\n")
        sb.append("                    GuzNotara Meeting Workspace\n")
        sb.append("============================================================\n")
        sb.append("Tanggal : $dateStr\n")
        sb.append("Durasi  : ${meeting.durationSeconds / 60} menit ${meeting.durationSeconds % 60} detik\n\n")

        sb.append("--- DOKUMEN RAPAT TERSTRUKTUR ---\n\n")
        for (sec in sections) {
            sb.append("[${sec.title}]\n")
            sb.append("${sec.content}\n\n")
        }

        if (segments.isNotEmpty()) {
            sb.append("--- TRANSKRIP VERBATIM ---\n\n")
            for (seg in segments) {
                sb.append("[${formatTime(seg.startTimeMs)}] ${seg.speaker}: ${seg.text}\n")
            }
        }

        file.writeText(sb.toString(), Charsets.UTF_8)
        return file
    }

    fun shareExportedFile(file: File, mimeType: String, subject: String): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (para in paragraphs) {
            if (para.isEmpty()) {
                result.add("")
                continue
            }
            val words = para.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) result.add(currentLine)
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) result.add(currentLine)
        }
        return result
    }

    private fun formatTime(millis: Long): String {
        val totalSec = millis / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return String.format("%02d:%02d", min, sec)
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace("[^a-zA-Z0-9_-]".toRegex(), "_").take(30)
    }
}
