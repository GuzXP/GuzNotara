# TECHNICAL DECISIONS — GuzNotara

### 1. ForegroundService for Audio Capture
- **Decision**: Use a dedicated `ForegroundService` with `microphone` foregroundServiceType and a persistent status bar notification.
- **Rationale**: Android aggressively kills background tasks. A foreground service guarantees that meetings up to several hours remain uninterrupted even when the user locks their device or switches to other apps.

### 2. Audio Storage: Local-First Disk Persistence
- **Decision**: Store audio files in app-internal files directory (`context.filesDir/recordings/`) formatted in MPEG-4 AAC (`.m4a`).
- **Rationale**: Streaming audio directly to disk avoids memory bloat and protects against process termination. The user explicitly controls audio retention or deletion.

### 3. Room Database for Structured Metadata
- **Decision**: Store meetings, raw segments, and structured sections in Room with foreign keys and cascading deletes.
- **Rationale**: Enables instant reactive UI updates via Kotlin Flow, supports full-text search, and decouples local audio recordings from cloud AI operations.

### 4. Gemini 3.5 Flash Model for Transcription & Structuring
- **Decision**: Use `gemini-3.5-flash` via Direct REST API with `OkHttpClient` 60s timeouts.
- **Rationale**: Balances high multimodal reasoning speed with accurate bilingual Indonesian-English audio transcription and verbatim context preservation without hallucinating omissions.

### 5. Genuine Native Android PdfDocument Export
- **Decision**: Use Android's built-in `android.graphics.pdf.PdfDocument` and Canvas text rendering.
- **Rationale**: Guarantees zero reliance on heavyweight third-party PDF SDKs, works 100% offline, and produces authentic formatted PDF files shareable through `FileProvider`.
