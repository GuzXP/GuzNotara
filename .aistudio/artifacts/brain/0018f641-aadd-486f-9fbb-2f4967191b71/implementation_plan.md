# GuzNotara — Native Android AI Meeting Transcription & Document Workspace

GuzNotara is a native Android meeting recorder, verbatim transcription engine, and structured document workspace inspired by Meeting.ai, Uniscribe, and TurboScribe. It captures high-fidelity audio, preserves conversations completely without lossy summarization (retaining small talk, banter, interruptions, and questions), and formats them into an organized, professional meeting document with synchronized source verification and native multi-format export (PDF, Markdown, TXT).

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following product decisions were confirmed during the planning phase:

- **Application Identity**: Named **GuzNotara**, reflecting a dignified, focused workspace for recording and note-taking (*Notara* from *notula / catatan rapat*).
- **Primary Language Focus**: **Bahasa Indonesia with code-switching English** (common in tech, corporate, and academic meetings in Indonesia). Prompts, terminology dictionaries, and speech segmentation are tuned for mixed bilingual conversation.
- **Default Document View**: **Structured Document with Verbatim Transcript Drawer / Dual Toggle**, ensuring users can read a polished, segmented meeting record while instantly cross-referencing verbatim speech snippets and original timestamps.
- **Data Integrity & Non-Summarization Mandate**: The AI will *never* compress the transcript into a mere 3-bullet summary unless explicitly asked. It organizes, punctuates, identifies speakers, and labels discussion chapters while preserving every substantive exchange, tangent, small talk, and question.

---

## 1. Overview & Core Concept

### What It Does
1. **Local-First Audio Recording**: Records conversations via a resilient foreground microphone service, writing directly to disk in segmented chunks with real-time waveform visualization, session persistence, and recovery against app termination.
2. **Hybrid Speech-to-Text Pipeline**: Converts audio recordings into timestamped transcript segments using Gemini audio processing (with chunked base64 audio transport via REST) and Android speech fallback capabilities.
3. **Lossless Document Organization**: Formats raw conversational transcripts into cohesive, editorial documents (Introductions, Agenda, Discussion Chapters, Q&A, Clarifications, and Action Items) while retaining 100% of substantive statements and colloquial dialogue.
4. **Interactive Document Workspace**: Enables speaker renaming, inline segment editing, verbatim vs. readable toggles, custom formatting refinement, and keyword search.
5. **Native Document Export**: Exports to genuine Android Canvas-rendered PDF documents, styled Markdown (`.md`), and raw text (`.txt`), with native Android Sharesheet integration.

### Target Audience & Persona
- **Indonesian & Regional Professionals, IT Students, & Researchers**: Meeting leaders, student project groups, and journalists who need reliable records of fast-paced discussions in mixed Indonesian/English without missing nuances or action items.

---

## 2. User Experience & Visual Design

### Aesthetic Direction
An **Editorial Audio-Productivity** aesthetic: calm, typography-first, high contrast, and distraction-free. Avoids generic AI dashboard tropes, neon purple gradients, and floating decorative bubbles in favor of a utilitarian, Swiss-inspired design system with warm neutral tones and precise visual feedback.

### Color Palette & Visual Theme
- **Theme**: Warm Editorial Light & Deep Slate Dark
- **Surface**: Warm off-white (`#F8F9FA`) in light mode; Deep obsidian slate (`#121417`) in dark mode.
- **Primary Accent**: Deep Forest Indigo (`#1E3A5F`) with Electric Cobalt highlight (`#2563EB`) for active recording states and primary actions.
- **Recording State**: Energetic Carmine Red (`#DC2626`) for active recording, pulse indicators, and live waveform meters.
- **Typography Scale**: Clean, legible editorial hierarchy using high-legibility geometric sans for display headers and robust monospace for timestamps and speaker tags.

### Key Screens & Navigation Flow
```
┌────────────────────────────────────────────────────────┐
│                      GuzNotara                         │
└────────────────────────────────────────────────────────┘
                           │
       ┌───────────────────┴───────────────────┐
       ▼                                       ▼
┌──────────────┐                       ┌──────────────┐
│ Library View │ ── [Tap FAB +] ─────> │  Recording   │
│ (Recents,    │                       │    Studio    │
│  Search,     │ <── [Save & Close] ── │ (Waveform,   │
│  Status)     │                       │  Timer, Stop)│
└──────────────┘                       └──────────────┘
       │                                       │
       │ [Tap Item]                            │ [Auto-routes on Stop]
       ▼                                       ▼
┌────────────────────────────────────────────────────────┐
│                   Workspace Hub                        │
│  ┌───────────────────────┬──────────────────────────┐  │
│  │   Document Editor     │    Verbatim Transcript   │  │
│  │ (Structured Chapters, │ (Timestamped Segments,   │  │
│  │  Speaker Sections)    │  Speaker Renaming, Raw)  │  │
│  └───────────────────────┴──────────────────────────┘  │
│                            │                           │
│                            ▼                           │
│              [Export Hub: PDF / MD / TXT]              │
└────────────────────────────────────────────────────────┘
```

1. **Library & Dashboard (Home)**:
   - Filterable list of meeting sessions with duration badges, timestamps, speaker count, and transcription status pills (`Recorded`, `Transcribing`, `Ready`, `Error`).
   - Quick search by title, speaker, or transcript snippet.
   - Prominent "Start New Recording" primary action.

2. **Recording Studio**:
   - Central elapsed timekeeper (`00:14:32`).
   - Real-time dynamic amplitude waveform rendered via Compose `Canvas`.
   - Recording controls: Pause, Resume, Stop & Save, Cancel.
   - Foreground notification status with background persistence guidance.
   - Storage safety indicator (available disk space check).

3. **Transcript & Document Workspace**:
   - Tabbed/Dual-view header: **Document** (editorial chapters) and **Raw Transcript** (chronological speaker timeline).
   - Speaker management dialog: Click speaker pill (`Speaker 1`) to rename globally (`Budi Santoso`).
   - Inline text editor with instant auto-save to Room database.
   - Reprocess with Gemini: Custom instruction prompt (e.g. "Focus on technical decisions while keeping Indonesian colloquial quotes").

4. **Export & Sharing Modal**:
   - Generation of paginated multi-page PDF with header, metadata, speaker transcript, and structured sections.
   - Markdown (`.md`) format ready for Obsidian, Notion, or GitHub.
   - Plain Text (`.txt`) for archival.
   - Native Android Intent share sheet.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Audio Capture & Background Reliability
- **Chosen Approach**: Native Android `ForegroundService` with `FOREGROUND_SERVICE_MICROPHONE` type and a sticky notification. Audio is captured via `AudioRecord` / `MediaRecorder` into standard AAC/M4A chunks stored in app-specific storage (`context.filesDir/recordings/`).
- **Why**: Survives screen lock, app minimization, and background throttling. Writing directly to disk prevents Out-Of-Memory (OOM) crashes on 30+ minute recordings.
- **Alternatives Considered**: In-process coroutine recording (killed when app is paused); WorkManager (designed for deferrable batch tasks, not continuous low-latency microphone capture).

### Decision 2: Speech-to-Text & Gemini AI Strategy
- **Chosen Approach**: Direct REST client using Retrofit + Moshi for Gemini API calls (`gemini-3.5-flash`), transmitting segmented audio parts or leveraging dual speech-to-text integration with Android's native `SpeechRecognizer` for on-device fallback.
- **Why**: Provides high-quality verbatim transcription for mixed Indonesian-English terminology, speaker labeling, and document structuring without lossy compression.
- **Integrity Enforcement**: Prompts enforce verbatim fidelity, forbidding synthetic summarization or removal of banter, interruptions, and questions.

### Decision 3: Local-First Persistence Strategy
- **Chosen Approach**: Room Database (`AppDatabase`) with three core tables: `MeetingEntity`, `TranscriptSegmentEntity`, and `DocumentSectionEntity`.
- **Why**: Decouples audio recording from AI processing. If an AI call fails or network drops, audio and raw transcript remain safely stored and can be re-run at any time.

---

## 4. Technical Architecture & Data Strategy

### System Component Architecture
```
┌────────────────────────────────────────────────────────┐
│                     UI Layer                           │
│  MainActivity ── Compose Navigation ── Screens:        │
│  [LibraryScreen] [RecordingScreen] [WorkspaceScreen]   │
└──────────────────────────┬─────────────────────────────┘
                           │ StateFlow / Actions
┌──────────────────────────▼─────────────────────────────┐
│                   ViewModel Layer                      │
│     MeetingViewModel ── RecordingViewModel             │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
┌──────────────▼──────────────┐ ┌─────────▼──────────────┐
│     Audio Recording Subsys  │ │     AI & Processing    │
│  - RecordingForegroundService│ │  - GeminiTranscription │
│  - AudioRecordManager       │ │  - DocumentOrganizer   │
│  - WaveformSampler (RMS)    │ │  - Retrofit REST Client│
└──────────────┬──────────────┘ └─────────┬──────────────┘
               │                          │
┌──────────────▼──────────────────────────▼──────────────┐
│                   Data & Storage Layer                 │
│  - MeetingRepository                                   │
│  - Room Database (Meetings, Segments, Sections)        │
│  - Local Disk Audio File Storage (M4A / AAC)          │
│  - PdfExportManager (Android PdfDocument)              │
└────────────────────────────────────────────────────────┘
```

### Room Database Schema
```
┌─────────────────────────┐       ┌───────────────────────────────┐
│     meetings            │       │     transcript_segments       │
├─────────────────────────┤       ├───────────────────────────────┤
│ id: String (PK)         │ 1   * │ id: String (PK)               │
│ title: String           │──────<│ meetingId: String (FK)        │
│ createdAt: Long         │       │ speaker: String               │
│ durationSeconds: Long   │       │ startTimeMs: Long             │
│ audioFilePath: String   │       │ endTimeMs: Long               │
│ status: String          │       │ text: String                  │
│ language: String        │       │ isEdited: Boolean             │
│ notes: String           │       └───────────────────────────────┘
└───────────┬─────────────┘
            │ 1
            │
            │ *
┌───────────▼─────────────────────┐
│     document_sections           │
├─────────────────────────────────┤
│ id: String (PK)                 │
│ meetingId: String (FK)          │
│ title: String                   │
│ content: String                 │
│ sectionOrder: Int               │
│ sectionType: String             │
└─────────────────────────────────┘
```

---

## 5. Scope & Implementation Milestones

### Phase A: Architecture, Documentation & Core Foundation (P0)
1. Initialize platform metadata (`metadata.json`, `strings.xml`, `applicationId` = `com.aistudio.guznotara.app`).
2. Generate custom adaptive launcher icon and background XMLs.
3. Author required Markdown documentation artifacts in the repository root:
   - `PROJECT_BRIEF.md`
   - `ARCHITECTURE.md`
   - `IMPLEMENTATION_PLAN.md`
   - `TECHNICAL_DECISIONS.md`
   - `TEST_PLAN.md`
   - `DESIGN_SYSTEM.md`
   - `KNOWN_LIMITATIONS.md`
4. Setup Room Database entities, DAOs, and repository.
5. Create Audio Recording Foreground Service with notification channel, microphone permission handling, and dynamic RMS amplitude sampling for waveforms.

### Phase B: Recording Studio & Real-Time Waveform (P0-P1)
1. Build `RecordingStudioScreen` with live time counter, active animated waveform, pause/resume/stop controls, and disk space health check.
2. Store recorded audio files locally in `filesDir/recordings/` with unique session IDs.
3. Handle graceful interruptions and app resumption.

### Phase C: Transcription & Gemini Document Pipeline (P1)
1. Configure Gemini REST API client with `.env.example` `GEMINI_API_KEY` configuration.
2. Implement audio segmentation and transcription processing pipeline tuned for Bahasa Indonesia with English technical vocabulary.
3. Implement `DocumentOrganizer` enforcing verbatim dialogue preservation, speaker attribution, and professional editorial formatting without summarization loss.
4. Provide on-device fallback speech recognition when API key is unconfigured or network is unavailable.

### Phase D: Workspace Hub, Editing & Export (P1-P2)
1. Build `WorkspaceScreen` with dual views: Structured Document & Chronological Verbatim Transcript.
2. Global speaker renaming tool (e.g. rename "Speaker 1" to "Dr. Arifin").
3. Implement `PdfExportManager` generating native Android `PdfDocument` files, formatted Markdown files, and plain text files with Android Sharesheet support.
4. Comprehensive search and filter in `LibraryScreen`.

### Phase E: Verification, Tests & Polish (P2)
1. Local unit and Robolectric tests for database CRUD, transcription data integrity, and export generation.
2. Full build verification with `compile_applet`.
