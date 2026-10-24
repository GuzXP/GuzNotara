package com.example.ai

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>,
    @Json(name = "role") val role: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: GeminiInlineData? = null
)

@JsonClass(generateAdapter = true)
data class GeminiInlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.2f,
    @Json(name = "responseMimeType") val responseMimeType: String? = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null
)

// Target JSON Structure returned by Gemini
@JsonClass(generateAdapter = true)
data class AiMeetingResult(
    @Json(name = "meetingTitle") val meetingTitle: String? = null,
    @Json(name = "segments") val segments: List<AiTranscriptSegment> = emptyList(),
    @Json(name = "sections") val sections: List<AiDocumentSection> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AiTranscriptSegment(
    @Json(name = "speaker") val speaker: String,
    @Json(name = "startTimeMs") val startTimeMs: Long,
    @Json(name = "endTimeMs") val endTimeMs: Long,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class AiDocumentSection(
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "sectionType") val sectionType: String // INFO, OVERVIEW, DISCUSSION, QA, CLARIFICATION, ACTION_ITEMS
)
