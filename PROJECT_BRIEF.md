# PROJECT BRIEF — GuzNotara

## 1. Product Overview
**GuzNotara** is a native Android meeting transcription and document workspace inspired by Meeting.ai, Uniscribe, and TurboScribe. It focuses on complete conversational fidelity, capturing speech locally, performing timestamped verbatim transcription, and organizing transcripts into structured documents without lossy summarization.

## 2. Key Objectives
- **Zero Content Reduction**: Retain greetings, interruptions, small talk, questions, clarifications, repetitions, and informal nuances.
- **Local-First Recording**: Audio is captured via a foreground service and written immediately to persistent storage, surviving app minimization or device interruptions.
- **Indonesian-First & Mixed English**: Tailored for Indonesian professional and academic settings where technical terms in English are frequently mixed with Indonesian dialogue.
- **Synchronized Dual View**: Users can toggle between an AI-organized document (with sections, headings, and speaker notes) and the original chronological verbatim transcript with timestamps.
- **Native Multi-Format Export**: Real Android `PdfDocument` generation, Markdown (`.md`), and plain text (`.txt`), with direct Android Sharesheet integration.

## 3. Core Acceptance Criteria
1. Background recording continues reliably while the device screen is off.
2. Waveform visualizer animates dynamically using live RMS amplitude data from the microphone.
3. Raw transcripts are permanently preserved and editable (including speaker renaming).
4. AI structuring preserves 100% of substantive statements and colloquial context.
5. Exported PDF produces clean formatted pages with title, metadata, structured sections, and full dialogue.
