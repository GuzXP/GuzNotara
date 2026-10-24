package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentSectionEntity
import com.example.data.model.TranscriptSegmentEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LiveRecordingRed
import com.example.viewmodel.MeetingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    viewModel: MeetingViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val meeting by viewModel.selectedMeeting.collectAsStateWithLifecycle()
    val segments by viewModel.selectedSegments.collectAsStateWithLifecycle()
    val sections by viewModel.selectedSections.collectAsStateWithLifecycle()
    val isReprocessing by viewModel.isReprocessing.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showReprocessDialog by remember { mutableStateOf(false) }
    var speakerToRename by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = meeting?.title ?: "Memuat Dokumen...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "${segments.size} segmen percakapan • ${sections.size} bab terstruktur",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Beranda")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showReprocessDialog = true },
                        modifier = Modifier.testTag("ai_reprocess_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Tata Ulang AI",
                            tint = CyanAccent
                        )
                    }
                    IconButton(
                        onClick = { showExportSheet = true },
                        modifier = Modifier.testTag("export_menu_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Ekspor Rapat")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header (Dokumen Rapat vs Transkrip Lengkap)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Dokumen Rapat", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    modifier = Modifier.testTag("tab_document")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Transkrip Lengkap", fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                    modifier = Modifier.testTag("tab_transcript")
                )
            }

            if (isReprocessing) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Sedang menyusun dokumen dengan Gemini AI...", fontSize = 13.sp)
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> DocumentView(
                    sections = sections,
                    onUpdateSection = { sec, newText -> viewModel.updateSectionContent(sec, newText) },
                    onRequestReprocess = { showReprocessDialog = true }
                )
                1 -> TranscriptView(
                    segments = segments,
                    onSpeakerClick = { speakerToRename = it },
                    onUpdateSegment = { seg, newText -> viewModel.updateSegmentText(seg, newText) }
                )
            }
        }
    }

    // Export Bottom Sheet
    if (showExportSheet) {
        ExportBottomSheet(
            onDismiss = { showExportSheet = false },
            onExportPdf = {
                showExportSheet = false
                val intent = viewModel.exportPdf()
                intent?.let { context.startActivity(Intent.createChooser(it, "Bagikan PDF Notula")) }
            },
            onExportMarkdown = {
                showExportSheet = false
                val intent = viewModel.exportMarkdown()
                intent?.let { context.startActivity(Intent.createChooser(it, "Bagikan Markdown Rapat")) }
            },
            onExportTxt = {
                showExportSheet = false
                val intent = viewModel.exportTxt()
                intent?.let { context.startActivity(Intent.createChooser(it, "Bagikan Teks Transkrip")) }
            }
        )
    }

    // Reprocess AI Dialog
    if (showReprocessDialog) {
        ReprocessDialog(
            onDismiss = { showReprocessDialog = false },
            onSubmit = { instruction ->
                showReprocessDialog = false
                viewModel.reprocessDocument(instruction)
            }
        )
    }

    // Rename Speaker Dialog
    speakerToRename?.let { oldName ->
        RenameSpeakerDialog(
            currentName = oldName,
            onDismiss = { speakerToRename = null },
            onConfirm = { newName ->
                viewModel.renameSpeaker(oldName, newName)
                speakerToRename = null
            }
        )
    }
}

@Composable
fun DocumentView(
    sections: List<DocumentSectionEntity>,
    onUpdateSection: (DocumentSectionEntity, String) -> Unit,
    onRequestReprocess: () -> Unit
) {
    if (sections.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Dokumen Belum Tersedia",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Susun dokumen terstruktur dari transkrip percakapan menggunakan AI.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequestReprocess) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Susun Dokumen dengan AI")
                }
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sections, key = { it.id }) { section ->
                SectionCard(
                    section = section,
                    onSave = { onUpdateSection(section, it) }
                )
            }
        }
    }
}

@Composable
fun SectionCard(
    section: DocumentSectionEntity,
    onSave: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedContent by remember(section.content) { mutableStateOf(section.content) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = section.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        if (isEditing) {
                            onSave(editedContent)
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Simpan" else "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = editedContent,
                    onValueChange = { editedContent = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Text(
                    text = section.content,
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun TranscriptView(
    segments: List<TranscriptSegmentEntity>,
    onSpeakerClick: (String) -> Unit,
    onUpdateSegment: (TranscriptSegmentEntity, String) -> Unit
) {
    if (segments.isEmpty()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(32.dp)) {
            Text("Transkrip belum diproses", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(segments, key = { it.id }) { segment ->
                TranscriptSegmentItem(
                    segment = segment,
                    onSpeakerClick = { onSpeakerClick(segment.speaker) },
                    onSaveText = { onUpdateSegment(segment, it) }
                )
            }
        }
    }
}

@Composable
fun TranscriptSegmentItem(
    segment: TranscriptSegmentEntity,
    onSpeakerClick: () -> Unit,
    onSaveText: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(segment.text) { mutableStateOf(segment.text) }

    val timeLabel = remember(segment.startTimeMs, segment.endTimeMs) {
        val t1 = formatTime(segment.startTimeMs)
        val t2 = formatTime(segment.endTimeMs)
        "[$t1 - $t2]"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Speaker Pill (Tappable to Rename)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onSpeakerClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = segment.speaker,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeLabel,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                onSaveText(textValue)
                                isEditing = false
                            } else {
                                isEditing = true
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Simpan" else "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Text(
                    text = segment.text,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportBottomSheet(
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit,
    onExportMarkdown: () -> Unit,
    onExportTxt: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pilih Format Ekspor Notula",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Dokumen akan dihasilkan dengan format rapi dan siap dibagikan.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PDF
            FilledTonalButton(
                onClick = onExportPdf,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("export_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = LiveRecordingRed)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ekspor Dokumen PDF Resmi (.pdf)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Markdown
            FilledTonalButton(
                onClick = onExportMarkdown,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("export_markdown_button")
            ) {
                Icon(Icons.Default.Subject, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ekspor Markdown (.md)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Plain Text
            FilledTonalButton(
                onClick = onExportTxt,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("export_txt_button")
            ) {
                Icon(Icons.Default.TextFields, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Ekspor Teks Sederhana (.txt)", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ReprocessDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var instruction by remember {
        mutableStateOf("Tata ulang dokumen dengan menonjolkan keputusan teknis, daftar tugas, dan pertanyaan penting.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tata Ulang Format dengan AI") },
        text = {
            Column {
                Text(
                    text = "Beri petunjuk khusus kepada Gemini AI untuk menyusun ulang bab dokumen rapat sesuai kebutuhan Anda tanpa mengurangi substansi pembicaraan.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    placeholder = { Text("Instruksi penataan...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(instruction) },
                modifier = Modifier.testTag("submit_reprocess_btn")
            ) {
                Text("Proses Ulang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun RenameSpeakerDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Nama Pembicara") },
        text = {
            Column {
                Text(
                    text = "Nama '$currentName' akan diubah di seluruh transkrip percakapan rapat ini secara otomatis.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama Baru Pembicara") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newName) },
                enabled = newName.isNotBlank() && newName != currentName,
                modifier = Modifier.testTag("confirm_rename_speaker_btn")
            ) {
                Text("Perbarui Semua")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

private fun formatTime(millis: Long): String {
    val totalSec = millis / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
