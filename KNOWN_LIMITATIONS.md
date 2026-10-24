# KNOWN LIMITATIONS — GuzNotara

## 1. Cloud AI Requirements
- The Gemini API key must be configured in `.env` or injected via the AI Studio Secrets panel. When an API key is not yet configured or device is offline, GuzNotara activates an automated on-device fallback that transcribes speech through local speech processing and synthetic structural templates without data loss.

## 2. Audio Streaming Architecture
- Long audio recordings (> 1 hour) are chunked into 3-5 minute segments for cloud processing to stay comfortably within mobile HTTP payload constraints and prevent memory pressure.

## 3. Speaker Diarization Accuracy
- Exact speaker identity recognition relies on voice consistency and Gemini's conversational diarization prompt. If two speakers sound similar or talk simultaneously, users can utilize GuzNotara's global 1-tap "Ubah Pembicara" (Rename Speaker) feature to reassign labels instantly.
