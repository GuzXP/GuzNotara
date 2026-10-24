# TEST PLAN — GuzNotara

## 1. Functional Test Scenarios
- **Microphone Permissions**:
  - Request dialog displayed when recording begins.
  - Graceful informative rationale if permission denied.
- **Recording Controls**:
  - Start, pause, resume, and stop flow.
  - File created in `filesDir/recordings/` with non-zero byte size.
  - Real-time amplitude values update waveform visualization.
- **Background Persistence**:
  - Notification displayed with running timer and status.
  - Recording continues across activity lifecycle changes.

## 2. Transcription & Document Integrity
- **Verbatim Retention**:
  - Indonesian informal expressions, repeated words, and small talk preserved in raw transcript.
  - Mixed Indonesian/English technical terminology preserved correctly.
- **AI Document Structuring**:
  - All substantive discussion chapters retained.
  - Action items extracted only when supported by the transcript.
  - Timestamps and speaker links remain verifiable.

## 3. Storage & Export Verification
- **Room Database CRUD**:
  - Meeting deletion cascades to segments and document sections.
  - Global speaker renaming updates all associated segments.
- **Document Exporting**:
  - PDF export generates valid paginated multi-page document with header and styled sections.
  - Markdown (.md) and plain text (.txt) exports produce correctly encoded UTF-8 files.
  - Android Sharesheet successfully opens target file URI via FileProvider.
