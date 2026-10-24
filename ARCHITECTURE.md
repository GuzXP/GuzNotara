# ARCHITECTURE — GuzNotara

## 1. System Layers

```
┌────────────────────────────────────────────────────────┐
│                   Presentation Layer                   │
│  - MainActivity                                        │
│  - Jetpack Compose Screens (Library, Studio, Workspace)│
│  - M3 Material Theme & Dynamic Waveform Canvas         │
└──────────────────────────┬─────────────────────────────┘
                           │ StateFlow & Intent Events
┌──────────────────────────▼─────────────────────────────┐
│                    ViewModel Layer                     │
│  - MeetingViewModel (List, Search, Workspace, Actions) │
│  - RecordingViewModel (Timer, Waveform, Session)       │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
┌──────────────▼──────────────┐ ┌─────────▼──────────────┐
│     Recording Subsystem     │ │     AI & Processing    │
│  - RecordingForegroundService│ │  - GeminiTranscription │
│  - AudioRecordManager       │ │  - DocumentOrganizer   │
│  - Realtime Amplitude Sample│ │  - Retrofit REST Client│
└──────────────┬──────────────┘ └─────────┬──────────────┘
               │                          │
┌──────────────▼──────────────────────────▼──────────────┐
│                  Data & Persistence Layer              │
│  - MeetingRepository                                   │
│  - Room Database (Meetings, Segments, Sections)        │
│  - App-specific File Storage (/recordings & /exports)  │
│  - Native PdfExportManager                             │
└────────────────────────────────────────────────────────┘
```

## 2. Data Flow
1. **Audio Capture**: `RecordingForegroundService` captures audio into AAC/M4A chunks via `MediaRecorder`/`AudioRecord` and emits RMS amplitude to `RecordingViewModel`.
2. **Session Persistence**: On recording completion, `MeetingEntity` is stored in the local Room database (`AppDatabase`).
3. **AI Transcription**: Audio segment is processed by `GeminiTranscriptionService` (`gemini-3.5-flash`), generating timestamped `TranscriptSegmentEntity` entries with speaker diarization.
4. **Document Structuring**: `DocumentOrganizer` converts raw segments into structured `DocumentSectionEntity` chapters while cross-referencing timestamps.
5. **Exporting**: `PdfExportManager` renders document and transcript directly into an Android `PdfDocument`, saved into `filesDir/exports/` and exposed via `FileProvider`.
