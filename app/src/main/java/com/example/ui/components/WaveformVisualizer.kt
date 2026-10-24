package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.LiveRecordingRed

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    isRecording: Boolean,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val barColor = if (isPaused) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    } else {
        LiveRecordingRed
    }

    // Default bars if recording hasn't started or list is empty
    val displayAmplitudes = if (amplitudes.isEmpty()) {
        List(30) { 0.1f }
    } else {
        amplitudes
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
    ) {
        val width = size.width
        val height = size.height
        val totalBars = displayAmplitudes.size
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (totalBars - 1)
        val barWidth = ((width - totalSpacing) / totalBars).coerceAtLeast(3.dp.toPx())

        for (i in displayAmplitudes.indices) {
            val amp = displayAmplitudes[i].coerceIn(0.08f, 1.0f)
            val barHeight = (amp * (height * 0.9f)).coerceAtLeast(6.dp.toPx())
            val x = i * (barWidth + barSpacing)
            val y = (height - barHeight) / 2f

            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    if (isPaused) barColor else CyanAccent,
                    barColor
                ),
                startY = y,
                endY = y + barHeight
            )

            drawRoundRect(
                brush = gradientBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
