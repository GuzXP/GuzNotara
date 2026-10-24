# DESIGN SYSTEM — GuzNotara

## 1. Design Philosophy
**Editorial Audio-Productivity**: Calm, typography-driven, high contrast, and distraction-free. Avoids decorative gimmicks in favor of crisp typography, confident rhythm, and functional audio feedback.

## 2. Color Palette
- **Deep Slate Base**: `#0F172A` (Obsidian Header & Primary Dark Background)
- **Primary Cobalt Accent**: `#2563EB` (Primary buttons, active tab indicators, speaker chips)
- **Secondary Slate**: `#334155` (Card borders, dividers, secondary tags)
- **Live Recording Red**: `#EF4444` (Pulse badge, recording indicator, stop button)
- **Warm Editorial Canvas**: `#F8FAFC` (Light mode reading background)
- **Surface Elevation**: `#FFFFFF` (Light cards) / `#1E293B` (Dark cards)

## 3. Typography Hierarchy
- **App Title / Brand**: Bold Sans, 22sp, tracking tight.
- **Section Headings**: Semi-bold, 18sp.
- **Body / Transcript**: Clean readable serif or sans, 15sp, line-height 22sp for effortless reading.
- **Timestamps & Speaker Chips**: Monospace Sans, 12sp, high contrast pill styling.

## 4. Key Component Standards
- Touch targets: strictly 48dp minimum interactive component size.
- Waveform: Real-time amplitude bars with smoothed interpolation and dynamic gradient fill.
- Speaker chips: Distinct tinted background per speaker (e.g. Speaker 1 = Cobalt tint, Speaker 2 = Emerald tint, Speaker 3 = Amber tint).
- TestTags applied to all interactive controls (`start_record_button`, `stop_record_button`, `tab_document`, `tab_transcript`, `export_pdf_button`, etc.).
