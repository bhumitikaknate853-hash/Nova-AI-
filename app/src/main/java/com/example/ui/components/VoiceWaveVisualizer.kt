package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun VoiceWaveVisualizer(
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    isActive: Boolean = false,
    audioLevel: Float = 0f,
    primaryColor: Color = Color(0xFF00E5FF),
    secondaryColor: Color = Color(0xFF7C4DFF)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val width = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2f
        val barCount = 28
        val barWidth = width / (barCount * 1.6f)
        val gap = (width - (barCount * barWidth)) / (barCount - 1)

        val activeAmp = if (isActive) (0.25f + audioLevel * 0.75f) else 0.08f

        for (i in 0 until barCount) {
            val x = i * (barWidth + gap)
            val sinVal = sin((i.toFloat() / barCount) * Math.PI * 3 + phase).toFloat()
            val normalizedHeight = (canvasHeight * 0.85f) * activeAmp * (0.3f + 0.7f * kotlin.math.abs(sinVal))
            val barH = normalizedHeight.coerceIn(4f, canvasHeight * 0.95f)

            val barTop = centerY - barH / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, secondaryColor)
                ),
                topLeft = Offset(x, barTop),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
